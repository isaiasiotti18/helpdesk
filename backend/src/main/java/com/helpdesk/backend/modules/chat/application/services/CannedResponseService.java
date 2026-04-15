package com.helpdesk.backend.modules.chat.application.services;

import com.helpdesk.backend.modules.chat.application.dto.CannedResponseDto;
import com.helpdesk.backend.modules.chat.application.dto.CreateCannedRequest;
import com.helpdesk.backend.modules.chat.application.dto.UpdateCannedRequest;
import com.helpdesk.backend.modules.chat.domain.CannedResponse;
import com.helpdesk.backend.modules.chat.domain.CannedResponseRepository;
import com.helpdesk.backend.modules.user.domain.User;
import com.helpdesk.backend.modules.user.domain.UserRepository;
import com.helpdesk.backend.shared.exception.BusinessException;
import com.helpdesk.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CannedResponseService {

    private final CannedResponseRepository cannedRepository;
    private final UserRepository userRepository;

    @Transactional
    public CannedResponseDto create(CreateCannedRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        CannedResponse canned = CannedResponse.builder()
                .title(request.title())
                .content(request.content())
                .shortcut(request.shortcut())
                .category(request.category())
                .createdBy(user)
                .isShared(request.isShared() != null ? request.isShared() : false)
                .build();

        return CannedResponseDto.from(cannedRepository.save(canned));
    }

    @Transactional(readOnly = true)
    public List<CannedResponseDto> listAccessible(UUID userId) {
        return cannedRepository.findAccessibleByUser(userId).stream()
                .map(CannedResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CannedResponseDto> search(UUID userId, String query) {
        return cannedRepository.search(userId, query).stream()
                .map(CannedResponseDto::from)
                .toList();
    }

    @Transactional
    public CannedResponseDto update(UUID id, UpdateCannedRequest request, UUID userId) {
        CannedResponse canned = cannedRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CannedResponse", id));

        if (!canned.getCreatedBy().getId().equals(userId)) {
            throw new BusinessException("Only the creator can edit this response", HttpStatus.FORBIDDEN);
        }

        if (request.title() != null) canned.setTitle(request.title());
        if (request.content() != null) canned.setContent(request.content());
        if (request.shortcut() != null) canned.setShortcut(request.shortcut());
        if (request.category() != null) canned.setCategory(request.category());
        if (request.isShared() != null) canned.setIsShared(request.isShared());

        return CannedResponseDto.from(cannedRepository.save(canned));
    }

    @Transactional
    public void delete(UUID id, UUID userId) {
        CannedResponse canned = cannedRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CannedResponse", id));

        if (!canned.getCreatedBy().getId().equals(userId)) {
            throw new BusinessException("Only the creator can delete this response", HttpStatus.FORBIDDEN);
        }

        cannedRepository.delete(canned);
    }
}
