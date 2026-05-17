package com.wit.ai.service;

import com.wit.ai.domain.AiTask;
import com.wit.ai.domain.TaskStatus;
import com.wit.ai.domain.TaskType;
import com.wit.ai.dto.CharacterAssetGenerateRequest;
import com.wit.ai.dto.TaskResponse;
import com.wit.ai.repository.AiTaskRepository;
import com.wit.member.domain.Member;
import com.wit.model.domain.CharacterModel;
import com.wit.model.domain.ModelStatus;
import com.wit.model.repository.CharacterModelRepository;
import com.wit.project.domain.Project;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CharacterGenerationServiceTest {

    @Mock CharacterModelRepository characterModelRepository;
    @Mock AiTaskRepository aiTaskRepository;
    @Mock ComfyUIOrchestrator orchestrator;
    @InjectMocks CharacterGenerationService service;

    private Member owner;
    private Member intruder;
    private Project project;
    private CharacterModel model;
    private CharacterAssetGenerateRequest request;

    @BeforeEach
    void setUp() {
        owner = member(1L);
        intruder = member(2L);
        project = project(100L, owner);
        model = characterModel(200L, project);
        request = new CharacterAssetGenerateRequest("dynamic pose", "white background");
    }

    @Test
    void generate_returns_taskResponse_on_success() {
        when(characterModelRepository.findById(200L)).thenReturn(Optional.of(model));
        when(aiTaskRepository.save(any(AiTask.class))).thenAnswer(inv -> {
            AiTask t = inv.getArgument(0);
            ReflectionTestUtils.setField(t, "taskId", 999L);
            return t;
        });

        TaskResponse response = service.generate(owner, 100L, 200L, request);

        assertThat(response.getTaskId()).isEqualTo(999L);
        assertThat(response.getTaskType()).isEqualTo(TaskType.CHARACTER);
        assertThat(response.getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(response.getProgressPercent()).isEqualTo(0);
    }

    @Test
    void generate_invokes_orchestrator_with_taskId_and_modelId() {
        when(characterModelRepository.findById(200L)).thenReturn(Optional.of(model));
        when(aiTaskRepository.save(any(AiTask.class))).thenAnswer(inv -> {
            AiTask t = inv.getArgument(0);
            ReflectionTestUtils.setField(t, "taskId", 999L);
            return t;
        });

        service.generate(owner, 100L, 200L, request);

        verify(orchestrator, times(1))
                .processCharacterGeneration(eq(999L), eq(200L),
                        eq("dynamic pose"), eq("white background"));
    }

    @Test
    void generate_throws_when_model_not_found() {
        when(characterModelRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generate(owner, 100L, 999L, request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(orchestrator, never())
                .processCharacterGeneration(anyLong(), anyLong(), anyString(), anyString());
    }

    @Test
    void generate_throws_when_project_mismatch() {
        when(characterModelRepository.findById(200L)).thenReturn(Optional.of(model));

        assertThatThrownBy(() -> service.generate(owner, 999L, 200L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("매칭");
    }

    @Test
    void generate_throws_when_member_not_owner() {
        when(characterModelRepository.findById(200L)).thenReturn(Optional.of(model));

        assertThatThrownBy(() -> service.generate(intruder, 100L, 200L, request))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ===== helpers =====

    private Member member(Long id) {
        Member m = Member.builder().email("e" + id + "@t").password("p").nickname("n").build();
        ReflectionTestUtils.setField(m, "memberId", id);
        return m;
    }

    private Project project(Long id, Member owner) {
        Project p = Project.builder().member(owner).title("t").build();
        ReflectionTestUtils.setField(p, "projectId", id);
        return p;
    }

    private CharacterModel characterModel(Long id, Project project) {
        CharacterModel m = CharacterModel.builder()
                .project(project)
                .modelName("anya")
                .triggerWord("anya_v1")
                .status(ModelStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(m, "modelId", id);
        return m;
    }
}
