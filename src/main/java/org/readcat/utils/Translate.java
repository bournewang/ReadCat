package org.readcat.utils;

import com.baidu.translate.demo.TransApi;
import com.google.cloud.translate.v3.*;
import com.google.cloud.translate.v3beta1.BatchTranslateDocumentResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;

public class Translate {

    private Environment env;

    public Translate(Environment env) {
        this.env = env;
    }

    public String translateText(String query) {
        WebClient webClient = WebClient.create();
        Mono<String> response = webClient.post()
                .uri(env.getProperty("google.translate_api.url"))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(Mono.just("q=" + query + "&source=en&target=zh-CN&key=" + env.getProperty("google.translate_api.app_key")), String.class)
                .retrieve()
                .bodyToMono(String.class);

        String res = response.block();
        JSONObject json = (JSONObject) JSONValue.parse(res);
        JSONObject data = (JSONObject) json.get("data");
        JSONArray result = (JSONArray) data.get("translations");
        JSONObject first = (JSONObject) result.get(0);
        String translate = (String) first.get("translatedText");
        System.out.println(translate);
        return translate;
    }

    public String BaiduTranslate(String query)
    {
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
}

