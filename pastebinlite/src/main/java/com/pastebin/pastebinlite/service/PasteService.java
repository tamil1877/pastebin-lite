package com.pastebin.pastebinlite.service;

import com.pastebin.pastebinlite.entity.Paste;
import com.pastebin.pastebinlite.repository.PasteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service
public class PasteService {

    private final PasteRepository repo;

    public PasteService(PasteRepository repo) {
        this.repo = repo;
    }

    // ==============================
    // CREATE PASTE
    // ==============================
    public Paste createPaste(String content, Integer ttlSeconds, Integer maxViews) {

        // validation
        if (content == null || content.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content cannot be empty");
        }

        Paste paste = new Paste();
        paste.setId(UUID.randomUUID().toString());
        paste.setContent(content);
        paste.setCreatedAt(Instant.now());
        paste.setViewCount(0);
        paste.setMaxViews(maxViews);

        // TTL logic
        if (ttlSeconds != null && ttlSeconds > 0) {
            paste.setExpiresAt(Instant.now().plusSeconds(ttlSeconds));
        }

        return repo.save(paste);
    }

    // ==============================
    // FETCH PASTE (TTL + VIEW LIMIT)
    // ==============================
    public Paste fetchPaste(String id, Instant now) {

        Paste paste = repo.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Paste not found"));

        // Time expiry check
        if (paste.getExpiresAt() != null && now.isAfter(paste.getExpiresAt())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paste expired");
        }

        // View limit check
        if (paste.getMaxViews() != null &&
                paste.getViewCount() >= paste.getMaxViews()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "View limit exceeded");
        }

        // Increment view count
        paste.setViewCount(paste.getViewCount() + 1);

        return repo.save(paste);
    }
}
