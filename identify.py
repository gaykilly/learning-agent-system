from ultralytics import YOLO
import cv2
import numpy as np
from flask import Flask, request, jsonify

app = Flask(__name__)

# 一启动就加载模型，永远不会丢
model = YOLO("yolov8n.pt")
print("="*50)
print("✅ 识别服务已启动，模型加载完成")
print("="*50)

# 跨域
@app.after_request
def cors(resp):
    resp.headers["Access-Control-Allow-Origin"] = "*"
    return resp

# 测试接口（能访问就代表服务正常）
@app.route("/test", methods=["GET"])
def test():
    return jsonify({"status": "ok", "msg": "识别服务正常运行"})

# 识别接口（你之前能识别，就是这个逻辑）
@app.route("/detect", methods=["POST"])
def detect():
    try:
        file = request.files["file"]
        img_bytes = file.read()
        nparr = np.frombuffer(img_bytes, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

        # 低置信度，确保能识别到
        results = model.predict(img, conf=0.2, iou=0.3)
        res = results[0]

        objects = []
        for box in res.boxes:
            cls_id = int(box.cls[0])
            x1, y1, x2, y2 = box.xyxy[0].tolist()
            objects.append({
                "class_name": model.names[cls_id],
                "confidence": round(box.conf[0].item(), 2),
                "bbox": [round(x1, 2), round(y1, 2), round(x2, 2), round(y2, 2)]
            })

        return jsonify({
            "code": 200,
            "count": len(objects),
            "objects": objects
        })

    except Exception as e:
        return jsonify({"code": 500, "error": str(e)})

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5002, debug=False, use_reloader=False)