package org.readcat.controller;

import com.baidu.translate.demo.TransApi;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.readcat.utils.Translate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RequestMapping("/api")
@RestController
public class TranslateController {
    @Autowired
    private Environment env;

    @GetMapping("/trans")
    public String translate(@RequestParam(value="query", defaultValue = "") String query) throws IOException {
        Translate translate = new Translate(env);
        return translate.translateText(query);
    }

    @GetMapping("/dict")
    public ResponseEntity<Object> dict(@RequestParam(value = "word", defaultValue = "") String word) throws Exception {
//        JSONObject res = new JSONObject();
        if (word.isEmpty()) {
            throw new FileNotFoundException("word is required!");
        }
        String dictDir = "upload/dict/en/";
        File dictDir1 = new File(dictDir);
        if (!dictDir1.exists()) {
            dictDir1.mkdirs();
            System.out.println("words directory created!");
        }
        File dictFile = new File(dictDir +"/" + word + ".html");

        ResponseEntity<Object> responseEntity = null;
        if (!dictFile.exists()) {

            try {
                WebClient webClient = WebClient.create();
                String res = webClient.get()
                        .uri(env.getProperty("dict_baseurl") + "/" + word)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block()
                        .toString()
                        ;
                System.out.println(res);
                String archiveDir = "upload/archive/";
                File archiveDir1 = new File(archiveDir);
                if (!archiveDir1.exists()) {
                    archiveDir1.mkdirs();
                    System.out.println("archiveDir directory created!");
                }
                String archivePath = archiveDir + "/" + word + ".json";
                File archiveFile = new File(archivePath);
                FileOutputStream arch = new FileOutputStream(archiveFile);
                arch.write(res.getBytes());
                arch.close();

                String html = this.praseDict(res);
                FileOutputStream output = new FileOutputStream(dictFile);
                output.write(html.getBytes());
                output.close();

                responseEntity = ResponseEntity.ok().
                        contentType(MediaType.parseMediaType(MediaType.TEXT_HTML_VALUE)).
                        contentLength(html.length()).
                        body(html);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            InputStreamResource resource = new InputStreamResource(new FileInputStream(dictFile));
            responseEntity = ResponseEntity.ok().
                    contentType(MediaType.parseMediaType(MediaType.TEXT_HTML_VALUE)).
                    contentLength(dictFile.length()).
                    body(resource);
        }

        return responseEntity;
    }

    private String praseDict(String text)
    {
        JSONArray items = (JSONArray) JSONValue.parse(text);
        String html = "<div class='dict'>";
        for (int i=0; i<items.size(); i++) {
            JSONObject exp = (JSONObject) items.get(i);
            html += "<div class='meanings'>";
            String phonetic = (String) exp.get("phonetic");
            if (!StringUtils.isEmpty(phonetic)) {
                html += "<div class='phonetics'>" + phonetic + "</div>";
            }
            JSONArray meanings = (JSONArray) exp.get("meanings");
            for (int j=0; j<meanings.size(); j++) {
                JSONObject meaning = (JSONObject) meanings.get(j);
                html += "<div class='meaning'>";
                String partOfSpeech = (String) meaning.get("partOfSpeech");
                if (!StringUtils.isEmpty(partOfSpeech)) {
                    html += "<span class='partOfSpeech'>" + partOfSpeech + "</span><br/>";
                }
                JSONArray definitions = (JSONArray) meaning.get("definitions");
                Integer def_length = definitions.size();
                for (Integer k=0; k<definitions.size(); k++) {
                    JSONObject definition = (JSONObject) definitions.get(k);
                    Integer k1 = k+1;
                    html += "<div class='definition'>" + (def_length < 2 ? "" : k1.toString() + ". ") +
                            (String) definition.get("definition") + "</div>";
                    String example = (String) definition.get("example");
                    if (!StringUtils.isEmpty(example)) {
                        html += "<div class='example'>" + example + "</div>";
                    }
                }
                JSONArray synonyms = (JSONArray) meaning.get("synonyms");
                if (synonyms.size()>0)
                    html += "<div class='synonyms'>synonyms: " + String.join(", ", synonyms) + "</div>";
                html += "</div>";
            }
            html += "</div>";
        }
        html += "</div>";

        return html;
    }

    @GetMapping("/examples")
    public ResponseEntity<Object> examples(@RequestParam(value="word", defaultValue = "") String word,
                           @RequestParam(value = "lang", defaultValue = "en") String lang,
                           HttpServletResponse response) throws FileNotFoundException {
        JSONObject res = new JSONObject();
        if (word.isEmpty()) {
            throw new FileNotFoundException("word is required!");
        }
        System.out.println("lang: "+lang);
        String baseDir = "upload/example/"+lang+"/";
        File directories = new File(baseDir);
        if (!directories.exists()) {
            directories.mkdirs();
            System.out.println("words directory created!");
        }
        File file = new File(baseDir +"/" + word + ".html");

        String enDir = "upload/example/en/";
        String enPath = enDir +"/" + word + ".html";
        String zhDir = "upload/example/zh-CN/";
        String zhPath = zhDir +"/" + word + ".html";

        ResponseEntity<Object> responseEntity = null;
        if (!file.exists()) {
            String url = env.getProperty("examples_baseurl") + "%s.html".formatted(word);
            Document doc = null;
            System.out.println("fetch url: "+url);
            try {
                String html = "";
                System.out.println("verify lang: "+lang+", length: "+lang.length());
                if (lang.equals("en")) {
                    System.out.println("lang is en, get url "+url);
                    doc = Jsoup.connect(url).get();
                    System.out.println("get doc");
                    Element examples = doc.getElementById("all");

                    Integer count = 1;
                    //                get 5 children of examples which doesn't contain "sentencedict"
                    Element all = new Element("div");
                    all.attr("id", "all");
                    for (Element e : examples.children()) {
                        if (!e.text().toLowerCase().contains("sentencedict")) {
//                            remove all a tags in e
                            for (Element a : e.getElementsByTag("a")) {
                                a.remove();
                            }
//                            remove all img tags in e
                            for (Element img : e.getElementsByTag("img")) {
                                img.remove();
                            }
                            all.appendChild(e);
                            if (count++ >= 5) {
                                break;
                            }
                        }
                    }
                    html = all.outerHtml();
                    System.out.println("en html: ");
                    System.out.println(html);

//                    translate html
                    String zh_html = (new Translate(env)).translateText(html);
                    File zhFile = new File(zhPath);
                    System.out.println("zh html: ");
                    System.out.println(zh_html);
//                    write zh_html to zhFile
                    FileOutputStream zhOutput = new FileOutputStream(zhFile);
                    zhOutput.write(zh_html.getBytes());
                    zhOutput.close();
                }else{
                    System.out.println("lang is not en");
//                    get content of file in en path
                    File enFile = new File(enPath);
                    if (enFile.exists()) {
                        String en_html = Files.readString((Paths.get(enPath)));
//                        replace html space symbols in en_html
                        en_html = en_html.replaceAll("&nbsp;", " ");
                        html = (new Translate(env)).translateText(en_html);
                    } else {
                        throw new FileNotFoundException("en file not found!");
                    }

                }

                System.out.println("html: "+html);
                FileOutputStream output = new FileOutputStream(file);
                output.write(html.getBytes());
                output.close();
                System.out.println("write to file: "+file.getAbsolutePath());

                InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
                responseEntity = ResponseEntity.ok().
                        contentType(MediaType.parseMediaType(MediaType.TEXT_HTML_VALUE)).
                        contentLength(file.length()).
                        body(resource);
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
