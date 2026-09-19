package com.rmos.service.knowledge;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class KnowledgeChunkingService {

    // Configurable but hardcoded for now, typical is 1000 characters with 200
    // overlap.
    private static final int CHUNK_SIZE = 1000;
    private static final int CHUNK_OVERLAP = 200;

    public List<String> chunkText(String content) {
        List<String> chunks = new ArrayList<>();
        if (content == null || content.trim().isEmpty()) {
            return chunks;
        }

        String[] paragraphs = content.split("\n\n");
        StringBuilder currentChunk = new StringBuilder();

        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            if (trimmed.isEmpty())
                continue;

            if (currentChunk.length() + trimmed.length() > CHUNK_SIZE) {
                if (!currentChunk.isEmpty()) {
                    chunks.add(currentChunk.toString().trim());
                    // Poor man's overlap logic based on keeping the last 200 chars if possible
                    String prevChunk = currentChunk.toString();
                    currentChunk = new StringBuilder();
                    if (prevChunk.length() > CHUNK_OVERLAP) {
                        currentChunk.append(prevChunk.substring(prevChunk.length() - CHUNK_OVERLAP)).append(" ");
                    } else {
                        currentChunk.append(prevChunk).append(" ");
                    }
                }

                // If a single paragraph is larger than chunk size, just add it (or could split
                // by sentences).
                // Doing basic add for now to prevent lost text.
                if (trimmed.length() > CHUNK_SIZE) {
                    chunks.add(trimmed);
                    currentChunk = new StringBuilder();
                    continue;
                }
            }

            currentChunk.append(trimmed).append("\n\n");
        }

        if (!currentChunk.isEmpty() && currentChunk.toString().trim().length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }
}
