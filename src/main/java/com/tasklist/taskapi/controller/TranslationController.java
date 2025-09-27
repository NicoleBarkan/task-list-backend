package com.tasklist.taskapi.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.Map;

@RestController
@RequestMapping("/api/translations")
public class TranslationController {

    private final ObjectMapper mapper = new ObjectMapper();

    @GetMapping(value = "/{lang}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getTranslations(@PathVariable String lang) {
        // fallback на en, если файла нет
        String path = "i18n/" + lang + ".json";
        ClassPathResource res = new ClassPathResource(path);
        if (!res.exists()) {
            res = new ClassPathResource("i18n/en.json");
            if (!res.exists()) return ResponseEntity.notFound().build();
        }

        try (InputStream is = res.getInputStream()) {
            Map<String, Object> translations = mapper.readValue(is, new TypeReference<>() {});
            return ResponseEntity.ok(translations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
