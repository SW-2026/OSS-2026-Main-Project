package com.wit.ai.service;

import com.wit.ai.domain.AiTask;
import com.wit.ai.domain.TaskStatus;
import com.wit.ai.domain.TaskType;
import com.wit.ai.dto.TaskResponse;
import com.wit.ai.repository.AiTaskRepository;
import com.wit.member.domain.Member;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock AiTaskRepository aiTaskRepository;
    @InjectMocks TaskService service;

    private Member owner;
    private Member intruder;
    private AiTask task;

    @BeforeEach
    void setUp() {
        owner = member(1L);
        intruder = member(2L);
        task = AiTask.builder()
                .member(owner)
                .taskType(TaskType.CHARACTER)
                .status(TaskStatus.PROCESSING)
                .progressPercent(50)
                .build();
        ReflectionTestUtils.setField(task, "taskId", 999L);
    }

    @Test
    void getTaskStatus_returns_response_when_owner_requests() {
        when(aiTaskRepository.findById(999L)).thenReturn(Optional.of(task));

        TaskResponse response = service.getTaskStatus(owner, 999L);

        assertThat(response.getTaskId()).isEqualTo(999L);
        assertThat(response.getStatus()).isEqualTo(TaskStatus.PROCESSING);
        assertThat(response.getProgressPercent()).isEqualTo(50);
    }

    @Test
    void getTaskStatus_throws_EntityNotFoundException_when_task_not_found() {
        when(aiTaskRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTaskStatus(owner, 404L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("404");
    }

    @Test
    void getTaskStatus_throws_AccessDeniedException_when_member_not_owner() {
        when(aiTaskRepository.findById(999L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> service.getTaskStatus(intruder, 999L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getTaskStatus_includes_full_task_fields_for_completed() {
        AiTask completed = AiTask.builder()
                .member(owner)
                .taskType(TaskType.CHARACTER)
                .status(TaskStatus.PENDING)
                .progressPercent(0)
                .build();
        ReflectionTestUtils.setField(completed, "taskId", 1000L);
        completed.markCompleted("CharacterAsset", 5000L, "/images/character/abc.png");
        when(aiTaskRepository.findById(1000L)).thenReturn(Optional.of(completed));

        TaskResponse response = service.getTaskStatus(owner, 1000L);

        assertThat(response.getTaskType()).isEqualTo(TaskType.CHARACTER);
        assertThat(response.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(response.getProgressPercent()).isEqualTo(100);
        assertThat(response.getTargetType()).isEqualTo("CharacterAsset");
        assertThat(response.getTargetId()).isEqualTo(5000L);
        assertThat(response.getResultUrl()).isEqualTo("/images/character/abc.png");
        assertThat(response.getErrorMessage()).isNull();
    }

    private Member member(Long id) {
        Member m = Member.builder().email("e" + id + "@t").password("p").nickname("n").build();
        ReflectionTestUtils.setField(m, "memberId", id);
        return m;
    }
}
