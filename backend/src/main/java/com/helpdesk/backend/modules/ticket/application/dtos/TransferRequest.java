package com.helpdesk.backend.modules.ticket.application.dtos;

import java.util.UUID;

public record TransferRequest(
        UUID agentId,
        UUID queueId,
        String reason
) {}
