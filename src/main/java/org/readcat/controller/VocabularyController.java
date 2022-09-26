package org.readcat.controller;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@RestController
@RequestMapping("/api")
public class VocabularyController {

    @GetMapping("/words-list")
    public ResponseEntity<Object> list(@RequestParam(value="list", defaultValue = "") String list) throws FileNotFoundException {
        File file = new File("src/main/resources/static/%s.json".formatted(list));
        System.out.println("file path: "+file.getAbsolutePath());
        ResponseEntity<Object> responseEntity = null;
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        responseEntity = ResponseEntity.ok().
                contentType(MediaType.parseMediaType(MediaType.APPLICATION_JSON_VALUE)).
                contentLength(file.length()).
                body(resource);

        return responseEntity;
    }
}
