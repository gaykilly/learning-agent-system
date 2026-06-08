import cv2
import os
import threading
import requests
import subprocess
import time
import shutil
from queue import Queue
from minio import Minio
from flask import Flask, jsonify, Response

app = Flask(__name__)

# ===================== 配置 =====================
RTMP_URL = "rtmp://127.0.0.1:1935/live/uav"

# FFmpeg 路径配置
FFMPEG_EXE = r"D:\idea\ffmpeg-8.1\ffmpeg-8.0.1-full_build\bin\ffmpeg.exe"
DETECT_API = "http://127.0.0.1:5002/detect"
REPORT_API = "http://127.0.0.1:6666/api/receive"
DETECT_TIMEOUT = 2.0  # 识别API超时时间（秒）

MINIO_CLIENT = Minio("127.0.0.1:9000", "minioadmin", "minioadmin", secure=False)
MINIO_BUCKET = "uav-stream"
TEMP_DIR = "./temp"
os.makedirs(TEMP_DIR, exist_ok=True)

pull_running = False

# 队列
raw_frame_queue = Queue(maxsize=30)
boxed_frame_queue = Queue(maxsize=30)

# ===================== 跨域 =====================
@app.after_request
def add_cors(response):
    response.headers["Access-Control-Allow-Origin"] = "*"
    response.headers["Access-Control-Allow-Methods"] = "POST,GET,OPTIONS"
    return response

# ===================== 上报后端 =====================
def report_to_backend(data):
    try:
        requests.post(REPORT_API, json=data, timeout=0.5)
    except:
        pass

# ===================== 拉流 + 识别 + 画框 =====================
def pull_stream():
    global pull_running
    print("✅ 拉流+识别线程启动")
    frame_count = 0
    last_objects = []

    while pull_running:
        try:
            cap = cv2.VideoCapture(RTMP_URL)
            if not cap.isOpened():
                print("⏳ 等待 RTMP 推流...")
                time.sleep(1)
                continue

            print("✅ RTMP 连接成功！")
            while pull_running:
                ret, frame = cap.read()
                if not ret:
                    print("❌ 拉流断开，自动重连")
                    break

                frame_count += 1

                # 原始帧（降低帧率）
                if frame_count % 2 == 0 and raw_frame_queue.qsize() < 30:
                    raw_frame_queue.put(frame.copy())

                # 识别（每3帧识别一次，降低API调用频率）
                if frame_count % 3 == 0:
                    try:
                        _, img_bytes = cv2.imencode(".jpg", frame)
                        res = requests.post(DETECT_API, files={"file": img_bytes}, timeout=0.2)
                        last_objects = res.json().get("objects", [])
                    except Exception as e:
                        print(f"识别API调用失败: {e}")

                # 画框（使用最新识别结果）
                draw = frame.copy()
                for obj in last_objects:
                    x1, y1, x2, y2 = map(int, obj["bbox"])
                    cv2.rectangle(draw, (x1, y1), (x2, y2), (0, 255, 0), 2)

                # 保存用队列（降低帧率）
                if frame_count % 2 == 0:
                    ts = int(time.time() * 1000)
                    if boxed_frame_queue.qsize() < 30:
                        boxed_frame_queue.put((ts, draw))

                # 上报（降低频率）
                if frame_count % 30 == 0:
                    report_to_backend({
                        "timestamp": ts,
                        "status": "success",
                        "box_img_path": f"frames/{ts}_box.jpg"
                })

            cap.release()
            time.sleep(1)
        except:
            time.sleep(1)

# ===================== 上传带框图片 =====================
def upload_box_image():
    while pull_running:
        try:
            ts, frame = boxed_frame_queue.get(timeout=1)
            path = os.path.join(TEMP_DIR, f"{ts}_box.jpg")
            cv2.imwrite(path, frame)
            MINIO_CLIENT.fput_object(MINIO_BUCKET, f"frames/{ts}_box.jpg", path)
            os.remove(path)
        except:
            continue

# ===================== 上传带框视频 =====================
def upload_box_video():
    fps = 20
    out = None
    current_ts = None
    while pull_running:
        try:
            ts, frame = boxed_frame_queue.get(timeout=1)
            h, w = frame.shape[:2]
            if out is None:
                current_ts = int(time.time() * 1000)
                vid_path = os.path.join(TEMP_DIR, f"{current_ts}_box.mp4")
                out = cv2.VideoWriter(vid_path, cv2.VideoWriter_fourcc(*'mp4v'), fps, (w, h))
            out.write(frame)
            if time.time() % 10 < 1:
                out.release()
                minio_path = f"boxed_video/{current_ts}_box.mp4"
                MINIO_CLIENT.fput_object(MINIO_BUCKET, minio_path, vid_path)
                os.remove(vid_path)
                out = None
                report_to_backend({"timestamp": current_ts, "box_video_path": minio_path})
        except:
            if out:
                try:
                    out.release()
                except:
                    pass
            out = None

# ===================== 上传原始视频 =====================
def upload_raw_video():
    fps = 20
    out = None
    current_ts = None
    while pull_running:
        try:
            frame = raw_frame_queue.get(timeout=1)
            h, w = frame.shape[:2]
            if out is None:
                current_ts = int(time.time() * 1000)
                vid_path = os.path.join(TEMP_DIR, f"{current_ts}_raw.mp4")
                out = cv2.VideoWriter(vid_path, cv2.VideoWriter_fourcc(*'mp4v'), fps, (w, h))
            out.write(frame)
            if time.time() % 10 < 1:
                out.release()
                minio_path = f"video/{current_ts}_raw.mp4"
                MINIO_CLIENT.fput_object(MINIO_BUCKET, minio_path, vid_path)
                os.remove(vid_path)
                out = None
                report_to_backend({"timestamp": current_ts, "raw_video_path": minio_path})
        except:
            if out:
                try:
                    out.release()
                except:
                    pass
            out = None

# ===================== ✅ 原版直播：直接转发RTMP，不带框 =====================
@app.route("/api/live/flv")
def live_flv():
    def generate():
        while pull_running:
            try:
                p = subprocess.Popen([
                    FFMPEG_EXE, "-i", RTMP_URL,
                    "-f", "flv", "-vcodec", "copy", "-"
                ], stdout=subprocess.PIPE, stderr=subprocess.DEVNULL)
                while pull_running:
                    data = p.stdout.read(1024 * 1024)
                    if not data:
                        break
                    yield data
                p.terminate()
            except:
                time.sleep(1)
    return Response(generate(), mimetype="video/x-flv")

# ===================== 🎯 带框直播：使用识别结果画框 =====================
@app.route("/api/live/boxed/mjpeg")
def live_boxed_mjpeg():
    def generate():
        global pull_running, boxed_frame_queue
        last_frame = None
        frame_count = 0
        while pull_running:
            try:
                ts, frame = boxed_frame_queue.get(timeout=0.1)
                last_frame = frame
                # 每2帧发送一次，降低带宽
                frame_count += 1
                if frame_count % 2 != 0:
                    continue
                # 降低JPEG质量，减少数据量
                ret, jpeg = cv2.imencode('.jpg', frame, [cv2.IMWRITE_JPEG_QUALITY, 60])
                if ret:
                    yield (b'--frame\r\n'
                           b'Content-Type: image/jpeg\r\n'
                           b'Content-Length: ' + str(len(jpeg)).encode() + b'\r\n\r\n' + jpeg.tobytes() + b'\r\n')
            except Exception as e:
                if last_frame is not None:
                    frame_count += 1
                    if frame_count % 2 == 0:
                        ret, jpeg = cv2.imencode('.jpg', last_frame, [cv2.IMWRITE_JPEG_QUALITY, 60])
                        if ret:
                            yield (b'--frame\r\n'
                                   b'Content-Type: image/jpeg\r\n'
                                   b'Content-Length: ' + str(len(jpeg)).encode() + b'\r\n\r\n' + jpeg.tobytes() + b'\r\n')
                time.sleep(0.033)
                continue
    return Response(generate(), mimetype='multipart/x-mixed-replace; boundary=frame')

# ===================== 启动 / 停止 =====================
@app.route("/api/pull/start", methods=["POST"])
def start_pull():
    global pull_running
    pull_running = True
    threading.Thread(target=pull_stream, daemon=True).start()
    threading.Thread(target=upload_box_image, daemon=True).start()
    threading.Thread(target=upload_box_video, daemon=True).start()
    threading.Thread(target=upload_raw_video, daemon=True).start()
    return jsonify(msg="启动成功")

@app.route("/api/pull/stop", methods=["POST"])
def stop_pull():
    global pull_running
    pull_running = False
    return jsonify(msg="已停止")

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5003, debug=False)