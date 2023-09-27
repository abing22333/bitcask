package com.abing.bitcask.client;

import com.abing.bitcask.common.constant.DbHttpPaths;
import com.abing.bitcask.common.exception.ServerException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * BitCask客户端
 *
 * @author abing
 * @date 2023/9/27
 */
public class BitCask implements com.abing.bitcask.common.api.BitCask {

    @Override
    public byte[] get(String key) {
        return post(DbHttpPaths.DB_GET, key).body();
    }

    @Override
    public void put(String key, byte[] value) {
        post(DbHttpPaths.DB_PUT, key, value);
    }

    @Override
    public void delete(String key) {
        post(DbHttpPaths.DB_DELETE, key);
    }


    private HttpResponse<byte[]> post(String path, String key) {
        return post(path, key, new byte[0]);
    }

    private HttpResponse<byte[]> post(String path, String key, byte[] value) {

        HttpClient httpClient = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://127.0.0.1:8080%s?key=%s", path, key)))
                .POST(HttpRequest.BodyPublishers.ofByteArray(value))
                .build();
        HttpResponse<byte[]> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        int statusCode = response.statusCode();
        if (statusCode != 200) {
            throw new ServerException(new String(response.body()));
        }

        return response;
    }
}
