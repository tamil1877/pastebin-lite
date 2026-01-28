package com.pastebin.pastebinlite.controller;

import com.pastebin.pastebinlite.entity.Paste;
import com.pastebin.pastebinlite.service.PasteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class PasteController {

    private final PasteService service;

    // Constructor Injection
    public PasteController(PasteService service) {
        this.service = service;
    }

    // ==========================
    // 1️⃣ HEALTH CHECK API
    // ==========================
    @GetMapping("/healthz")
    public Map<String, Boolean> healthCheck() {
        return Map.of("ok", true);
    }

    // ==========================
    // 2️⃣ CREATE PASTE API
    // ==========================
    @PostMapping("/pastes")
    public Map<String, String> createPaste(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {

        String content = (String) body.get("content");

        Integer ttlSeconds = body.get("ttl_seconds") != null
                ? ((Number) body.get("ttl_seconds")).intValue()
                : null;

        Integer maxViews = body.get("max_views") != null
                ? ((Number) body.get("max_views")).intValue()
                : null;

        Paste paste = service.createPaste(content, ttlSeconds, maxViews);

        String url = request.getScheme() + "://" +
                request.getServerName() + ":" +
                request.getServerPort() +
                "/api/p/" + paste.getId();   // ⚠️ NOTE THIS PATH

        return Map.of(
                "id", paste.getId(),
                "url", url
        );
    }

    // ==========================
    // 3️⃣ FETCH PASTE (JSON API)
    // ==========================
    @GetMapping("/pastes/{id}")
    public Map<String, Object> fetchPaste(
            @PathVariable String id,
            @RequestHeader(value = "x-test-now-ms", required = false) Long testNow) {

        Instant now = (testNow != null)
                ? Instant.ofEpochMilli(testNow)
                : Instant.now();

        Paste paste = service.fetchPaste(id, now);

        Integer remainingViews = paste.getMaxViews() == null
                ? null
                : paste.getMaxViews() - paste.getViewCount();

        return Map.of(
                "content", paste.getContent(),
                "remaining_views", remainingViews,
                "expires_at", paste.getExpiresAt()
        );
    }

    // ==========================
    // 4️⃣ HTML VIEW (PASTEBIN LINK)
    // ==========================
    @GetMapping("/p/{id}")
    public ModelAndView viewPasteHtml(@PathVariable String id) {

        Paste paste = service.fetchPaste(id, Instant.now());

        ModelAndView mv = new ModelAndView("paste");
        mv.addObject("content", paste.getContent());
        mv.addObject("expiresAt", paste.getExpiresAt());
        mv.addObject(
                "remainingViews",
                paste.getMaxViews() == null
                        ? "Unlimited"
                        : paste.getMaxViews() - paste.getViewCount()
        );

        return mv;
    }
}
