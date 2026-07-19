package com.spendsense.backend.support.service;

import com.spendsense.backend.support.dto.CreateTicketRequest;
import com.spendsense.backend.support.dto.SupportTicketDTO;
import com.spendsense.backend.support.dto.UpdateTicketRequest;
import com.spendsense.backend.support.dto.UpdateTicketStatusRequest;

import java.util.List;

public interface SupportTicketService {
    SupportTicketDTO createTicket(String email, CreateTicketRequest request);
    List<SupportTicketDTO> getUserTickets(String email);
    List<SupportTicketDTO> getAllTickets();
    SupportTicketDTO updateTicketStatus(Long ticketId, UpdateTicketStatusRequest request);
    SupportTicketDTO updateUserTicket(Long ticketId, String email, UpdateTicketRequest request);
    void deleteUserTicket(Long ticketId, String email);
}
