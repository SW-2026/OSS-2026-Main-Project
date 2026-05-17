package com.wit.ai.service;

import com.wit.ai.domain.AiTask;
import com.wit.ai.domain.TaskStatus;
import com.wit.ai.domain.TaskType;
import com.wit.ai.dto.AiPanelsGenerateRequest;
import com.wit.ai.dto.CharacterMention;
import com.wit.ai.dto.TaskResponse;
import com.wit.ai.repository.AiTaskRepository;
import com.wit.episode.domain.Episode;
import com.wit.episode.repository.EpisodeRepository;
import com.wit.member.domain.Member;
import com.wit.model.domain.CharacterModel;
import com.wit.model.domain.ModelStatus;
import com.wit.model.repository.CharacterModelRepository;
import com.wit.project.domain.Project;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PanelGenerationServiceTest {

    @Mock EpisodeRepository episodeRepository;
    @Mock CharacterModelRepository characterModelRepository;
    @Mock AiTaskRepository aiTaskRepository;
    @Mock ComfyUIOrchestrator orchestrator;
    @InjectMocks PanelGenerationService service;

    private Member owner;
    private Member intruder;
    private Project project;
    private Project otherProject;
    private Episode episode;
    private CharacterModel model1;
    private CharacterModel model2;
    private CharacterModel modelInOtherProject;
    private AiPanelsGenerateRequest request;

    private static final Long EPISODE_ID = 300L;

    @BeforeEach
    void setUp() {
        owner = member(1L);
        intruder = member(2L);
        project = project(100L, owner);
        otherProject = project(101L, owner);
        episode = episode(EPISODE_ID, project);
        model1 = characterModel(200L, project);
        model2 = characterModel(201L, project);
        modelInOtherProject = characterModel(202L, otherProject);
        request = new AiPanelsGenerateRequest(
                "비 내리는 밤이었다. @연우는 골목길을 달리고 있었다.",
                List.of(
                        new CharacterMention("연우", 200L, "yeonwoo_v1"),
                        new CharacterMention("지섭", 201L, "jiseop_v1")
                )
        );
    }

    @Test
    void generate_returns_taskResponse_when_owner_and_mentions_valid() {
        when(episodeRepository.findById(EPISODE_ID)).thenReturn(Optional.of(episode));
        when(characterModelRepository.findAllById(any())).thenReturn(List.of(model1, model2));
        when(aiTaskRepository.save(any(AiTask.class))).thenAnswer(inv -> {
            AiTask t = inv.getArgument(0);
            ReflectionTestUtils.setField(t, "taskId", 999L);
            return t;
        });

        TaskResponse response = service.generate(owner, EPISODE_ID, request);

        assertThat(response.getTaskId()).isEqualTo(999L);
        assertThat(response.getTaskType()).isEqualTo(TaskType.PANELS);
        assertThat(response.getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(response.getProgressPercent()).isEqualTo(0);
        assertThat(response.getTargetType()).isEqualTo("Episode");
        assertThat(response.getTargetId()).isEqualTo(EPISODE_ID);

        ArgumentCaptor<AiTask> taskCaptor = ArgumentCaptor.forClass(AiTask.class);
        verify(aiTaskRepository, times(1)).save(taskCaptor.capture());
        AiTask saved = taskCaptor.getValue();
        assertThat(saved.getMember()).isSameAs(owner);
        assertThat(saved.getTaskType()).isEqualTo(TaskType.PANELS);
        assertThat(saved.getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(saved.getProgressPercent()).isEqualTo(0);
        assertThat(saved.getTargetType()).isEqualTo("Episode");
        assertThat(saved.getTargetId()).isEqualTo(EPISODE_ID);
    }

    @Test
    void generate_invokes_orchestrator_with_taskId_and_episodeId() {
        when(episodeRepository.findById(EPISODE_ID)).thenReturn(Optional.of(episode));
        when(characterModelRepository.findAllById(any())).thenReturn(List.of(model1, model2));
        when(aiTaskRepository.save(any(AiTask.class))).thenAnswer(inv -> {
            AiTask t = inv.getArgument(0);
            ReflectionTestUtils.setField(t, "taskId", 999L);
            return t;
        });

        service.generate(owner, EPISODE_ID, request);

        verify(orchestrator, times(1))
                .processPanelGeneration(eq(999L), eq(EPISODE_ID), eq(request));
    }

    @Test
    void generate_throws_when_episode_not_found() {
        when(episodeRepository.findById(EPISODE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generate(owner, EPISODE_ID, request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(orchestrator, never())
                .processPanelGeneration(anyLong(), anyLong(), any());
    }

    @Test
    void generate_throws_AccessDeniedException_when_not_owner() {
        when(episodeRepository.findById(EPISODE_ID)).thenReturn(Optional.of(episode));

        assertThatThrownBy(() -> service.generate(intruder, EPISODE_ID, request))
                .isInstanceOf(AccessDeniedException.class);

        verify(orchestrator, never())
                .processPanelGeneration(anyLong(), anyLong(), any());
    }

    @Test
    void generate_throws_when_mention_modelId_in_other_project() {
        AiPanelsGenerateRequest crossProjectRequest = new AiPanelsGenerateRequest(
                "story",
                List.of(new CharacterMention("외부", 202L, "other_v1"))
        );
        when(episodeRepository.findById(EPISODE_ID)).thenReturn(Optional.of(episode));
        when(characterModelRepository.findAllById(any())).thenReturn(List.of(modelInOtherProject));

        assertThatThrownBy(() -> service.generate(owner, EPISODE_ID, crossProjectRequest))
                .isInstanceOf(AccessDeniedException.class);

        verify(orchestrator, never())
                .processPanelGeneration(anyLong(), anyLong(), any());
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

    private Episode episode(Long id, Project project) {
        Episode e = Episode.builder().project(project).epNumber(1).epTitle("ep").content("c").build();
        ReflectionTestUtils.setField(e, "episodeId", id);
        return e;
    }

    private CharacterModel characterModel(Long id, Project project) {
        CharacterModel m = CharacterModel.builder()
                .project(project)
                .modelName("name" + id)
                .triggerWord("trig_" + id)
                .status(ModelStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(m, "modelId", id);
        return m;
    }
}
