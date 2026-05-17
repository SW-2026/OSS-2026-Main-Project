package com.wit.ai.controller;

import com.wit.ai.dto.TaskResponse;
import com.wit.ai.service.TaskService;
import com.wit.auth.dto.PrincipalDetails;
import com.wit.global.response.ApiResponse;
import com.wit.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/api/ai/tasks/{taskId}")
    public ApiResponse<TaskResponse> getTask(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long taskId
    ) {
        Member member = principalDetails.getMember();
        return ApiResponse.ok(taskService.getTaskStatus(member, taskId));
    }
}
