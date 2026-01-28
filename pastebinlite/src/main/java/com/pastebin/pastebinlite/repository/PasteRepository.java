package com.pastebin.pastebinlite.repository;

import com.pastebin.pastebinlite.entity.Paste;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasteRepository extends JpaRepository<Paste, String> {
}
