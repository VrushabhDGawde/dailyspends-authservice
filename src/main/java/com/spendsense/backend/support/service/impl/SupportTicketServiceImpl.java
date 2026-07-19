package com.spendsense.backend.support.service.impl;

import com.spendsense.backend.auth.entity.AppUser;
import com.spendsense.backend.auth.repository.AppUserRepository;
import com.spendsense.backend.common.exception.UserNotFoundException;
import com.spendsense.backend.support.dto.CreateTicketRequest;
import com.spendsense.backend.support.dto.SupportTicketDTO;
import com.spendsense.backend.support.dto.UpdateTicketStatusRequest;
import com.spendsense.backend.support.entity.SupportTicket;
import com.spendsense.backend.support.repository.SupportTicketRepository;
import com.spendsense.backend.support.service.SupportTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupportTicketServiceImpl implements SupportTicketService {

    private final SupportTicketRepository ticketRepository;
    private final AppUserRepository userRepository;

    @Override
    @Transactional
    public SupportTicketDTO createTicket(String email, CreateTicketRequest request) {
        if ("guest@dailyspends.com".equals(email)) {
            throw new RuntimeException("Your session has expired or you are not logged in. Please log in to raise a ticket.");
        }

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        Long userId = user.getId();
        String userName = user.getFullName();
        String userEmail = user.getEmail();

        String ticketNum = "TCK-" + (1000 + new Random().nextInt(9000));

        SupportTicket ticket = SupportTicket.builder()
                .ticketNumber(ticketNum)
                .userId(userId)
                .userName(userName)
                .userEmail(userEmail)
                .subject(request.getSubject())
                .category(request.getCategory())
                .priority(request.getPriority() != null ? request.getPriority() : "MEDIUM")
                .status("OPEN")
                .description(request.getDescription())
                .build();

        SupportTicket saved = ticketRepository.save(ticket);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketDTO> getUserTickets(String email) {
        AppUser user = userRepository.findByEmail(email).orElse(null);
        if (user == null || "guest@dailyspends.com".equals(email)) {
            // Guests cannot fetch tickets securely
            return List.of();
        }

        return ticketRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketDTO> getAllTickets() {
        return ticketRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SupportTicketDTO updateTicketStatus(Long ticketId, UpdateTicketStatusRequest request) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            ticket.setStatus(request.getStatus().toUpperCase());
        }
        if (request.getAdminNotes() != null) {
            ticket.setAdminNotes(request.getAdminNotes());
        }

        SupportTicket updated = ticketRepository.save(ticket);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public SupportTicketDTO updateUserTicket(Long ticketId, String email, com.spendsense.backend.support.dto.UpdateTicketRequest request) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        ticket.setSubject(request.getSubject());
        ticket.setCategory(request.getCategory());
        if (request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
        }
        ticket.setDescription(request.getDescription());

        SupportTicket updated = ticketRepository.save(ticket);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteUserTicket(Long ticketId, String email) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));
        ticketRepository.delete(ticket);
    }

    private SupportTicketDTO mapToDTO(SupportTicket entity) {
        return SupportTicketDTO.builder()
                .id(entity.getId())
                .ticketNumber(entity.getTicketNumber())
                .userId(entity.getUserId())
                .userName(entity.getUserName())
                .userEmail(entity.getUserEmail())
                .subject(entity.getSubject())
                .category(entity.getCategory())
                .priority(entity.getPriority())
                .status(entity.getStatus())
                .description(entity.getDescription())
                .adminNotes(entity.getAdminNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
