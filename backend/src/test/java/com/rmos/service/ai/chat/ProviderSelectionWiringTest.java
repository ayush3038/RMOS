package com.rmos.service.ai.chat;

import com.rmos.config.ChatProperties;
import com.rmos.dto.chat.ChatProviderStatus;
import com.rmos.dto.chat.ChatStatusResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end provider-selection wiring, using the REAL Spring beans (no mocked
 * {@link ChatAssistantService}) and the same {@code rmos.ai.*} properties the
 * application uses in development.
 *
 * <p>These tests pin down exactly which provider {@code GET /api/v1/chat/status}
 * names, because that payload is what drives the UI provider indicator. They also
 * document the only configuration in which the UI may legitimately show
 * "Ollama Active".
 */
class ProviderSelectionWiringTest {

    @Nested
    @SpringBootTest(properties = {
            "rmos.ai.chat.provider=gemini",
            "rmos.ai.gemini.api-key=AIzaSyTestOnlyKey0000000000000000000000",
            "rmos.ai.nim.enabled=false",
            "rmos.rate-limit.enabled=false"
    })
    @ActiveProfiles("test")
    class GeminiKeyConfigured {

        @Autowired
        private ChatAssistantService chatService;

        @Autowired
        private GeminiChatProvider gemini;

        @Autowired
        private OllamaChatProvider ollama;

        @Test
        void geminiIsTheSelectedProvider() {
            ChatStatusResponse status = chatService.status();

            assertThat(status.isAvailable()).isTrue();
            assertThat(status.getProvider()).isEqualTo("gemini");
            assertThat(status.getModel()).isEqualTo("gemini-3.1-flash-lite");
        }

        @Test
        void ollamaIsNeverSelectedWhileGeminiIsConfigured() {
            ChatStatusResponse status = chatService.status();

            assertThat(status.getProvider()).isNotEqualTo("ollama");
            assertThat(status.getProviders())
                    .filteredOn(ChatProviderStatus::getType, "ollama")
                    .isEmpty();
        }

        @Test
        void geminiResolvesTheStableModelAndIsConfigured() {
            assertThat(gemini.isConfigured()).isTrue();
            assertThat(gemini.model()).isEqualTo("gemini-3.1-flash-lite");
        }

        @Test
        void ollamaStaysAvailableAsAnOptInLocalAlternative() {
            assertThat(ollama.type()).isEqualTo("ollama");
        }
    }

    @Nested
    @SpringBootTest(properties = {
            "rmos.ai.chat.provider=gemini",
            "rmos.ai.gemini.api-key=AIzaSyTestOnlyKey0000000000000000000000",
            "rmos.ai.nim.enabled=false",
            "rmos.rate-limit.enabled=false"
    })
    @ActiveProfiles("test")
    class InCodeFallbackChain {

        @Autowired
        private ChatAssistantService chatService;

        @Test
        void inCodeFallbackChainAlsoSelectsGeminiNotOllama() {
            /*
             * Guards the in-code default in ChatProperties. It used to be
             * "nim,ollama", so any context where rmos.ai.chat.provider was not
             * supplied by a YAML file silently selected Ollama as the first
             * configured provider, because NVIDIA NIM can never be configured
             * while account access is blocked.
             */
            assertThat(new ChatProperties().getProvider()).isEqualTo("gemini");

            assertThat(chatService.status().getProvider()).isEqualTo("gemini");
        }
    }

    @Nested
    @SpringBootTest(properties = {
            "rmos.ai.chat.provider=gemini,ollama",
            "rmos.ai.gemini.api-key=",
            "rmos.ai.nim.enabled=false",
            "rmos.rate-limit.enabled=false"
    })
    @ActiveProfiles("test")
    class GeminiKeyMissing {

        @Autowired
        private ChatAssistantService chatService;

        /**
         * Documents the exact conditions that make Ollama the active provider,
         * and therefore the only reason the UI would ever show "Ollama Active":
         * ollama must be in the chain AND Gemini must be unconfigured.
         */
        @Test
        void ollamaBecomesTheSelectedProvider() {
            ChatStatusResponse status = chatService.status();

            assertThat(status.getProvider()).isEqualTo("ollama");
            assertThat(status.getModel()).isEqualTo("llama3.2");
        }
    }

    @Nested
    @SpringBootTest(properties = {
            "rmos.ai.chat.provider=gemini,ollama",
            "rmos.ai.gemini.api-key=PASTE_YOUR_REAL_KEY_HERE",
            "rmos.ai.nim.enabled=false",
            "rmos.rate-limit.enabled=false"
    })
    @ActiveProfiles("test")
    class PlaceholderKey {

        @Autowired
        private ChatAssistantService chatService;

        @Test
        void placeholderKeyLeavesGeminiUnconfiguredSoOllamaTakesOver() {
            assertThat(chatService.status().getProvider()).isEqualTo("ollama");
        }
    }

    @Nested
    @SpringBootTest(properties = {
            "rmos.ai.chat.provider=gemini",
            "rmos.ai.gemini.api-key=PASTE_YOUR_REAL_KEY_HERE",
            "rmos.ai.nim.enabled=false",
            "rmos.rate-limit.enabled=false"
    })
    @ActiveProfiles("test")
    class PlaceholderKeyWithoutFallback {

        @Autowired
        private ChatAssistantService chatService;

        @Test
        void placeholderKeyIsReportedAsUnavailableRatherThanOllama() {
            ChatStatusResponse status = chatService.status();

            assertThat(status.isAvailable()).isFalse();
            assertThat(status.getProvider()).isNull();
            assertThat(status.getModel()).isNull();
        }
    }
}