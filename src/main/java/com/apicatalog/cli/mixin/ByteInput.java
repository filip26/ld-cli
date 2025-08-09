package com.apicatalog.cli.mixin;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import picocli.CommandLine.Option;

public class ByteInput {
    
    @Option(names = { "-i", "--input" }, description = "Input document URI or file path.", paramLabel = "<uri|file>")
    public URI input = null;
    
    static final java.net.http.HttpClient CLIENT = java.net.http.HttpClient.newBuilder().followRedirects(Redirect.ALWAYS).build();
    
    public byte[] fetch() throws Exception {
        if (input == null) {
            return System.in.readAllBytes();
        }

        if (input.isAbsolute()) {
            if ("file".equalsIgnoreCase(input.getScheme())) {
                return Files.readAllBytes(Path.of(input));
            }
            try (var is = fetchHttp(input)) {
                return is.readAllBytes();
            }
        }
        return Files.readAllBytes(Path.of(input.toString()));
    }

    static InputStream fetchHttp(URI uri) throws Exception {

        var request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .header("Accept", "*/*")
                .timeout(Duration.ofMinutes(1));

        var response = CLIENT.send(request.build(), BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            throw new IllegalArgumentException("The [" + uri + "] has returned code " + response.statusCode() + ", expected 200 OK");
        }
        return response.body();
    }

}
