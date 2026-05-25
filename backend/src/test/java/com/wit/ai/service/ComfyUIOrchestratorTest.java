package com.wit.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wit.ai.client.ComfyUIClient;
import com.wit.ai.client.ComfyUIClientException;
import com.wit.ai.client.ComfyUIResult;
import com.wit.ai.client.LlmException;
import com.wit.ai.config.ComfyUIProperties;
import com.wit.ai.domain.AiTask;
import com.wit.ai.domain.CharacterAsset;
import com.wit.ai.domain.TaskStatus;
import com.wit.ai.domain.TaskType;
import com.wit.ai.dto.AiPanelsGenerateRequest;
import com.wit.ai.dto.CharacterMention;
import com.wit.ai.dto.ComposedPrompt;
import com.wit.ai.dto.ScenarioPanel;
import com.wit.ai.repository.AiTaskRepository;
import com.wit.ai.repository.CharacterAssetRepository;
import com.wit.ai.storage.ImageStorage;
import com.wit.ai.storage.StoredImage;
import com.wit.ai.workflow.WorkflowParams;
import com.wit.ai.workflow.WorkflowTemplateLoader;
import com.wit.episode.domain.Episode;
import com.wit.episode.domain.Panel;
import com.wit.episode.domain.PanelStatus;
import com.wit.episode.repository.EpisodeRepository;
import com.wit.episode.repository.PanelRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
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
    @Mock EpisodeRepository episodeRepository;
    @Mock PanelRepository panelRepository;
    @Mock ScenarioAnalyzer scenarioAnalyzer;
    @Mock ObjectMapper objectMapper;
    @InjectMocks ComfyUIOrchestrator orchestrator;

    private Member owner;
    private Project project;
    private CharacterModel model;
    private Episode episode;
    private AiTask task;

    private static final Long EPISODE_ID = 300L;
    private static final Long TASK_ID = 999L;
    private static final Long MODEL_ID_1 = 200L;
    private static final Long MODEL_ID_2 = 201L;

    @BeforeEach
    void setUp() {
        owner = member(1L);
        project = project(100L, owner);
        model = characterModel(MODEL_ID_1, project);
        episode = episode(EPISODE_ID, project);
        task = aiTaskPending(TASK_ID, owner);
    }

    // ===== 2-10c: processCharacterGeneration (기존 5 케이스 보존) =====

    @Test
    void process_success_savesCharacterAsset_andMarksCompleted() {
        when(aiTaskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(characterModelRepository.findById(MODEL_ID_1)).thenReturn(Optional.of(model));
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

        orchestrator.processCharacterGeneration(TASK_ID, MODEL_ID_1,
                "dynamic pose", "white background");

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
        when(aiTaskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(characterModelRepository.findById(MODEL_ID_1)).thenReturn(Optional.of(model));
        when(promptComposer.compose(any(ScenarioPanel.class), any(CharacterMention.class)))
                .thenReturn(new ComposedPrompt("positive", "negative", 42L, "anya_v1"));
        when(workflowTemplateLoader.load(anyString(), any(WorkflowParams.class)))
                .thenReturn("{}");
        when(comfyUIClient.submitWorkflow("{}"))
                .thenThrow(new ComfyUIClientException("HTTP 500: bad"));

        orchestrator.processCharacterGeneration(TASK_ID, MODEL_ID_1, "pose", "bg");

        assertThat(task.getStatus()).isEqualTo(TaskStatus.FAILED);
        assertThat(task.getErrorMessage()).contains("[submit]").contains("HTTP 500");
    }

    @Test
    void process_pollTimeout_marksTaskFailed() {
        when(aiTaskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(characterModelRepository.findById(MODEL_ID_1)).thenReturn(Optional.of(model));
        when(promptComposer.compose(any(ScenarioPanel.class), any(CharacterMention.class)))
                .thenReturn(new ComposedPrompt("positive", "negative", 42L, "anya_v1"));
        when(workflowTemplateLoader.load(anyString(), any(WorkflowParams.class)))
                .thenReturn("{}");
        when(comfyUIClient.submitWorkflow(anyString())).thenReturn("prompt-123");
        when(comfyUIProperties.maxPollAttempts()).thenReturn(2);
        when(comfyUIProperties.pollIntervalMillis()).thenReturn(1);
        when(comfyUIClient.pollResult("prompt-123")).thenReturn(Optional.empty());

        orchestrator.processCharacterGeneration(TASK_ID, MODEL_ID_1, "pose", "bg");

        assertThat(task.getStatus()).isEqualTo(TaskStatus.FAILED);
        assertThat(task.getErrorMessage()).contains("[poll]").contains("timeout");
        verify(comfyUIClient, times(2)).pollResult("prompt-123");
    }

    @Test
    void process_downloadFailure_marksTaskFailed() {
        when(aiTaskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(characterModelRepository.findById(MODEL_ID_1)).thenReturn(Optional.of(model));
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

        orchestrator.processCharacterGeneration(TASK_ID, MODEL_ID_1, "pose", "bg");

        assertThat(task.getStatus()).isEqualTo(TaskStatus.FAILED);
        assertThat(task.getErrorMessage()).contains("[download]");
    }

    @Test
    void process_modelNotFound_marksTaskFailed() {
        when(aiTaskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(characterModelRepository.findById(MODEL_ID_1)).thenReturn(Optional.empty());

        orchestrator.processCharacterGeneration(TASK_ID, MODEL_ID_1, "pose", "bg");

        assertThat(task.getStatus()).isEqualTo(TaskStatus.FAILED);
        assertThat(task.getErrorMessage()).contains("[loadModel]");
    }

    // ===== 2-12b: processPanelGeneration (신규 5 케이스) =====

    @Test
    void processPanel_success_10_completed_markCompleted() throws JsonProcessingException {
        AiPanelsGenerateRequest request = panelsRequest(List.of(
                new CharacterMention("연우", MODEL_ID_1, "yeonwoo_v1"),
                new CharacterMention("지섭", MODEL_ID_2, "jiseop_v1")
        ));
        stubPanelHappyPath(request, modelIdsForPanels(MODEL_ID_1, MODEL_ID_2, null, null, null, null, null, null, null, null));
        CharacterModel model2 = characterModel(MODEL_ID_2, project);
        when(characterModelRepository.findById(MODEL_ID_1)).thenReturn(Optional.of(model));
        when(characterModelRepository.findById(MODEL_ID_2)).thenReturn(Optional.of(model2));

        orchestrator.processPanelGeneration(TASK_ID, EPISODE_ID, request);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(task.getProgressPercent()).isEqualTo(100);
        assertThat(task.getTargetType()).isEqualTo("Episode");
        assertThat(task.getTargetId()).isEqualTo(EPISODE_ID);
        // mention 있는 panel 2개 → CharacterAsset save 2번
        verify(characterAssetRepository, times(2)).save(any(CharacterAsset.class));
    }

    @Test
    void processPanel_episode_missing_marksFailed_loadEpisode() {
        when(aiTaskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(episodeRepository.findById(EPISODE_ID)).thenReturn(Optional.empty());

        AiPanelsGenerateRequest request = panelsRequest(List.of());
        orchestrator.processPanelGeneration(TASK_ID, EPISODE_ID, request);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.FAILED);
        assertThat(task.getErrorMessage()).contains("[loadEpisode]");
        verify(scenarioAnalyzer, never()).analyze(anyString(), any(), any());
    }

    @Test
    void processPanel_llm_failure_marksFailed_analyze() {
        when(aiTaskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(episodeRepository.findById(EPISODE_ID)).thenReturn(Optional.of(episode));
        when(scenarioAnalyzer.analyze(anyString(), any(), any()))
                .thenThrow(new LlmException("Gemini rate limit"));

        AiPanelsGenerateRequest request = panelsRequest(List.of());
        orchestrator.processPanelGeneration(TASK_ID, EPISODE_ID, request);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.FAILED);
        assertThat(task.getErrorMessage()).contains("[analyze]").contains("Gemini");
        verify(panelRepository, never()).saveAll(any());
    }

    @Test
    void processPanel_partial_failure_marksFailed_partial() throws JsonProcessingException {
        AiPanelsGenerateRequest request = panelsRequest(List.of(
                new CharacterMention("연우", MODEL_ID_1, "yeonwoo_v1")
        ));
        stubPanelHappyPath(request, modelIdsForPanels(MODEL_ID_1, MODEL_ID_1, MODEL_ID_1, MODEL_ID_1, MODEL_ID_1, MODEL_ID_1, MODEL_ID_1, MODEL_ID_1, MODEL_ID_1, MODEL_ID_1));
        when(characterModelRepository.findById(MODEL_ID_1)).thenReturn(Optional.of(model));

        // submitWorkflow가 panel 3, 7에서만 throw
        AtomicInteger submitCount = new AtomicInteger(0);
        when(comfyUIClient.submitWorkflow(anyString())).thenAnswer(inv -> {
            int n = submitCount.incrementAndGet();
            if (n == 3 || n == 7) {
                throw new ComfyUIClientException("submit failed at panel " + n);
            }
            return "prompt-" + n;
        });

        orchestrator.processPanelGeneration(TASK_ID, EPISODE_ID, request);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.FAILED);
        assertThat(task.getErrorMessage()).contains("[panels-partial-failed]")
                .contains("[3, 7]")
                .contains("2/10 failed");
        // 성공 8 panel → CharacterAsset 8번 save
        verify(characterAssetRepository, times(8)).save(any(CharacterAsset.class));
    }

    @Test
    void processPanel_characterModelId_null_skips_CharacterAsset() throws JsonProcessingException {
        AiPanelsGenerateRequest request = panelsRequest(List.of(
                new CharacterMention("연우", MODEL_ID_1, "yeonwoo_v1")
        ));
        // 10 panel 중 1개만 mention 있음 (modelId=MODEL_ID_1), 9개는 null
        stubPanelHappyPath(request, modelIdsForPanels(MODEL_ID_1, null, null, null, null, null, null, null, null, null));
        when(characterModelRepository.findById(MODEL_ID_1)).thenReturn(Optional.of(model));

        orchestrator.processPanelGeneration(TASK_ID, EPISODE_ID, request);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        // mention 있는 panel 1개 → CharacterAsset save 1번만
        verify(characterAssetRepository, times(1)).save(any(CharacterAsset.class));
        // mention 없는 panel은 CharacterModelRepository.findById 호출 안 됨
        verify(characterModelRepository, times(1)).findById(MODEL_ID_1);
    }

    // ===== panel happy-path stub 헬퍼 =====

    private void stubPanelHappyPath(AiPanelsGenerateRequest request, List<Long> modelIdsByPanel)
            throws JsonProcessingException {
        when(aiTaskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(episodeRepository.findById(EPISODE_ID)).thenReturn(Optional.of(episode));
        lenient().when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        List<ScenarioPanel> scenarioPanels = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            scenarioPanels.add(new ScenarioPanel(
                    i + 1, "panel " + (i + 1), modelIdsByPanel.get(i),
                    "action", "emotion", "pose", "bg", "camera"));
        }
        when(scenarioAnalyzer.analyze(anyString(), any(), any())).thenReturn(scenarioPanels);

        when(promptComposer.compose(any(ScenarioPanel.class), any()))
                .thenReturn(new ComposedPrompt("positive", "negative", 42L, "anya_v1"));
        when(workflowTemplateLoader.load(eq("character.json"), any(WorkflowParams.class)))
                .thenReturn("{}");
        lenient().when(comfyUIClient.submitWorkflow(anyString())).thenReturn("prompt-id");
        lenient().when(comfyUIProperties.maxPollAttempts()).thenReturn(3);
        lenient().when(comfyUIProperties.pollIntervalMillis()).thenReturn(1);
        when(comfyUIClient.pollResult(anyString()))
                .thenReturn(Optional.of(new ComfyUIResult("out.png", "")));
        when(comfyUIClient.downloadImage(anyString(), anyString()))
                .thenReturn(new byte[]{1, 2, 3});
        when(imageStorage.save(any(), eq("panel"), any()))
                .thenReturn(new StoredImage("placeholder/panel/x.png", "/images/panel/x.png"));
        when(characterAssetRepository.save(any(CharacterAsset.class))).thenAnswer(inv -> {
            CharacterAsset a = inv.getArgument(0);
            ReflectionTestUtils.setField(a, "assetId", 5000L);
            return a;
        });
        when(panelRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private List<Long> modelIdsForPanels(Long... ids) {
        return List.of(java.util.Arrays.stream(ids)
                .map(id -> id == null ? Long.valueOf(-1L) : id)
                .toArray(Long[]::new)).stream()
                .map(id -> id == -1L ? null : id)
                .toList();
    }

    private AiPanelsGenerateRequest panelsRequest(List<CharacterMention> mentions) {
        return new AiPanelsGenerateRequest("비 내리는 밤이었다 …", mentions);
    }

    // ===== entity helpers =====

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
                .modelName("anya" + id)
                .triggerWord("anya_v" + id)
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
