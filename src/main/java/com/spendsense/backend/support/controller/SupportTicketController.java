package com.spendsense.backend.support.controller;

import com.spendsense.backend.support.dto.CreateTicketRequest;
import com.spendsense.backend.support.dto.SupportTicketDTO;
import com.spendsense.backend.support.dto.UpdateTicketStatusRequest;
import com.spendsense.backend.support.service.SupportTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    // User Endpoints
    @PostMapping("/support/tickets")
    public ResponseEntity<SupportTicketDTO> createTicket(
            Authentication authentication,
            @Valid @RequestBody CreateTicketRequest request) {
        String email = (authentication != null && authentication.isAuthenticated()) ? authentication.getName() : "guest@dailyspends.com";
        return ResponseEntity.ok(supportTicketService.createTicket(email, request));
    }

    @GetMapping("/support/tickets/my")
    public ResponseEntity<List<SupportTicketDTO>> getMyTickets(Authentication authentication) {
        String email = (authentication != null && authentication.isAuthenticated()) ? authentication.getName() : "guest@dailyspends.com";
        return ResponseEntity.ok(supportTicketService.getUserTickets(email));
    }

    @PutMapping("/user/support/tickets/{id}")
    public ResponseEntity<SupportTicketDTO> updateTicket(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody com.spendsense.backend.support.dto.UpdateTicketRequest request) {
        String email = (authentication != null && authentication.isAuthenticated()) ? authentication.getName() : "guest@dailyspends.com";
        return ResponseEntity.ok(supportTicketService.updateUserTicket(id, email, request));
    }

    @DeleteMapping("/user/support/tickets/{id}")
    public ResponseEntity<Void> deleteTicket(
            Authentication authentication,
            @PathVariable Long id) {
        String email = (authentication != null && authentication.isAuthenticated()) ? authentication.getName() : "guest@dailyspends.com";
        supportTicketService.deleteUserTicket(id, email);
        return ResponseEntity.noContent().build();
    }

    // Admin Endpoints
    @GetMapping("/admin/support/tickets")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<SupportTicketDTO>> getAllTickets() {
        return ResponseEntity.ok(supportTicketService.getAllTickets());
    }

    @PutMapping("/admin/support/tickets/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SupportTicketDTO> updateTicketStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketStatusRequest request) {
        return ResponseEntity.ok(supportTicketService.updateTicketStatus(id, request));
    }
}
