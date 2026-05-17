package com.wit.ai.service;

import com.wit.ai.client.ComfyUIClient;
import com.wit.ai.client.ComfyUIClientException;
import com.wit.ai.client.ComfyUIResult;
import com.wit.ai.config.ComfyUIProperties;
import com.wit.ai.domain.AiTask;
import com.wit.ai.domain.CharacterAsset;
import com.wit.ai.domain.TaskStatus;
import com.wit.ai.domain.TaskType;
import com.wit.ai.dto.CharacterMention;
import com.wit.ai.dto.ComposedPrompt;
import com.wit.ai.dto.ScenarioPanel;
import com.wit.ai.repository.AiTaskRepository;
import com.wit.ai.repository.CharacterAssetRepository;
import com.wit.ai.storage.ImageStorage;
import com.wit.ai.storage.StoredImage;
import com.wit.ai.workflow.WorkflowParams;
import com.wit.ai.workflow.WorkflowTemplateLoader;
import com.wit.member.domain.Member;
import com.wit.model.domain.CharacterModel;
import com.wit.model.domain.ModelStatus;
import com.wit.model.repository.CharacterModelRepository;
import com.wit.project.domain.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComfyUIOrchestratorTest {

    @Mock CharacterModelRepository characterModelRepository;
    @Mock AiTaskRepository aiTaskRepository;
    @Mock CharacterAssetRepository characterAssetRepository;
    @Mock PromptComposer promptComposer;
    @Mock WorkflowTemplateLoader workflowTemplateLoader;
    @Mock ComfyUIClient comfyUIClient;
    @Mock ImageStorage imageStorage;
    @Mock ComfyUIProperties comfyUIProperties;
    @InjectMocks ComfyUIOrchestrator orchestrator;

    private Member owner;
    private Project project;
    private CharacterModel model;
    private AiTask task;

    @BeforeEach
    void setUp() {
        owner = member(1L);
        project = project(100L, owner);
        model = characterModel(200L, project);
        task = aiTaskPending(999L, owner);
    }

    @Test
    void process_success_savesCharacterAsset_andMarksCompleted() {
        when(aiTaskRepository.findById(999L)).thenReturn(Optional.of(task));
        when(characterModelRepository.findById(200L)).thenReturn(Optional.of(model));
        when(promptComposer.compose(any(ScenarioPanel.class), any(CharacterMention.class)))
                .thenReturn(new ComposedPrompt("positive", "negative", 42L, "anya_v1"));
        when(workflowTemplateLoader.load(eq("character.json"), any(WorkflowParams.class)))
                .thenReturn("{}");
        when(comfyUIClient.submitWorkflow("{}")).thenReturn("prompt-123");
        when(comfyUIProperties.maxPollAttempts()).thenReturn(3);
        when(comfyUIProperties.pollIntervalMillis()).thenReturn(1);
        when(comfyUIClient.pollResult("prompt-123"))
                .thenReturn(Optional.of(new ComfyUIResult("out.png", "")));
        when(comfyUIClient.downloadImage("out.png", "")).thenReturn(new byte[]{1, 2, 3});
        when(imageStorage.save(any(), eq("character"), any()))
                .thenReturn(new StoredImage("placeholder/character/abc.png",
                        "/images/character/abc.png"));
        when(characterAssetRepository.save(any(CharacterAsset.class))).thenAnswer(inv -> {
            CharacterAsset a = inv.getArgument(0);
            ReflectionTestUtils.setField(a, "assetId", 5000L);
            return a;
        });

        orchestrator.processCharacterGeneration(999L, 200L, "dynamic pose", "white background");

        ArgumentCaptor<CharacterAsset> assetCaptor = ArgumentCaptor.forClass(CharacterAsset.class);
        verify(characterAssetRepository).save(assetCaptor.capture());
        CharacterAsset savedAsset = assetCaptor.getValue();
        assertThat(savedAsset.getImageUrl()).isEqualTo("/images/character/abc.png");
        assertThat(savedAsset.getFinalPrompt()).isEqualTo("positive");
        assertThat(savedAsset.getSeed()).isEqualTo(42L);
        assertThat(savedAsset.getCharacterModel()).isSameAs(model);
        assertThat(savedAsset.getProject()).isSameAs(project);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(task.getProgressPercent()).isEqualTo(100);
        assertThat(task.getResultUrl()).isEqualTo("/images/character/abc.png");
        assertThat(task.getTargetType()).isEqualTo("CharacterAsset");
        assertThat(task.getTargetId()).isEqualTo(5000L);
    }

    @Test
    void process_submitFailure_marksTaskFailed() {
        when(aiTaskRepository.findById(999L)).thenReturn(Optional.of(task));
        when(characterModelRepository.findById(200L)).thenReturn(Optional.of(model));
        when(promptComposer.compose(any(ScenarioPanel.class), any(CharacterMention.class)))
                .thenReturn(new ComposedPrompt("positive", "negative", 42L, "anya_v1"));
        when(workflowTemplateLoader.load(anyString(), any(WorkflowParams.class)))
                .thenReturn("{}");
        when(comfyUIClient.submitWorkflow("{}"))
                .thenThrow(new ComfyUIClientException("HTTP 500: bad"));

        orchestrator.processCharacterGeneration(999L, 200L, "pose", "bg");

        assertThat(task.getStatus()).isEqualTo(TaskStatus.FAILED);
        assertThat(task.getErrorMessage()).contains("[submit]").contains("HTTP 500");
    }

    @Test
    void process_pollTimeout_marksTaskFailed() {
        when(aiTaskRepository.findById(999L)).thenReturn(Optional.of(task));
        when(characterModelRepository.findById(200L)).thenReturn(Optional.of(model));
        when(promptComposer.compose(any(ScenarioPanel.class), any(CharacterMention.class)))
                .thenReturn(new ComposedPrompt("positive", "negative", 42L, "anya_v1"));
        when(workflowTemplateLoader.load(anyString(), any(WorkflowParams.class)))
                .thenReturn("{}");
        when(comfyUIClient.submitWorkflow(anyString())).thenReturn("prompt-123");
        when(comfyUIProperties.maxPollAttempts()).thenReturn(2);
        when(comfyUIProperties.pollIntervalMillis()).thenReturn(1);
        when(comfyUIClient.pollResult("prompt-123")).thenReturn(Optional.empty());

        orchestrator.processCharacterGeneration(999L, 200L, "pose", "bg");

        assertThat(task.getStatus()).isEqualTo(TaskStatus.FAILED);
        assertThat(task.getErrorMessage()).contains("[poll]").contains("timeout");
        verify(comfyUIClient, times(2)).pollResult("prompt-123");
    }

    @Test
    void process_downloadFailure_marksTaskFailed() {
        when(aiTaskRepository.findById(999L)).thenReturn(Optional.of(task));
        when(characterModelRepository.findById(200L)).thenReturn(Optional.of(model));
        when(promptComposer.compose(any(ScenarioPanel.class), any(CharacterMention.class)))
                .thenReturn(new ComposedPrompt("positive", "negative", 42L, "anya_v1"));
        when(workflowTemplateLoader.load(anyString(), any(WorkflowParams.class)))
                .thenReturn("{}");
        when(comfyUIClient.submitWorkflow(anyString())).thenReturn("prompt-123");
        when(comfyUIProperties.maxPollAttempts()).thenReturn(3);
        when(comfyUIProperties.pollIntervalMillis()).thenReturn(1);
        when(comfyUIClient.pollResult("prompt-123"))
                .thenReturn(Optional.of(new ComfyUIResult("out.png", "")));
        when(comfyUIClient.downloadImage("out.png", ""))
                .thenThrow(new ComfyUIClientException("HTTP 404: not found"));

        orchestrator.processCharacterGeneration(999L, 200L, "pose", "bg");

        assertThat(task.getStatus()).isEqualTo(TaskStatus.FAILED);
        assertThat(task.getErrorMessage()).contains("[download]");
    }

    @Test
    void process_modelNotFound_marksTaskFailed() {
        when(aiTaskRepository.findById(999L)).thenReturn(Optional.of(task));
        when(characterModelRepository.findById(200L)).thenReturn(Optional.empty());

        orchestrator.processCharacterGeneration(999L, 200L, "pose", "bg");

        assertThat(task.getStatus()).isEqualTo(TaskStatus.FAILED);
        assertThat(task.getErrorMessage()).contains("[loadModel]");
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

    private AiTask aiTaskPending(Long id, Member member) {
        AiTask t = AiTask.builder()
                .member(member)
                .taskType(TaskType.CHARACTER)
                .status(TaskStatus.PENDING)
                .progressPercent(0)
                .build();
        ReflectionTestUtils.setField(t, "taskId", id);
        return t;
    }
}
