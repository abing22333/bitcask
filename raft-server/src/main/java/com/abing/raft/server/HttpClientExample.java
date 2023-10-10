package com.abing.raft.server;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

public class HttpClientExample {

    public static void main(String[] args) throws IOException, InterruptedException {
        // 创建一个新的HTTP客户端
        HttpClient httpClient = HttpClient.newHttpClient();

        // 创建HTTP请求
        HttpRequest setRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:8080/db/put?key=abing"))
                .POST(HttpRequest.BodyPublishers.ofByteArray("fffdddd".getBytes()))
                .build();

        HttpResponse<byte[]> response1 = httpClient.send(setRequest, HttpResponse.BodyHandlers.ofByteArray());
        System.out.println("Status Code: " + response1.statusCode());
        System.out.println("Response Body: " + Arrays.toString(response1.body()));



        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:8080/db/get?key=abing"))
                .GET()
                .build();

        HttpResponse<byte[]> response2 = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofByteArray());
        System.out.println("Status Code: " + response2.statusCode());
        System.out.println("Response Body: " + Arrays.toString(response2.body()));
    }
}
