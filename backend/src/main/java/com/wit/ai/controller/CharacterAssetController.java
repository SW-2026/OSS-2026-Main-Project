package com.wit.ai.controller;

import com.wit.ai.dto.CharacterAssetGenerateRequest;
import com.wit.ai.dto.TaskResponse;
import com.wit.ai.service.CharacterGenerationService;
import com.wit.auth.dto.PrincipalDetails;
import com.wit.global.response.ApiResponse;
import com.wit.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CharacterAssetController {

    private final CharacterGenerationService characterGenerationService;

    @PostMapping("/api/projects/{projectId}/character-models/{modelId}/character-assets")
    public ResponseEntity<ApiResponse<TaskResponse>> generate(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long projectId,
            @PathVariable Long modelId,
            @RequestBody CharacterAssetGenerateRequest request
    ) {
        Member member = principalDetails.getMember();
        TaskResponse response =
                characterGenerationService.generate(member, projectId, modelId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.ok(response));
    }
}
