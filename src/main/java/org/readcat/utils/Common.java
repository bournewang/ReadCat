package org.readcat.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.readcat.security.model.UserShow;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Common {
    public static String urlGet(String url) {
        WebClient client = WebClient.create();
        String s = client.
                get().
                uri(url).
                retrieve().
                onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new Exception("4xx error"))).
                onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new Exception("5xx error"))).
                bodyToMono(String.class).
                block().
                toString();
        return s;
    }

    public static String getFile(String path) {
        System.out.println("getContentFromPath: " + path);
        String content = "";
        try {
            FileInputStream is = new FileInputStream(new File(path));
            content = new String(is.readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    static public void writeFile(String content, String path) {
        try {
            File file = new File(path);
            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public UserShow getCurrentUser()
    {
        return (UserShow) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    static public String getTitle(Document doc) {
        Elements elements = doc.head().select("title");
        if (elements.isEmpty()) return null;

        String title = elements.first().text();

        String[] seperators = {"-", "–", "|"};
        // Get the part before the first — if it exists
        for (String s : seperators) {
            s = " " + s + " ";
            if (title.contains(s)) {
                return title.substring(0, title.indexOf(s));
            }
        }
        for (String s : seperators) {
            if (title.contains(s)) {
                return title.substring(0, title.indexOf(s));
            }
        }

        return title;
    }

    static public HashMap<String, Object> getTitleContentFromUrl(String url) throws Exception
    {
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
            Element body = doc.body();
            Matcher m = Pattern.compile("(\\S+)").matcher(body.text());

//            total words in this page
            long numWordsOnPage = m.results().count();
            Elements ps = body.select("p");
            System.out.println(doc.title());
//            System.out.println("total words: " + numWordsOnPage);

            Element pWithMostWords = body;
            long highestWordCount = 0;

            if (ps.size() == 0) {
                ps = body.select("div");
            }

//            find the paragraph with most words
            for (Element p : ps) {
                Matcher m2 = Pattern.compile("(\\S+)").matcher(p.text());
                long wordCount = m2.results().count();// p.innerText.match(/\S+/g);
//                System.out.println("word count: " + wordCount);
                if (wordCount > highestWordCount) {
                    highestWordCount = wordCount;
                    pWithMostWords = p;
//                    System.out.println("highestWordCount: " + wordCount);
                }
            }

            // Keep selecting more generally until over 2/5th of the words on the page have been selected
            Element selectedContainer = pWithMostWords;
            long wordCountSelected = highestWordCount;

//            if the paragraph has words less than 40% of the total words
//            get the parent node
            while (((float) wordCountSelected / numWordsOnPage < 0.4)
                    && !selectedContainer.equals(body)
                    && selectedContainer.parent().text().length() > 0) {
                selectedContainer = selectedContainer.parent();
                Matcher m3 = Pattern.compile("(\\S+)").matcher(selectedContainer.text());
                wordCountSelected = m3.results().count();
//                System.out.println("selected/totalwords < 0.4, get parent, wordCountSelected count: " + wordCountSelected);
            }

            // Make sure a single p tag is not selected
            if (selectedContainer.tagName() == "P") {
                selectedContainer = selectedContainer.parent();
            }
//            return selectedContainer;
//            fetch base url
            URL urlObj = new URL(url);
            String oriBase = urlObj.getProtocol() + "://" + urlObj.getHost();
            Integer port = urlObj.getPort();
            if (port > 0 && port != 80 && port != 443) oriBase += ":" + port.toString();

//            deal with lazy image load in standard.co.uk/
//            replace all <amg-img> tag with <img> tag in selectedContainer
            Elements imgs = selectedContainer.select("amp-img");
            for (Element img : imgs) {
                String src = img.attr("src");
                if (src.startsWith("//")) {
                    src = "http:" + src;
                } else if (src.startsWith("/")) {
                    src = oriBase + src;
                }
                img
                        .tagName("img")
                        .attr("src", src)
                        .removeAttr("width")
                        .removeAttr("height")
                ;
            }
//            remove all i-amphtml-sizer
            selectedContainer.select("i-amphtml-sizer").remove();

            for (Element img: selectedContainer.select("img")) {
                if (!img.attr("data-src").isEmpty()) {
                    img.attr("src", img.attr("data-src"));
                }
                String src = img.attr("src");
                img.clearAttributes();
                img.attr("src", src);
                if (src.startsWith("/")) {
                    img.setBaseUri(oriBase);
                }
            }

//            for each noscript element, parse its content as html and find the img tag to replace noscript tag
            for (Element noscript: selectedContainer.select("noscript")) {
                String html = noscript.html();
                Document doc2 = Jsoup.parse(html);
                Element img = doc2.select("img").first();
                if (img != null) {
                    noscript.replaceWith(img);
                }
            }

            // set all a tags href to null
            for (Element a: selectedContainer.select("a")) {
                a.clearAttributes();
            }

            for (Element figure: selectedContainer.select("figure.image--embedded")) {
                Element amp = figure.selectFirst("amp-img");
                if (amp == null) continue;
                String src = amp.attr("src");
                Element parent = figure.parent();
                Element caption = parent.selectFirst("figcaption");
                String title = null;
                if (caption != null ){
                    title = caption.text();
                }
                parent.children().remove();

                Element img = new Element("img");
                img.attr("src", src);
                img.appendTo(parent);
                if (title != null) {
                    Element cap = new Element("p");
                    cap.text(title);
                    cap.appendTo(parent);
                }
            }

            List<String> selectors = List.of(
                    ".metro-email-signup", // metro
                    "[data-embed-group=read-more]", // standard
                    "#piano-reg-wall", // standard
                    ".social__button-container",//new scientist
                    ".Image__Copyright" //national geographic
            );
            for (String selector: selectors) {
                selectedContainer.select(selector).remove();
            }

            String[] words = selectedContainer.text().split("\\S+");
//            Map
//            Common.getTitle(doc), selectedContainer.outerHtml(), words.length
//            return a hash map with title, content, and words count
            HashMap<String, Object> res = new HashMap<String, Object>();
            res.put("title", Common.getTitle(doc));
            res.put("content", selectedContainer.outerHtml());
            res.put("wordCount", words.length);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
