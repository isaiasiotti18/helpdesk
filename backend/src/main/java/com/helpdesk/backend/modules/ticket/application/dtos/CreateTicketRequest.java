package com.helpdesk.backend.modules.ticket.application.dtos;

import com.helpdesk.backend.modules.ticket.domain.Priority;
import jakarta.validation.constraints.NotBlank;

public record CreateTicketRequest(
		@NotBlank String title,
		String description,
		Priority priority,
		String categoryId) {
}
