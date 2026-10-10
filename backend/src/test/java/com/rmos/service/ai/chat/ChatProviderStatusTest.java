package com.rmos.service.ai.chat;

import com.rmos.config.ChatProperties;
import com.rmos.dto.chat.ChatProviderStatus;
import com.rmos.dto.chat.ChatStatusResponse;
import com.rmos.repository.AssetRepository;
import com.rmos.repository.MaintenanceTaskRepository;
import com.rmos.repository.planning.PlanningRunRepository;
import com.rmos.service.knowledge.KnowledgeRetrievalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Provider indicator tests for {@code GET /api/v1/chat/status}.
 *
 * The UI badge is driven entirely by this payload, so these tests pin down that
 * it always names the provider that will actually serve a request, reports the
 * resolved model, and claims nothing at all when no provider is configured.
 */
class ChatProviderStatusTest {

    private ChatProperties chatProperties;
    private KnowledgeRetrievalService retrievalService;
    private AssetRepository assetRepository;
    private MaintenanceTaskRepository maintenanceTaskRepository;
    private PlanningRunRepository planningRunRepository;
    private Environment environment;

    @BeforeEach
    void setUp() {
        chatProperties = new ChatProperties();
        retrievalService = mock(KnowledgeRetrievalService.class);
        assetRepository = mock(AssetRepository.class);
        maintenanceTaskRepository = mock(MaintenanceTaskRepository.class);
        planningRunRepository = mock(PlanningRunRepository.class);
        environment = mock(Environment.class);

        when(environment.getProperty(anyString(), anyString())).thenReturn("");
        when(retrievalService.retrieve(anyString(), anyInt(), anyList(), any())).thenReturn(List.of());
    }

    private ChatAssistantService service(ChatProvider... providers) {
        return new ChatAssistantService(chatProperties, List.of(providers), retrievalService, assetRepository,
                maintenanceTaskRepository, planningRunRepository, environment);
    }

    /** Minimal stub so the status logic can be exercised without real providers. */
    private ChatProvider provider(String type, String model, boolean configured) {
        ChatProvider provider = mock(ChatProvider.class);
        when(provider.type()).thenReturn(type);
        when(provider.model()).thenReturn(model);
        when(provider.isConfigured()).thenReturn(configured);
        when(provider.describe())
                .thenReturn(type + (configured ? " (configured) - model " + model + " at http://host" : " (not configured)"));
        return provider;
    }

    // ------------------------------------------------------------------
    // Gemini
    // ------------------------------------------------------------------

    @Test
    void status_reportsGeminiWithItsConfiguredModel() {
        chatProperties.setProvider("gemini");
        ChatStatusResponse response = service(provider("gemini", "gemini-3.1-flash-lite", true)).status();

        assertThat(response.isAvailable()).isTrue();
        assertThat(response.getProvider()).isEqualTo("gemini");
        assertThat(response.getModel()).isEqualTo("gemini-3.1-flash-lite");
        assertThat(response.getMessage()).contains("ready via gemini");
        assertThat(response.getProviders())
                .extracting(ChatProviderStatus::getType)
                .containsExactly("gemini");
    }

    @Test
    void status_reportsGeminiAheadOfOllamaWhenBothAreConfigured() {
        chatProperties.setProvider("gemini,ollama");

        ChatStatusResponse response = service(
                provider("gemini", "gemini-3.1-flash-lite", true),
                provider("ollama", "llama3.2", true)).status();

        assertThat(response.getProvider()).isEqualTo("gemini");
        assertThat(response.getModel()).isEqualTo("gemini-3.1-flash-lite");
        // Both are still listed so the UI can show the full configuration.
        assertThat(response.getProviders())
                .extracting(ChatProviderStatus::getType)
                .containsExactly("gemini", "ollama");
    }

    @Test
    void status_fallsThroughToOllamaOnlyWhenGeminiIsNotConfigured() {
        chatProperties.setProvider("gemini,ollama");

        ChatStatusResponse response = service(
                provider("gemini", "gemini-3.1-flash-lite", false),
                provider("ollama", "llama3.2", true)).status();

        assertThat(response.isAvailable()).isTrue();
        assertThat(response.getProvider()).isEqualTo("ollama");
        assertThat(response.getModel()).isEqualTo("llama3.2");
    }

    // ------------------------------------------------------------------
    // Ollama
    // ------------------------------------------------------------------

    @Test
    void status_reportsOllamaWhenItIsTheOnlyProvider() {
        chatProperties.setProvider("ollama");

        ChatStatusResponse response = service(provider("ollama", "llama3.2", true)).status();

        assertThat(response.isAvailable()).isTrue();
        assertThat(response.getProvider()).isEqualTo("ollama");
        assertThat(response.getModel()).isEqualTo("llama3.2");
        assertThat(response.getMessage()).contains("ready via ollama");
    }

    // ------------------------------------------------------------------
    // Unavailable
    // ------------------------------------------------------------------

    @Test
    void status_claimsNothingWhenNoProviderIsConfigured() {
        chatProperties.setProvider("gemini");

        ChatStatusResponse response = service(
                provider("gemini", "gemini-3.1-flash-lite", false),
                provider("ollama", "llama3.2", false)).status();

        assertThat(response.isAvailable()).isFalse();
        // Critically: no provider or model may be reported, so the UI cannot
        // render "Gemini Active"/"Ollama Active" when nothing is configured.
        assertThat(response.getProvider()).isNull();
        assertThat(response.getModel()).isNull();
        assertThat(response.getMessage()).contains("No AI chat provider is configured");
        assertThat(response.getSetupInstructions()).isNotEmpty();
    }

    @Test
    void status_claimsNothingWhenTheChainIsEmpty() {
        chatProperties.setProvider("none");

        ChatStatusResponse response = service(provider("gemini", "gemini-3.1-flash-lite", true)).status();

        assertThat(response.isAvailable()).isFalse();
        assertThat(response.getProvider()).isNull();
        assertThat(response.getModel()).isNull();
    }

    @Test
    void status_reportsUnknownProviderInTheChainWithoutSelectingIt() {
        chatProperties.setProvider("gemini,not-a-provider");

        ChatStatusResponse response = service(provider("gemini", "gemini-3.1-flash-lite", true)).status();

        assertThat(response.getProvider()).isEqualTo("gemini");
        assertThat(response.getProviders()).hasSize(2);
        assertThat(response.getProviders().get(1).getType()).isEqualTo("not-a-provider");
        assertThat(response.getProviders().get(1).isConfigured()).isFalse();
    }

    // ------------------------------------------------------------------
    // No secrets in the payload
    // ------------------------------------------------------------------

    @Test
    void status_payloadNeverContainsProviderCredentials() {
        chatProperties.setProvider("gemini");
        ChatStatusResponse response = service(provider("gemini", "gemini-3.1-flash-lite", true)).status();

        String serialised = String.valueOf(response.getProviders()) + response.getProvider() + response.getModel()
                + response.getMessage() + response.getSetupInstructions();

        assertThat(serialised).doesNotContain("apiKey");
        assertThat(serialised).doesNotContain("api-key");
        assertThat(serialised).doesNotContain("x-goog-api-key");
        assertThat(serialised).doesNotContain("AIza");
        // The base URL is not a secret, but keys/tokens never are.
        assertThat(serialised.toLowerCase()).doesNotContain("bearer ");
    }
}