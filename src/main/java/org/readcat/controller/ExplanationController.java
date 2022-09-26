package org.readcat.controller;

import org.readcat.utils.Explanation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RequestMapping("/api")
@RestController
public class ExplanationController {
    @Autowired
    Environment env;

    @GetMapping("/explanation")
    public HashMap<String, List<String>> index(@RequestParam(value="word", defaultValue = "") String word,
                                         @RequestParam(value="langs", defaultValue = "en") String langs
    ) throws IOException {
        List<String> langsList = List.of(langs.split(","));
        return (new Explanation(env)).getDictAndExamples(word, langsList);

    }

    @GetMapping("/explanation/{word}")
    public String parsedict(@PathVariable String word) throws IOException {
        return (new Explanation(env)).parseDict(word, "en");
    }
}
