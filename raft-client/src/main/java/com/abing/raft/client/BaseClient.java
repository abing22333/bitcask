package com.abing.raft.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * 公用的client方法
 *
 * @author abing
 * @date 2023/10/20
 */
public class BaseClient {

    protected HttpClient httpClient;

    protected <T, R> R post(String host, String path, T value) throws IOException, InterruptedException {
        byte[] byteValue = SerializationUtil.serialize(value);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://%s%s", host, path)))
                .POST(HttpRequest.BodyPublishers.ofByteArray(byteValue))
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

        return SerializationUtil.deserialize(response.body());
    }
}
