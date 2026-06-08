# 无人机管控平台 - 项目说明

## 项目简介
这是一个基于 Vue3 + Vite 的无人机管控平台，包含实时视频直播、地图管理、项目管理等功能。

## 文件夹结构
```
vue-chuban/
├── public/videos/          # 视频文件
├── src/                     # Vue前端源代码
│   ├── components/          # 组件
│   ├── views/               # 页面
│   └── ...
├── camera.py               # 推流服务（5001端口）
├── identify.py             # 识别服务（5002端口）
├── transform.py            # 拉流服务（5003端口）
├── yolov8n.pt              # YOLO模型文件
├── nginx.conf              # Nginx配置文件
└── package.json            # 前端依赖
```

## 环境要求
- Node.js 16+
- Python 3.8+
- FFmpeg（已包含在项目中）
- Nginx（已配置RTMP服务）

## 快速启动

### 1. 安装前端依赖
```bash
cd vue-chuban
npm install
```

### 2. 启动后端服务（需要4个终端）

#### 终端1：启动 Nginx RTMP 服务
```bash
cd D:\idea\nginx-1.7.11.3\Gryphon
nginx.exe -c conf\nginx.conf
```
或者双击运行：
```
nginx-1.7.11.3\Gryphon\nginx.exe
```

#### 终端2：启动推流服务（5001端口）
```bash
cd vue-chuban
python camera.py
```

#### 终端3：启动识别服务（5002端口）
```bash
cd vue-chuban
pip install ultralytics flask opencv-python
python identify.py
```

#### 终端4：启动拉流服务（5003端口）
```bash
cd vue-chuban
pip install minio flask
python transform.py
```

### 3. 启动前端
```bash
cd vue-chuban
npm run dev
```

### 4. 访问系统
打开浏览器访问：http://localhost:8083

## 功能说明

### 视频直播
1. 点击「启动推流」按钮
2. 点击「启动拉流+识别+直播」按钮
3. 即可观看实时视频

### 地图功能
- 在项目列表页面可以创建新项目
- 点击地图选择项目位置
- 进入项目后地图会自动跳转到项目位置

## 常见问题

### 1. FFmpeg 找不到
如果提示 FFmpeg 找不到，请修改 Python 文件中的路径：
```python
FFMPEG_EXE = r"你的ffmpeg路径\ffmpeg.exe"
```

### 2. 端口被占用
如果提示端口被占用，请检查是否有其他服务占用了 5001、5002、5003 或 8083 端口。

### 3. 视频无法播放
请确保 Nginx 服务已启动，并且推流和拉流服务都已正常运行。

## 技术栈
- 前端：Vue3 + Vite + TailwindCSS + 高德地图
- 后端：Python Flask + OpenCV + YOLO
- 流媒体：Nginx RTMP + FFmpeg
- 播放器：flv.js + HLS.js

## 许可证
内部项目