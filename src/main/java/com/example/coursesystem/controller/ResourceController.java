package com.example.coursesystem.controller;

import com.example.coursesystem.agent.ResourceAgent;
import com.example.coursesystem.entity.ResourceHistory;
import com.example.coursesystem.repository.ResourceHistoryRepository;
import com.example.coursesystem.service.ProfileService;
import jakarta.persistence.EntityManager;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.xslf.usermodel.*;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.List;

@RestController
@RequestMapping("/api/resource")
@CrossOrigin(origins = "*")
public class ResourceController {

    @Autowired
    private ResourceHistoryRepository historyRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ResourceAgent resourceAgent;

    @Autowired
    private ProfileService profileService;

    // ==================== 流式AI调用 ====================
    private Flux<String> callAIStream(String prompt, String studentId) {
        String profileContext = profileService.getProfileContext(studentId);
        return resourceAgent.processStream(prompt, profileContext);
    }

    // ==================== 非流式AI调用 ====================
    private String callAI(String prompt, String studentId) {
        String profileContext = profileService.getProfileContext(studentId);
        return resourceAgent.process(prompt, profileContext);
    }

    // ==================== 图片生成 ====================
    private byte[] generateFreeImage() {
        try {
            String imageUrl = "https://picsum.photos/1024/576?random=" + System.currentTimeMillis();
            BufferedImage image = ImageIO.read(new URL(imageUrl));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    // ==================== 1 教学大纲 ====================
    @PostMapping("/syllabus")
    public String generateSyllabus(@RequestBody RequestDTO dto) {
        String prompt = String.format("""
                请为以下知识点生成一份完整的高校教学大纲：
                知识点：%s
                要求：
                1. 包含课程名称、课程性质、学分、总学时
                2. 分章节划分教学内容，每章包含教学目标、教学重点、教学难点、建议学时分条详细展开说明
                3. 教学方法与教学手段：多媒体、案例、讨论、翻转课堂等详细说明。
                4. 课程考核方式与成绩构成：平时成绩、作业、考勤、期中、期末比例全部写清楚。
                5. 推荐教材、参考资料、网络资源。
                6. 课程学习建议、学习注意事项。
                """, dto.getInput());

        String result = callAI(prompt, dto.getStudentId());
        saveHistory("syllabus", dto.getInput(), result);
        return result;
    }

    // ==================== 1 教学大纲（流式） ====================
    @GetMapping(value = "/syllabus/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateSyllabusStream(
            @RequestParam String input,
            @RequestParam(defaultValue = "default") String studentId) {
        String prompt = String.format("""
                请为以下知识点生成一份完整的高校教学大纲：
                知识点：%s
                要求：
                1. 包含课程名称、课程性质、学分、总学时
                2. 分章节划分教学内容，每章包含教学目标、教学重点、教学难点、建议学时分条详细展开说明
                3. 教学方法与教学手段：多媒体、案例、讨论、翻转课堂等详细说明。
                4. 课程考核方式与成绩构成：平时成绩、作业、考勤、期中、期末比例全部写清楚。
                5. 推荐教材、参考资料、网络资源。
                6. 课程学习建议、学习注意事项。
                """, input);

        return callAIStream(prompt, studentId);
    }

    // ==================== 2 PPT ====================
    @PostMapping("/ppt")
    public String generatePPT(@RequestBody RequestDTO dto) {
        String prompt = String.format("""
                请为以下知识点生成一份完整的PPT课件大纲，严格按以下格式输出：
                知识点：%s
                格式要求（约10-20页）：
                1. 第1页：标题页，包含课程名称、副标题
                2. 第2页：目录页，列出所有章节
                3. 第3页及以后：每个章节1-2页，每页包含【标题】和【5-7个核心要点】
                4. 最后1页：总结页+致谢
                5. 每页用【第X页：标题】开头，要点用-开头
                6. 结构必须包含：封面页、课程介绍页、教学目标页、目录页、每章知识点详解页、案例分析页、重点总结页、课堂提问页、课堂练习页、总结复习页、参考文献页、致谢页。
                7.每页内容都要写详细一点、适合课堂讲课使用。
                """, dto.getInput());

        String result = callAI(prompt, dto.getStudentId());
        saveHistory("ppt", dto.getInput(), result);
        return result;
    }

    // ==================== 2 PPT（流式） ====================
    @GetMapping(value = "/ppt/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generatePPTStream(
            @RequestParam String input,
            @RequestParam(defaultValue = "default") String studentId) {
        String prompt = String.format("""
                请为以下知识点生成一份完整的PPT课件大纲，严格按以下格式输出：
                知识点：%s
                格式要求（约10-20页）：
                1. 第1页：标题页，包含课程名称、副标题
                2. 第2页：目录页，列出所有章节
                3. 第3页及以后：每个章节1-2页，每页包含【标题】和【5-7个核心要点】
                4. 最后1页：总结页+致谢
                5. 每页用【第X页：标题】开头，要点用-开头
                6. 结构必须包含：封面页、课程介绍页、教学目标页、目录页、每章知识点详解页、案例分析页、重点总结页、课堂提问页、课堂练习页、总结复习页、参考文献页、致谢页。
                7.每页内容都要写详细一点、适合课堂讲课使用。
                """, input);

        return callAIStream(prompt, studentId);
    }

    // ==================== 3 练习题  ====================
    @PostMapping("/exercise")
    public String generateExercise(@RequestBody RequestDTO dto) {
        String prompt = String.format("""
                请为以下知识点生成一套配套练习题：
                知识点：%s
                要求：
                1. 包含选择题（10道）、填空题（5道）、简答题（3道）
                2. 每道题都要有详细的答案和解析
                3. 难度适中，覆盖核心知识点
                4. 格式清晰，便于学生练习
                """, dto.getInput());

        String result = callAI(prompt, dto.getStudentId());
        saveHistory("exercise", dto.getInput(), result);
        return result;
    }

    // ==================== 3 练习题（流式） ====================
    @GetMapping(value = "/exercise/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateExerciseStream(
            @RequestParam String input,
            @RequestParam(defaultValue = "default") String studentId) {
        String prompt = String.format("""
                请为以下知识点生成一套配套练习题：
                知识点：%s
                要求：
                1. 包含选择题（10道）、填空题（5道）、简答题（3道）
                2. 每道题都要有详细的答案和解析
                3. 难度适中，覆盖核心知识点
                4. 格式清晰，便于学生练习
                """, input);

        return callAIStream(prompt, studentId);
    }

    // ==================== 4 思维导图 ====================
    @PostMapping("/mindmap")
    public String generateMindMap(@RequestBody RequestDTO dto) {
        String prompt = String.format("""
                请为以下知识点生成一套专业的知识点思维导图结构：
                知识点：%s
                要求：
                1. 层级清晰、内容完整、适合大学生学习使用，
                2. 以知识点为根节点，层级不超过 4 层，使用缩进表示层级关系。
                """, dto.getInput());
        String result = callAI(prompt, dto.getStudentId());
        saveHistory("mindmap", dto.getInput(), result);
        return result;
    }

    // ==================== 4 思维导图（流式） ====================
    @GetMapping(value = "/mindmap/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateMindMapStream(
            @RequestParam String input,
            @RequestParam(defaultValue = "default") String studentId) {
        String prompt = String.format("""
                请为以下知识点生成一套专业的知识点思维导图结构：
                知识点：%s
                要求：
                1. 层级清晰、内容完整、适合大学生学习使用，
                2. 以知识点为根节点，层级不超过 4 层，使用缩进表示层级关系。
                """, input);

        return callAIStream(prompt, studentId);
    }

    // ==================== 5 复习提纲  ====================
    @PostMapping("/review")
    public String generateReview(@RequestBody RequestDTO dto) {
        String prompt = String.format("""
                请为以下知识点生成一份期末复习提纲：
                知识点：%s
                要求：
                1. 内容简洁清晰、重点突出，
                2. 适合学生快速复习，
                3. 提炼10至15个核心考点，
                4. 标注易混淆知识点和高频考点，并给出针对性的复习建议。
                """, dto.getInput());
        String result = callAI(prompt, dto.getStudentId());
        saveHistory("review", dto.getInput(), result);
        return result;
    }

    // ==================== 5 复习提纲（流式） ====================
    @GetMapping(value = "/review/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateReviewStream(
            @RequestParam String input,
            @RequestParam(defaultValue = "default") String studentId) {
        String prompt = String.format("""
                请为以下知识点生成一份期末复习提纲：
                知识点：%s
                要求：
                1. 内容简洁清晰、重点突出，
                2. 适合学生快速复习，
                3. 提炼10至15个核心考点，
                4. 标注易混淆知识点和高频考点，并给出针对性的复习建议。
                """, input);

        return callAIStream(prompt, studentId);
    }

    // ==================== 历史记录 ====================
    //按创建时间升序返回（最早的记录在最前面）
    @GetMapping("/history")
    public List<ResourceHistory> getHistory() {
        return historyRepository.findAll(Sort.by(Sort.Direction.ASC, "createTime"));
    }

    @DeleteMapping("/history/{id}")
    public void deleteHistory(@PathVariable Long id) {
        historyRepository.deleteById(id);
    }

    @Transactional
    @DeleteMapping("/history/clearAll")
    public ResponseEntity<String> clearAll() {
        try {
            entityManager.createNativeQuery("TRUNCATE TABLE resource_history").executeUpdate();
            return ResponseEntity.ok("清空成功");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("清空失败");
        }
    }

    private void saveHistory(String type, String input, String output) {
        ResourceHistory h = new ResourceHistory();
        h.setType(type);
        h.setInputText(input);
        h.setOutputText(output);
        historyRepository.save(h);
    }

    // ==================== PPT下载 ====================
    @PostMapping("/ppt/download")
    public ResponseEntity<byte[]> downloadPPT(@RequestBody RequestDTO dto) {
        try {
            String prompt = String.format("""
                    请为以下知识点生成一份完整的PPT课件大纲：
                    知识点：%s
                    格式：第1页...第2页...
                    """, dto.getInput());
            String pptContent = callAI(prompt, dto.getStudentId());

            XMLSlideShow ppt = new XMLSlideShow();
            ppt.setPageSize(new java.awt.Dimension(1280, 720));
            String[] pages = pptContent.split("第\\d+页：");

            for (int i = 1; i < pages.length; i++) {
                String page = pages[i].trim();
                if (page.isEmpty()) continue;
                XSLFSlide slide = ppt.createSlide();
                String[] lines = page.split("\n");
                String title = lines[0].trim();

                XSLFTextBox titleBox = slide.createTextBox();
                titleBox.setAnchor(new Rectangle(50, 50, 550, 100));
                XSLFTextRun tr = titleBox.addNewTextParagraph().addNewTextRun();
                tr.setText(title);
                tr.setFontSize(36d);
                tr.setBold(true);
                tr.setFontColor(new Color(0, 51, 102));

                XSLFTextBox contentBox = slide.createTextBox();
                contentBox.setAnchor(new Rectangle(80, 180, 550, 450));
                for (int j = 1; j < lines.length; j++) {
                    String line = lines[j].trim();
                    if (line.isEmpty()) continue;
                    XSLFTextRun r = contentBox.addNewTextParagraph().addNewTextRun();
                    r.setText(line);
                    r.setFontSize(24d);
                }

                byte[] img = generateFreeImage();
                if (img.length > 0) {
                    XSLFPictureData pd = ppt.addPicture(img, PictureData.PictureType.PNG);
                    XSLFPictureShape shape = slide.createPicture(pd);
                    shape.setAnchor(new Rectangle(680, 100, 550, 500));
                }
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ppt.write(bos);
            ppt.close();
            byte[] data = bos.toByteArray();
            bos.close();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment;filename=" + dto.getInput() + "_课件.pptx");
            return ResponseEntity.ok().headers(headers).body(data);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // ==================== WORD下载（支持5类） ====================
    @PostMapping("/download/word")
    public ResponseEntity<byte[]> downloadWord(@RequestBody RequestDTO dto) {
        try {
            String content = "";
            String name = "";

            if ("syllabus".equals(dto.getType())) {
                content = generateSyllabus(dto);
                name = dto.getInput() + "_教学大纲.docx";
            } else if ("ppt".equals(dto.getType())) {
                content = generatePPT(dto);
                name = dto.getInput() + "_PPT大纲.docx";
            } else if ("exercise".equals(dto.getType())) {
                content = generateExercise(dto);
                name = dto.getInput() + "_练习题.docx";
            } else if ("mindmap".equals(dto.getType())) {
                content = generateMindMap(dto);
                name = dto.getInput() + "_思维导图.docx";
            } else if ("review".equals(dto.getType())) {
                content = generateReview(dto);
                name = dto.getInput() + "_复习提纲.docx";
            }

            XWPFDocument doc = new XWPFDocument();
            for (String line : content.split("\n")) {
                if (line.isBlank()) continue;
                XWPFParagraph p = doc.createParagraph();
                XWPFRun run = p.createRun();
                run.setText(line);
                run.setFontSize(12);
                run.setFontFamily("宋体");
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            doc.write(bos);
            doc.close();
            byte[] data = bos.toByteArray();
            bos.close();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment;filename=" + new String(name.getBytes("UTF-8"), "ISO-8859-1"));
            return ResponseEntity.ok().headers(headers).body(data);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    public static class RequestDTO {
        private String input;
        private String type;
        private String studentId;

        public String getInput() {return input;}
        public void setInput(String input) {this.input = input;}
        public String getType() {return type;}
        public void setType(String type) {this.type = type;}
        public String getStudentId() {return studentId;}
        public void setStudentId(String studentId) {this.studentId = studentId;}
    }
}
