import cv2
import subprocess as sp
import threading
import time
from flask import Flask, jsonify

app = Flask(__name__)

# 视频文件路径
VIDEO_FILE = r"D:\PythonCode\rtsp_images\96d1c5d9934cb9745f4e2c093bf483b7.mp4"
RTMP_URL = "rtmp://127.0.0.1:1935/live/uav"

# FFmpeg 路径配置
FFMPEG_EXE = r"D:\idea\ffmpeg-8.1\ffmpeg-8.0.1-full_build\bin\ffmpeg.exe"

push_running = False
cap = None
pipe = None

def push_worker():
    global push_running, cap, pipe
    # 打开视频
    cap = cv2.VideoCapture(VIDEO_FILE)
    if not cap.isOpened():
        print("❌ 视频文件打开失败！请检查路径是否正确")
        push_running = False
        return

    # 强制读取宽高（如果为0，手动设置一个默认值）
    fps = cap.get(cv2.CAP_PROP_FPS) or 30
    w = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
    h = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
    if w == 0 or h == 0:
        print("⚠️  视频宽高读取失败，使用默认 1280x720")
        w, h = 1280, 720

    print(f"✅ 视频信息：{w}x{h} @ {fps}fps")

    # FFmpeg 命令（去掉可能导致问题的参数）
    command = [
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

    pipe = sp.Popen(command, stdin=sp.PIPE, stderr=sp.DEVNULL)

    while push_running:
        ret, frame = cap.read()
        if not ret:
            # 视频播放完毕，循环
            cap.set(cv2.CAP_PROP_POS_FRAMES, 0)
            continue

        # 确保帧大小匹配（防止宽高为0导致的异常）
        if frame.shape[1] != w or frame.shape[0] != h:
            frame = cv2.resize(frame, (w, h))

        try:
            pipe.stdin.write(frame.tobytes())
        except Exception as e:
            print("❌ FFmpeg 管道错误：", e)
            break

    # 释放资源
    cap.release()
    if pipe and pipe.stdin:
        pipe.stdin.close()
        pipe.wait()
    push_running = False
    print("✅ 推流已停止")

@app.route("/api/push/start", methods=["POST"])
def start_push():
    global push_running, push_thread
    if push_running:
        return jsonify({"code": 0, "msg": "推流正在运行", "rtmp": RTMP_URL})
    push_running = True
    push_thread = threading.Thread(target=push_worker, daemon=True)
    push_thread.start()
    return jsonify({"code": 0, "msg": "推流启动成功", "rtmp": RTMP_URL})

@app.route("/api/push/stop", methods=["POST"])
def stop_push():
    global push_running
    push_running = False
    return jsonify({"code": 0, "msg": "推流已停止"})

# 跨域支持
@app.after_request
def add_cors(response):
    response.headers["Access-Control-Allow-Origin"] = "*"
    return response

if __name__ == "__main__":
    print("🚀 推流服务运行在：http://127.0.0.1:5001")
    print("📌 启动：POST /api/push/start")
    print("📌 停止：POST /api/push/stop")
    app.run(host="0.0.0.0", port=5001, debug=False, use_reloader=False)