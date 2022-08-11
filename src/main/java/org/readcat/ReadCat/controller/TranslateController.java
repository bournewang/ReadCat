package org.readcat.ReadCat.controller;

import com.baidu.translate.demo.TransApi;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Array;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequestMapping("/api")
@RestController
public class TranslateController {
    @Autowired
    private Environment env;

    @GetMapping("/trans")
    public String translate(@RequestParam(value="query", defaultValue = "") String query){
        TransApi api = new TransApi(
                env.getProperty("baidu.translate_api.app_id"),
                env.getProperty("baidu.translate_api.app_key"));
        String res = api.getTransResult(query, "en", "zh");
        JSONObject json = (JSONObject)JSONValue.parse(res);
        JSONArray result = (JSONArray) json.get("trans_result");
        JSONObject first = (JSONObject) result.get(0);
        String translate = (String) first.get("dst");
        System.out.println(translate);
        return translate;
    }

    @GetMapping("/examples")
    public ResponseEntity<Object> examples(@RequestParam(value="word", defaultValue = "") String word,
                           HttpServletResponse response) throws FileNotFoundException {
        JSONObject res = new JSONObject();
        if (word.isEmpty()) {
            throw new FileNotFoundException("word is required!");
        }
        String baseDir = "upload/example/en/";
        File directories = new File(baseDir);
        if (!directories.exists()) {
            directories.mkdirs();
            System.out.println("words directory created!");
        }
        File file = new File(baseDir +"/" + word + ".html");

        ResponseEntity<Object> responseEntity = null;
        if (!file.exists()) {
            String url = env.getProperty("examples_baseurl") + "%s.html".formatted(word);
            Document doc = null;
            System.out.println("fetch url: "+url);
            try {
                doc = Jsoup.connect(url).get();
                System.out.println("get doc");
                Element examples = doc.getElementById("student");
                examples.attr("style", "");
                System.out.println("get student element: "+examples.attr("id"));
                String html = examples.outerHtml();
                System.out.println("html: "+html);
                FileOutputStream output = new FileOutputStream(file);
                output.write(html.getBytes());
                output.close();
                System.out.println("write to file: "+file.getAbsolutePath());

                responseEntity = ResponseEntity.ok().
                        contentType(MediaType.parseMediaType(MediaType.TEXT_HTML_VALUE)).
                        contentLength(html.length()).
                        body(html);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
            responseEntity = ResponseEntity.ok().
                    contentType(MediaType.parseMediaType(MediaType.TEXT_HTML_VALUE)).
                    contentLength(file.length()).
                    body(resource);
        }

        return responseEntity;
    }
}
