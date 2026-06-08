import cv2
import os
import time
import subprocess
import threading
import requests
from queue import Queue
from flask import Flask, jsonify, Response
from ultralytics import YOLO

app = Flask(__name__)

# ===================== 配置 =====================
RTSP_URL = "rtsp://192.168.31.220:8080/h264_pcm.sdp"
RTMP_URL = "rtmp://127.0.0.1:1935/live/uav"
FFMPEG_EXE = r"D:\ffmpeg\bin\ffmpeg.exe"

UPLOAD_API = "http://127.0.0.1:8080/api/python/upload"
REPORT_API = "http://127.0.0.1:6666/api/receive"

TEMP_DIR = "./temp"
SEGMENT_DIR = os.path.join(TEMP_DIR, "segments")
IMAGE_DIR = os.path.join(TEMP_DIR, "images")
os.makedirs(SEGMENT_DIR, exist_ok=True)
os.makedirs(IMAGE_DIR, exist_ok=True)

# 全局状态
stream_running = False
recording_start_time = None
MAX_RECORDING_DURATION = 3600  # 1小时

# 加载 YOLO 模型
print("正在加载 YOLO 模型...")
model = YOLO("yolov8n.pt")
print("YOLO 模型加载完成")

# 队列
frame_queue = Queue(maxsize=30)
raw_frame_queue = Queue(maxsize=30)

# ===================== RTSP 拉流 + RTMP 推流 =====================
def pull_and_push():
    """从 RTSP 拉流，推送到 RTMP，同时分发帧到队列"""
    global stream_running

    while stream_running:
        try:
            # 使用 OpenCV 读取 RTSP 流
            cap = cv2.VideoCapture(RTSP_URL)
            if not cap.isOpened():
                print("RTSP 连接失败，等待重连...")
                time.sleep(2)
                continue

            print("RTSP 连接成功！")

            # 获取视频信息
            fps = cap.get(cv2.CAP_PROP_FPS) or 30
            w = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
            h = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
            if w == 0 or h == 0:
                w, h = 1280, 720

            print(f"视频信息：{w}x{h} @ {fps}fps")

            # 使用 FFmpeg 推流到 RTMP
            cmd = [
                FFMPEG_EXE,
                "-y",
                "-f", "rawvideo",
                "-pix_fmt", "bgr24",
                "-s", f"{w}x{h}",
                "-r", str(fps),
                "-i", "-",
                "-c:v", "libx264",
                "-preset", "ultrafast",
                "-tune", "zerolatency",
                "-f", "flv",
                RTMP_URL
            ]

            pipe = subprocess.Popen(cmd, stdin=subprocess.PIPE, stderr=subprocess.DEVNULL)

            frame_count = 0

            while stream_running:
                ret, frame = cap.read()
                if not ret:
                    print("RTSP 帧读取失败，重连...")
                    break

                frame_count += 1

                # 分发帧到队列
                if frame_queue.qsize() < 30:
                    frame_queue.put(frame.copy())

                if raw_frame_queue.qsize() < 30:
                    raw_frame_queue.put(frame.copy())

                # 推流到 RTMP
                try:
                    pipe.stdin.write(frame.tobytes())
                except Exception as e:
                    print(f"FFmpeg 管道错误：{e}")
                    break

                if frame_count % 30 == 0:
                    print(f"已处理 {frame_count} 帧")

            cap.release()
            if pipe.stdin:
                pipe.stdin.close()
            pipe.wait()

        except Exception as e:
            print(f"RTSP 流异常: {e}")
            time.sleep(2)

# ===================== 目标检测 =====================
last_objects = []
last_upload_time = 0
IMAGE_UPLOAD_RATE = 0.5  # 0.5秒限频

def detect_stream():
    """目标检测线程"""
    global stream_running, last_objects, last_upload_time

    print("检测线程启动")
    frame_count = 0

    while stream_running:
        try:
            if raw_frame_queue.empty():
                time.sleep(0.01)
                continue

            frame = raw_frame_queue.get(timeout=1)
            frame_count += 1

            # YOLO 检测
            try:
                results = model.predict(frame, conf=0.2, iou=0.3, verbose=False)
                res = results[0]
                current_objects = []

                for box in res.boxes:
                    cls_id = int(box.cls[0])
                    x1, y1, x2, y2 = box.xyxy[0].tolist()
                    current_objects.append({
                        "class_name": model.names[cls_id],
                        "confidence": round(box.conf[0].item(), 2),
                        "bbox": [round(x1, 2), round(y1, 2), round(x2, 2), round(y2, 2)]
                    })

                # 检测结果变化时上传带框图片
                if current_objects != last_objects:
                    current_time = time.time()
                    if current_time - last_upload_time >= IMAGE_UPLOAD_RATE:
                        last_upload_time = current_time
                        upload_boxed_image(frame, current_objects)

                last_objects = current_objects

            except Exception as e:
                print(f"检测异常: {e}")
                continue

            if frame_count % 30 == 0:
                print(f"已检测 {frame_count} 帧")

        except Exception as e:
            print(f"检测线程异常: {e}")
            time.sleep(0.1)

def upload_boxed_image(frame, objects):
    """上传带框图片到 Java 后端"""
    try:
        # 画框
        draw = frame.copy()
        for obj in objects:
            x1, y1, x2, y2 = map(int, obj["bbox"])
            label = f"{obj['class_name']} {obj['confidence']:.2f}"
            cv2.rectangle(draw, (x1, y1), (x2, y2), (0, 255, 0), 2)
            cv2.putText(draw, label, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 2)

        # 保存临时文件
        ts = int(time.time() * 1000)
        path = os.path.join(IMAGE_DIR, f"{ts}_box.jpg")
        cv2.imwrite(path, draw)

        # 上传到 Java 后端
        with open(path, 'rb') as f:
            files = {'file': (f'{ts}_box.jpg', f, 'image/jpeg')}
            data = {'directory': 'frames'}
            res = requests.post(f"{UPLOAD_API}/image", files=files, data=data, timeout=10)
            if res.status_code == 200:
                result = res.json()
                print(f"带框图片已上传: {result.get('data', {}).get('objectName', '')}")
            else:
                print(f"图片上传失败: {res.text}")

        # 清理临时文件
        os.remove(path)

    except Exception as e:
        print(f"图片上传异常: {e}")

# ===================== 视频录制 + 上传 =====================
def record_video():
    """录制视频，1小时后自动停止，每10分钟上传一次"""
    global stream_running, recording_start_time

    print("录制线程启动")

    # 清理旧的 segment 文件
    for f in os.listdir(SEGMENT_DIR):
        if f.startswith("segment_") and f.endswith(".mp4"):
            os.remove(os.path.join(SEGMENT_DIR, f))

    segment_pattern = os.path.join(SEGMENT_DIR, "segment_%03d.mp4")

    cmd = [
        FFMPEG_EXE,
        "-i", RTMP_URL,
        "-c:v", "libx264",
        "-preset", "ultrafast",
        "-g", "300",  # 关键帧间隔
        "-f", "segment",
        "-segment_time", "600",  # 10分钟一个片段
        "-reset_timestamps", "1",
        "-segment_format", "mp4",
        "-y", segment_pattern
    ]

    proc = subprocess.Popen(cmd, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    recording_start_time = time.time()

    # 监控录制时长
    while stream_running and proc.poll() is None:
        elapsed = time.time() - recording_start_time
        if elapsed >= MAX_RECORDING_DURATION:
            print("录制已达1小时，自动停止")
            stream_running = False
            break
        time.sleep(1)

    if proc.poll() is None:
        proc.terminate()
        proc.wait()

    # 上传所有片段
    upload_all_segments()

def upload_all_segments():
    """上传所有视频片段到 Java 后端"""
    try:
        files = [f for f in os.listdir(SEGMENT_DIR)
                 if f.startswith("segment_") and f.endswith(".mp4")]
        files.sort()

        for f in files:
            filepath = os.path.join(SEGMENT_DIR, f)
            if os.path.getsize(filepath) < 1024:
                continue

            with open(filepath, 'rb') as file:
                files_data = {'file': (f, file, 'video/mp4')}
                data = {'directory': 'video'}
                res = requests.post(f"{UPLOAD_API}/video", files=files_data, data=data, timeout=60)
                if res.status_code == 200:
                    result = res.json()
                    object_name = result.get('data', {}).get('objectName', '')
                    print(f"视频片段已上传: {object_name}")

                    # 上报后端
                    ts = int(time.time() * 1000)
                    requests.post(REPORT_API, json={
                        "timestamp": ts,
                        "raw_video_path": object_name
                    }, timeout=2)
                else:
                    print(f"视频上传失败: {res.text}")

            os.remove(filepath)

    except Exception as e:
        print(f"视频上传异常: {e}")

# ===================== 跨域 =====================
@app.after_request
def add_cors(response):
    response.headers["Access-Control-Allow-Origin"] = "*"
    response.headers["Access-Control-Allow-Methods"] = "POST,GET,OPTIONS"
    return response

# ===================== 直播流 =====================
@app.route("/api/live/flv")
def live_flv():
    """FLV 直播流"""
    def generate():
        while stream_running:
            try:
                p = subprocess.Popen(
                    [FFMPEG_EXE, "-i", RTMP_URL, "-f", "flv", "-vcodec", "copy", "-an", "-"],
                    stdout=subprocess.PIPE,
                    stderr=subprocess.DEVNULL,
                    bufsize=0
                )
                while stream_running:
                    data = p.stdout.read(8192)
                    if not data:
                        break
                    yield data
                p.terminate()
            except:
                time.sleep(0.5)
    return Response(generate(), mimetype="video/x-flv")

# ===================== 启动/停止 =====================
@app.route("/api/stream/start", methods=["POST"])
def start_stream():
    global stream_running
    if stream_running:
        return jsonify({"code": 0, "msg": "流已在运行"})

    stream_running = True

    # 启动各线程
    threading.Thread(target=pull_and_push, daemon=True).start()
    threading.Thread(target=detect_stream, daemon=True).start()
    threading.Thread(target=record_video, daemon=True).start()

    return jsonify({"code": 0, "msg": "流启动成功"})

@app.route("/api/stream/stop", methods=["POST"])
def stop_stream():
    global stream_running
    stream_running = False

    return jsonify({"code": 0, "msg": "流已停止"})

# 兼容旧的 API 端点
@app.route("/api/pull/start", methods=["POST"])
def start_pull():
    return start_stream()

@app.route("/api/pull/stop", methods=["POST"])
def stop_pull():
    return stop_stream()

# ===================== 带框直播流 =====================
@app.route("/api/live/boxed/mjpeg")
def live_boxed_mjpeg():
    """带框 MJPEG 直播流"""
    def generate():
        while stream_running:
            try:
                if not raw_frame_queue.empty():
                    frame = raw_frame_queue.get(timeout=1)
                    # 画框
                    draw = frame.copy()
                    for obj in last_objects:
                        x1, y1, x2, y2 = map(int, obj["bbox"])
                        label = f"{obj['class_name']} {obj['confidence']:.2f}"
                        cv2.rectangle(draw, (x1, y1), (x2, y2), (0, 255, 0), 2)
                        cv2.putText(draw, label, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 2)

                    ret, jpeg = cv2.imencode('.jpg', draw, [cv2.IMWRITE_JPEG_QUALITY, 60])
                    if ret:
                        yield (b'--frame\r\n'
                               b'Content-Type: image/jpeg\r\n'
                               b'Content-Length: ' + str(len(jpeg)).encode() + b'\r\n\r\n' + jpeg.tobytes() + b'\r\n')
                else:
                    time.sleep(0.033)
            except Exception as e:
                time.sleep(0.1)
    return Response(generate(), mimetype='multipart/x-mixed-replace; boundary=frame')

# ===================== 主入口 =====================
if __name__ == "__main__":
    print("="*50)
    print("无人机巡检服务启动")
    print(f"RTSP 源: {RTSP_URL}")
    print(f"RTMP 输出: {RTMP_URL}")
    print("="*50)
    print("API:")
    print("  POST /api/stream/start - 启动流")
    print("  POST /api/stream/stop - 停止流")
    print("  GET /api/live/flv - 直播流")
    print("="*50)

    app.run(host="0.0.0.0", port=5003, debug=False)
