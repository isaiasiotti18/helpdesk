package com.helpdesk.backend.modules.ticket.application.services;

import com.helpdesk.backend.modules.ticket.application.dtos.RatingRequest;
import com.helpdesk.backend.modules.ticket.application.dtos.RatingResponse;
import com.helpdesk.backend.modules.ticket.domain.Ticket;
import com.helpdesk.backend.modules.ticket.domain.TicketRating;
import com.helpdesk.backend.modules.ticket.domain.TicketRatingRepository;
import com.helpdesk.backend.modules.ticket.domain.TicketStatus;
import com.helpdesk.backend.modules.ticket.domain.repositories.TicketRepository;
import com.helpdesk.backend.modules.user.domain.User;
import com.helpdesk.backend.modules.user.domain.UserRepository;
import com.helpdesk.backend.shared.exception.BusinessException;
import com.helpdesk.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final TicketRatingRepository ratingRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Transactional
    public RatingResponse rate(UUID ticketId, RatingRequest request, UUID userId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));

        if (ticket.getStatus() != TicketStatus.CLOSED && ticket.getStatus() != TicketStatus.RESOLVED) {
            throw new BusinessException("Ticket must be closed or resolved to rate", HttpStatus.BAD_REQUEST);
        }

        if (!ticket.getCreatedBy().getId().equals(userId)) {
            throw new BusinessException("Only the ticket creator can rate", HttpStatus.FORBIDDEN);
        }

        if (ratingRepository.existsByTicketId(ticketId)) {
            throw new BusinessException("Ticket already rated", HttpStatus.CONFLICT);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        TicketRating rating = TicketRating.builder()
                .ticket(ticket)
                .user(user)
                .score(request.score())
                .comment(request.comment())
                .build();

        return RatingResponse.from(ratingRepository.save(rating));
    }

    @Transactional(readOnly = true)
    public RatingResponse getByTicketId(UUID ticketId) {
        TicketRating rating = ratingRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating for ticket", ticketId));
        return RatingResponse.from(rating);
    }

    @Transactional(readOnly = true)
    public Double getAverageScore() {
        return ratingRepository.findAverageScore();
    }
}
