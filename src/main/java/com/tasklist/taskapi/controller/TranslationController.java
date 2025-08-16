package com.tasklist.taskapi.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@RestController
@RequestMapping("/api/translations")
public class TranslationController {

    @GetMapping("/{lang}")
    public ResponseEntity<Map<String, Object>> getTranslations(@PathVariable String lang) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = new ClassPathResource("i18n/" + lang + ".json").getInputStream();
        Map<String, Object> translations = mapper.readValue(is, new TypeReference<>() {});
        return ResponseEntity.ok(translations);
    }
}
