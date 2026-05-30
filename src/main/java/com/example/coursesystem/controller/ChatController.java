package com.example.coursesystem.controller;

import com.example.coursesystem.agent.*;
import com.example.coursesystem.entity.LearningHistory;
import com.example.coursesystem.repository.LearningHistoryRepository;
import com.example.coursesystem.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * 对话控制器 - 处理与学生的对话交互
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ProfileAgent profileAgent;

    @Autowired
    private ResourceAgent resourceAgent;

    @Autowired
    private PathAgent pathAgent;

    @Autowired
    private TutorAgent tutorAgent;

    @Autowired
    private EvalAgent evalAgent;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private LearningHistoryRepository historyRepository;

    /**
     * 流式对话接口
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(
            @RequestParam String message,
            @RequestParam(defaultValue = "default") String studentId,
            @RequestParam(defaultValue = "tutor") String agentType) {

        String context = "studentId:" + studentId + ";";

        // 保存学习历史
        saveHistory(studentId, "chat", message);

        BaseAgent agent = getAgent(agentType);
        return agent.processStream(message, context);
    }

    /**
     * 非流式对话接口
     */
    @PostMapping("/send")
    public String sendMessage(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String studentId = request.getOrDefault("studentId", "default");
        String agentType = request.getOrDefault("agentType", "tutor");

        String context = "studentId:" + studentId + ";";

        // 保存学习历史
        saveHistory(studentId, "chat", message);

        BaseAgent agent = getAgent(agentType);
        String response = agent.process(message, context);

        // 更新画像
        profileService.updateProfileFromChat(studentId, message);

        return response;
    }

    /**
     * 获取可用的智能体列表
     */
    @GetMapping("/agents")
    public Map<String, String> getAgents() {
        return Map.of(
                "profile", profileAgent.getAgentName() + " - " + profileAgent.getAgentDescription(),
                "resource", resourceAgent.getAgentName() + " - " + resourceAgent.getAgentDescription(),
                "path", pathAgent.getAgentName() + " - " + pathAgent.getAgentDescription(),
                "tutor", tutorAgent.getAgentName() + " - " + tutorAgent.getAgentDescription(),
                "eval", evalAgent.getAgentName() + " - " + evalAgent.getAgentDescription()
        );
    }

    private BaseAgent getAgent(String agentType) {
        return switch (agentType) {
            case "profile" -> profileAgent;
            case "resource" -> resourceAgent;
            case "path" -> pathAgent;
            case "eval" -> evalAgent;
            default -> tutorAgent;
        };
    }

    private void saveHistory(String studentId, String type, String content) {
        try {
            LearningHistory history = new LearningHistory();
            history.setStudentId(studentId);
            history.setType(type);
            history.setContent(content);
            historyRepository.save(history);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
