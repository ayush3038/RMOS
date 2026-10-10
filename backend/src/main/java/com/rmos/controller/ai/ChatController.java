package com.rmos.controller.ai;

import com.rmos.dto.chat.ChatRequest;
import com.rmos.dto.chat.ChatResponse;
import com.rmos.dto.chat.ChatStatusResponse;
import com.rmos.service.ai.chat.ChatAssistantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RMOS AI assistant chat endpoints.
 *
 * Both routes are protected by the stateless JWT filter (catch-all
 * authenticated) and by role-based method security. The assistant is
 * read-only: it cannot modify schedules, blocks or permissions.
 */
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private static final String AUTHORIZED_ROLES = "hasAnyRole('VIEWER', 'PLANNER', 'REVIEWER', 'ADMIN')";

    private final ChatAssistantService chatService;

    public ChatController(ChatAssistantService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/messages")
    @PreAuthorize(AUTHORIZED_ROLES)
    public ResponseEntity<ChatResponse> messages(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatService.ask(request,
                SecurityContextHolder.getContext().getAuthentication().getAuthorities()));
    }

    @GetMapping("/status")
    @PreAuthorize(AUTHORIZED_ROLES)
    public ResponseEntity<ChatStatusResponse> status() {
        return ResponseEntity.ok(chatService.status());
    }
}
