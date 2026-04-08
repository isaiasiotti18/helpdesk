package com.helpdesk.backend.modules.chat.application.services;

import com.helpdesk.backend.modules.chat.domain.Message;
import com.helpdesk.backend.modules.chat.domain.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncMessagePersistence {

    private final MessageRepository messageRepository;
    private final ConcurrentLinkedQueue<Message> buffer = new ConcurrentLinkedQueue<>();

    public void persist(Message message) {
        buffer.add(message);
    }

    @Scheduled(fixedDelay = 200)
    @Transactional
    public void flush() {
        if (buffer.isEmpty())
            return;

        List<Message> batch = new ArrayList<>();

        Message msg;
        while ((msg = buffer.poll()) != null && batch.size() < 50) {
            batch.add(msg);
        }

        if (!batch.isEmpty()) {
            try {
                messageRepository.saveAll(batch);
                log.debug("Flushed {} messages", batch.size());
            } catch (Exception e) {
                log.error("Failed to flush {} messages: {}", batch.size(), e.getMessage());
            }
        }
    }
}
