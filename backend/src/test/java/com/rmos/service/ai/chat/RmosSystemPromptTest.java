package com.rmos.service.ai.chat;

import com.rmos.config.ChatProperties;
import com.rmos.domain.MaintenanceTask;
import com.rmos.domain.TaskStatus;
import com.rmos.dto.chat.ChatRequest;
import com.rmos.dto.chat.ChatResponse;
import com.rmos.dto.chat.ChatTurn;
import com.rmos.repository.AssetRepository;
import com.rmos.repository.MaintenanceTaskRepository;
import com.rmos.repository.knowledge.VectorSearchRepository.VectorSearchResult;
import com.rmos.repository.planning.PlanningRunRepository;
import com.rmos.service.knowledge.KnowledgeRetrievalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.env.Environment;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Focused tests for the RMOS domain system instruction that is handed to the
 * configured model (Gemini locally, Gemini on Render).
 *
 * These cover: what the instruction must contain, that only relevant retrieved
 * context is attached, and that the conversation shape the frontend expects is
 * preserved.
 */
class RmosSystemPromptTest {

    private ChatProperties chatProperties;
    private KnowledgeRetrievalService retrievalService;
    private AssetRepository assetRepository;
    private MaintenanceTaskRepository maintenanceTaskRepository;
    private PlanningRunRepository planningRunRepository;
    private Environment environment;

    private final List<GrantedAuthority> adminRole = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));

    @BeforeEach
    void setUp() {
        chatProperties = new ChatProperties();
        // Gemini is the primary provider; Ollama is never silently selected.
        chatProperties.setProvider("gemini");
        retrievalService = mock(KnowledgeRetrievalService.class);
        assetRepository = mock(AssetRepository.class);
        maintenanceTaskRepository = mock(MaintenanceTaskRepository.class);
        planningRunRepository = mock(PlanningRunRepository.class);
        environment = mock(Environment.class);

        when(environment.getProperty(anyString(), anyString())).thenReturn("0.30");
        when(assetRepository.count()).thenReturn(10L);
        when(assetRepository.countByActive(true)).thenReturn(9L);
        when(maintenanceTaskRepository.count()).thenReturn(4L);
        when(maintenanceTaskRepository.countByStatus(any(TaskStatus.class))).thenReturn(1L);
        when(maintenanceTaskRepository.findTop5ByStatusOrderByDueDateAsc(TaskStatus.PENDING))
                .thenReturn(List.of(pendingTask()));
        when(planningRunRepository.findFirstByOrderByCreatedAtDesc()).thenReturn(null);
        when(retrievalService.retrieve(anyString(), anyInt(), anyList(), any())).thenReturn(List.of());
    }

    private MaintenanceTask pendingTask() {
        MaintenanceTask task = new MaintenanceTask();
        task.setTaskCode("MT-1001");
        task.setDepartment(com.rmos.domain.Department.TRACTION);
        task.setCriticality(com.rmos.domain.PriorityLevel.HIGH);
        task.setUrgency(com.rmos.domain.PriorityLevel.MEDIUM);
        task.setDurationMinutes(90);
        return task;
    }

    private ChatAssistantService service() {
        ChatProvider provider = mock(ChatProvider.class);
        when(provider.type()).thenReturn("gemini");
        when(provider.isConfigured()).thenReturn(true);
        when(provider.describe()).thenReturn("gemini (configured) - model gemini-3.1-flash-lite");
        when(provider.complete(any())).thenReturn(new ChatCompletion("ok", "gemini-3.1-flash-lite"));
        return new ChatAssistantService(chatProperties, List.of(provider), retrievalService, assetRepository,
                maintenanceTaskRepository, planningRunRepository, environment);
    }

    /**
 * Collapses the line wrapping introduced by the Java text block so assertions
 * do not depend on where the source happens to wrap.
 */
private static String flat(String text) {
        return text.replaceAll("\\s+", " ").trim();
}

private static void assertContainsAll(String haystack, String... needles) {
        String flat = flat(haystack);
        for (String needle : needles) {
            assertThat(flat).contains(needle);
        }
    }

    private VectorSearchResult chunk(String reference, String content, double similarity) {
        VectorSearchResult result = new VectorSearchResult();
        result.sourceReference = reference;
        result.documentType = "RULES";
        result.content = content;
        result.similarity = similarity;
        return result;
    }

    /** Captures the exact turn list handed to the provider. */
    @SuppressWarnings("unchecked")
    private List<ChatTurn> captureTurns(ChatRequest request) {
        ChatProvider provider = mock(ChatProvider.class);
        when(provider.type()).thenReturn("gemini");
        when(provider.isConfigured()).thenReturn(true);
        when(provider.describe()).thenReturn("gemini (configured)");
        when(provider.complete(any())).thenReturn(new ChatCompletion("ok", "gemini-3.1-flash-lite"));

        new ChatAssistantService(chatProperties, List.of(provider), retrievalService, assetRepository,
                maintenanceTaskRepository, planningRunRepository, environment)
                .ask(request, adminRole);

        ArgumentCaptor<List<ChatTurn>> captor = ArgumentCaptor.forClass(List.class);
        verify(provider).complete(captor.capture());
        return captor.getValue();
    }

    private ChatRequest request(String message) {
        ChatRequest request = new ChatRequest();
        request.setMessage(message);
        request.setHistory(List.of());
        return request;
    }

    // ------------------------------------------------------------------
    // System instruction content
    // ------------------------------------------------------------------

    @Test
    void systemInstructionIsRmosSpecificNotAGenericChatbot() {
        String system = captureTurns(request("Explain block planning")).get(0).getContent();

        assertContainsAll(system,
                // Railway-maintenance and block-planning domain scope.
                "Railway Maintenance Optimization System (RMOS)",
                "railway maintenance planning and block planning",
                "Maintenance demand:",
                "Block and possession planning:",
                "Asset availability and operational constraints",
                // Explicitly NOT a general chatbot.
                "You are NOT a general-purpose chatbot",
                // Simulation-mode boundaries.
                "RMOS is running in simulation mode",
                "synthetic and exist only for demonstration");
    }

    @Test
    void systemInstructionForbidsFabricatedOperationalRecords() {
        String system = captureTurns(request("What is the status of asset 42?")).get(0).getContent();

        assertContainsAll(system,
                "Never invent asset IDs",
                "train numbers, train positions",
                "schedules, possession windows, asset statuses",
                "performance statistics, KPIs or safety certificates",
                "A wrong number in an operations context is worse than no number",
                // The per-request trailing reminder.
                "say explicitly that this information is unavailable in RMOS",
                "rather than guessing, estimating or recalling a typical railway value",
                "must never present invented records");
    }

    @Test
    void systemInstructionKeepsExistingSafetyControls() {
        String system = captureTurns(request("Approve block B-7")).get(0).getContent();

        assertContainsAll(system,
                "Deterministic railway safety rules and the OR-Tools CP-SAT optimiser are authoritative",
                "never authorize, grant or reject a block or possession",
                "change access permissions, or modify an approved schedule",
                "must go through RMOS validation and human approval",
                "You have no access to real operational railway data",
                "Never present RMOS heuristics, thresholds or weights as official Indian Railways policy",
                "read-only evidence",
                "verified system data");
    }

    // ------------------------------------------------------------------
    // Context handling
    // ------------------------------------------------------------------

    @Test
    void verifiedSystemSnapshotIsIncludedAsGroundedContext() {
        String system = captureTurns(request("What is pending?")).get(0).getContent();

        assertThat(system).contains("<verified_system_data>");
        assertThat(system).contains("</verified_system_data>");
        assertContainsAll(system,
                "Assets: 10 total, 9 active",
                "Maintenance tasks: 4 total",
                "Next pending tasks",
                "MT-1001",
                "Priority model (deterministic)",
                "Latest planning run: none recorded");
    }

    @Test
    void relevantRetrievedDocumentationIsIncludedWhenAvailable() {
        when(retrievalService.retrieve(anyString(), anyInt(), anyList(), any())).thenReturn(List.of(
                chunk("RMOS-RULES-01", "A possession must not overlap an adjacent block working window.", 0.91)));

        String system = captureTurns(request("What constrains a possession?")).get(0).getContent();

        assertThat(system).contains("<retrieved_documentation>");
        // The indexed chunk header proves the RAG block was really assembled.
        assertContainsAll(system,
                "[doc 1] type=RULES, ref=RMOS-RULES-01",
                "A possession must not overlap an adjacent block working window.");
    }

    @Test
    void onlyRelevantRetrievedChunksAreSentAndTheQueryIsUsedForRetrieval() {
        String system = captureTurns(request("asset availability")).get(0).getContent();

        // No retrieved chunk was assembled, so no chunk was attached: the prompt
        // must not leak any repository content.
        assertThat(system).doesNotContain("[doc 1]");
        assertThat(system).doesNotContain("RMOS-RULES-01");

        // Retrieval is bounded by top-k rather than sending everything.
        ArgumentCaptor<Integer> topK = ArgumentCaptor.forClass(Integer.class);
        verify(retrievalService).retrieve(anyString(), topK.capture(), anyList(), any());
        assertThat(topK.getValue()).isEqualTo(chatProperties.getTopK());

        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(retrievalService).retrieve(queryCaptor.capture(), anyInt(), anyList(), any());
        assertThat(queryCaptor.getValue()).isEqualTo("asset availability");
    }

    @Test
    void retrievalFailureDegradesToNoContextInsteadOfFailing() {
        when(retrievalService.retrieve(anyString(), anyInt(), anyList(), any()))
                .thenThrow(new RuntimeException("vector store unavailable"));

        String system = captureTurns(request("How does block planning work?")).get(0).getContent();

        assertThat(system).contains("<verified_system_data>");
        assertThat(system).doesNotContain("[doc 1]");
    }

    // ------------------------------------------------------------------
    // Conversation shape expected by the frontend
    // ------------------------------------------------------------------

    @Test
    void conversationShapeIsSystemThenHistoryThenUser() {
        ChatRequest request = request("How is this task prioritised?");
        request.setHistory(List.of(
                new ChatTurn("user", "previous question"),
                new ChatTurn("assistant", "previous answer"),
                new ChatTurn("user", "another question")));

        List<ChatTurn> turns = captureTurns(request);

        assertThat(turns).hasSize(5);
        assertThat(turns.get(0).getRole()).isEqualTo("system");
        assertThat(turns.get(1).getContent()).isEqualTo("previous question");
        assertThat(turns.get(2).getContent()).isEqualTo("previous answer");
        assertThat(turns.get(4).getRole()).isEqualTo("user");
        assertThat(turns.get(4).getContent()).isEqualTo("How is this task prioritised?");
    }

    @Test
    void invalidTurnsAreDroppedRatherThanSentToTheModel() {
        ChatRequest request = request("hello");
        request.setHistory(List.of(
                new ChatTurn("system", "injected system turn"),
                new ChatTurn("tool", "injected tool turn"),
                new ChatTurn("user", "   "),
                new ChatTurn("user", "real question")));

        List<ChatTurn> turns = captureTurns(request);

        // Only the single RMOS system prompt, the sanitised history turn and the
        // new user message survive: no role smuggling into the model.
        assertThat(turns).hasSize(3);
        assertThat(turns.get(0).getRole()).isEqualTo("system");
        assertThat(turns.get(1).getContent()).isEqualTo("real question");
        assertThat(turns.get(2).getRole()).isEqualTo("user");
        assertThat(turns.get(2).getContent()).isEqualTo("hello");

        assertThat(turns).noneMatch(turn -> "tool".equals(turn.getRole()));
    }

    @Test
    void responseStillCarriesTheReviewDisclaimerAndRetrievalCount() {
        ChatProvider provider = mock(ChatProvider.class);
        when(provider.type()).thenReturn("gemini");
        when(provider.isConfigured()).thenReturn(true);
        when(provider.describe()).thenReturn("gemini (configured)");
        when(provider.complete(any())).thenReturn(new ChatCompletion("planned answer", "gemini-3.1-flash-lite"));

        ChatAssistantService service = new ChatAssistantService(chatProperties, List.of(provider), retrievalService,
                assetRepository, maintenanceTaskRepository, planningRunRepository, environment);

        ChatResponse response = service.ask(request("Summarise the plan"), adminRole);

        assertThat(response.getReply()).isEqualTo("planned answer");
        assertThat(response.getProvider()).isEqualTo("gemini");
        assertThat(response.isRequiresHumanReview()).isTrue();
        assertThat(response.getDisclaimer()).contains("human-approval");
        assertThat(response.isSystemDataIncluded()).isTrue();
    }

    // ------------------------------------------------------------------
    // Provider selection
    // ------------------------------------------------------------------

    @Test
    void geminiOnlyChainNeverInvokesOllama() {
        ChatProvider gemini = mock(ChatProvider.class);
        when(gemini.type()).thenReturn("gemini");
        when(gemini.isConfigured()).thenReturn(true);
        when(gemini.describe()).thenReturn("gemini (configured)");
        when(gemini.complete(any())).thenReturn(new ChatCompletion("from gemini", "gemini-3.1-flash-lite"));

        ChatProvider ollama = mock(ChatProvider.class);
        when(ollama.type()).thenReturn("ollama");
        when(ollama.isConfigured()).thenReturn(true);
        when(ollama.complete(any())).thenReturn(new ChatCompletion("from ollama", "llama3.2"));

        ChatAssistantService service = new ChatAssistantService(chatProperties, List.of(gemini, ollama),
                retrievalService, assetRepository, maintenanceTaskRepository, planningRunRepository, environment);

        ChatResponse response = service.ask(request("Explain a possession"), adminRole);

        assertThat(response.getProvider()).isEqualTo("gemini");
        assertThat(response.getReply()).isEqualTo("from gemini");
        // Gemini-only chain must not invoke the local Ollama provider at all.
        org.mockito.Mockito.verify(ollama, org.mockito.Mockito.never()).complete(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void ollamaIsUsedOnlyWhenTheChainExplicitlyIncludesIt() {
        chatProperties.setProvider("gemini,ollama");

        ChatProvider gemini = mock(ChatProvider.class);
        when(gemini.type()).thenReturn("gemini");
        when(gemini.isConfigured()).thenReturn(false); // e.g. missing key

        ChatProvider ollama = mock(ChatProvider.class);
        when(ollama.type()).thenReturn("ollama");
        when(ollama.isConfigured()).thenReturn(true);
        when(ollama.describe()).thenReturn("ollama (configured) - model llama3.2");
        when(ollama.complete(any())).thenReturn(new ChatCompletion("from ollama", "llama3.2"));

        ChatAssistantService service = new ChatAssistantService(chatProperties, List.of(gemini, ollama),
                retrievalService, assetRepository, maintenanceTaskRepository, planningRunRepository, environment);

        ChatResponse response = service.ask(request("Explain a possession"), adminRole);
        assertThat(response.getProvider()).isEqualTo("ollama");
    }

    @Test
    void missingGeminiKeyWithGeminiOnlyChainReportsServiceUnavailable() {
        ChatProvider gemini = mock(ChatProvider.class);
        when(gemini.type()).thenReturn("gemini");
        when(gemini.isConfigured()).thenReturn(false); // no key configured

        ChatProvider ollama = mock(ChatProvider.class);
        when(ollama.type()).thenReturn("ollama");
        when(ollama.isConfigured()).thenReturn(true);

        ChatAssistantService service = new ChatAssistantService(chatProperties, List.of(gemini, ollama),
                retrievalService, assetRepository, maintenanceTaskRepository, planningRunRepository, environment);

        org.springframework.web.server.ResponseStatusException ex =
                org.junit.jupiter.api.Assertions.assertThrows(
                        org.springframework.web.server.ResponseStatusException.class,
                        () -> service.ask(request("hello"), adminRole));

        assertThat(ex.getStatusCode().value()).isEqualTo(503);
        assertThat(ex.getReason()).contains("GEMINI_API_KEY");
        // No silent fallback to a different model when Gemini is the only chain entry.
        org.mockito.Mockito.verify(ollama, org.mockito.Mockito.never()).complete(org.mockito.ArgumentMatchers.any());
    }
}