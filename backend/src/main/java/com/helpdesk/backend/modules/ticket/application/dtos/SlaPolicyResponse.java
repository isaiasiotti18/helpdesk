package com.helpdesk.backend.modules.ticket.application.dtos;

import com.helpdesk.backend.modules.ticket.domain.SlaPolicy;

public record SlaPolicyResponse(
        String id,
        String name,
        String priority,
        Integer firstResponseMinutes,
        Integer resolutionMinutes,
        Boolean isActive
) {
    public static SlaPolicyResponse from(SlaPolicy p) {
        return new SlaPolicyResponse(
                p.getId().toString(),
                p.getName(),
                p.getPriority().name(),
                p.getFirstResponseMinutes(),
                p.getResolutionMinutes(),
                p.getIsActive()
        );
    }
}
