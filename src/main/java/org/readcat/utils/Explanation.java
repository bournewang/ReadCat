package org.readcat.utils;

import io.netty.handler.codec.http.HttpUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.core.env.Environment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import javax.servlet.http.HttpUtils;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Explanation {
    private Environment env;

    public Explanation(Environment env) {
        this.env = env;
    }

    public static void main(String[] args) {
        System.out.println("Hello World!");
    }

    private String dictPath(String word, String lang) {
        return env.getProperty("archive.base_dir.dict") + lang + "/" + word + ".html";
    }
    private String examplePath(String word, String lang) {
        return env.getProperty("archive.base_dir.example") + lang + "/" + word + ".html";
    }

    public HashMap<String, List<String>> getDictAndExamples(String word, List<String> langs) throws IOException {
        HashMap<String, List<String>> map = new HashMap<String, List<String>>();
        List<String> dicts = new ArrayList<>();
        List<String> examples = new ArrayList<>();

        for (String lang: langs) {
            String dictPath = dictPath(word, lang);
            String examplePath = examplePath(word, lang);
            File dictFile = new File(dictPath);
            File exampleFile = new File(examplePath);

            String word1 = word.toLowerCase().replaceAll(" ", "-").replaceAll("'", "-");
            String dictPath1 = dictPath(word1, lang);
            String examplePath1 = examplePath(word1, lang);
            File dictFile1 = new File(dictPath1);
            File exampleFile1 = new File(examplePath1);

            if (dictFile.exists() && exampleFile.exists()) {
                dicts.add(Common.getFile(dictPath));
                examples.add(Common.getFile(examplePath));
            } else if (dictFile1.exists() && exampleFile1.exists()) {
                dicts.add(Common.getFile(dictPath1));
                examples.add(Common.getFile(examplePath1));
            } else {
                if (lang.equals("en")) {
                    String dict = parseDict(word, lang);
                    if (dict != null) {
                        dicts.add(dict);
                    }
                    String example = parseExample(word, lang);
                    if (example != null) {
                        examples.add(example);
                    }
                }else{
                    HashMap<String, String> res = parseYoudao(word, lang);
                    if (res.get("dict") != null)
                        dicts.add(res.get("dict"));
                    if (res.get("example") != null)
                        examples.add(res.get("example"));
                }
            }
        }
        map.put("dicts", dicts);
        map.put("examples", examples);
        return map;
    }

    public HashMap<String, String> parseYoudao(String word, String lang) throws IOException {
//        parse element className 'trans-container' in file with Jsoup
        String url = "https://dict.youdao.com/w/"+word+"/#keyfrom=dict2.top";
//        String lang = "zh-CN";
        String rel_path = lang + "/" + word + ".html";
        HashMap<String, String> map = new HashMap<String, String>();
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
            map.put("dict", "没有找到该单词");
            map.put("example", "没有找到该单词");
            return map;
        }
        Elements trans = doc.getElementsByClass("trans-container");
        if (!trans.isEmpty()){
            String defineText = trans.get(0).outerHtml();

            if (!defineText.isEmpty()) {
                String dict_path = dictPath(word, lang);
                Common.writeFile(defineText, dict_path);
                map.put("dict", defineText);
            }
        }

        String examples = "";
        Element bilingual = doc.getElementById("bilingual");
//        remove element with className 'example-via'
        if (bilingual != null) {
            bilingual.getElementsByClass("example-via").remove();
            bilingual.getElementsByClass("more-example").remove();
            examples = bilingual.outerHtml();
        } else {
            Element collins = doc.getElementById("collinsResult");
            if (collins != null) {
                examples = collins.outerHtml();
            }
        }
        if (!examples.isEmpty()) {
            String example_path = examplePath(word, lang);
            Common.writeFile(examples, example_path);
            map.put("example", examples);
        }

        return map;
    }

    public String parseDict(String word, String lang)
    {
        String url = env.getProperty("dict_baseurl") + "/" + word;
//        抓去网页内容
        String text = null;
        try {
            text = Common.urlGet(url);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        System.out.println("========= text ==========");
        System.out.println(text);

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

        String dictPath = dictPath(word, lang);
        Common.writeFile(html, dictPath);

        return html;
    }

    public String parseExample(String word, String lang) {
        String path = examplePath(word, lang);// "upload/example/en/"+word+".html";

        String url = env.getProperty("examples_baseurl") + "%s.html".formatted(word);
        Document doc = null;
        System.out.println("fetch url: "+url);

        String html = "";
        try {
            doc = Jsoup.connect(url).get();
            System.out.println("get doc");
            Element examples = doc.getElementById("all");
            if (examples == null) {
                return null;
            }
            Integer count = 1;
            Element all = new Element("div");
            all.attr("id", "all");
            for (Element e : examples.children()) {
                if (!e.text().toLowerCase().contains("sentencedict")) {
                    for (Element a : e.getElementsByTag("a")) {
                        a.remove();
                    }
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

            Common.writeFile(html, path);
//            System.out.println("html: "+html);
//            FileOutputStream output = new FileOutputStream(file);
//            output.write(html.getBytes());
//            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return html;
    }
}
