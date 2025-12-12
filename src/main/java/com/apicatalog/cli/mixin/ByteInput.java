package com.apicatalog.cli.mixin;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import picocli.CommandLine.Option;

public class ByteInput {

    @Option(names = { "-i", "--input" }, description = "Input document URI or file path.", paramLabel = "<uri|file>")
    public URI input = null;

    public byte[] fetch() throws Exception {
        if (input == null) {
            return System.in.readAllBytes();
        }

        if (input.isAbsolute()) {
            if ("file".equalsIgnoreCase(input.getScheme())) {
                return Files.readAllBytes(Path.of(input));
            }
            try (var is = fetch(input)) {
                return is.readAllBytes();
            }
        }
        return Files.readAllBytes(Path.of(input.toString()));
    }

    static InputStream fetch(URI uri) throws Exception {

        if (!"https".equalsIgnoreCase(uri.getScheme())
                && !"http".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalArgumentException("Unsupported scheme [" + uri + "]");
        }

        var request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .header("Accept", "*/*")
                .timeout(Duration.ofMinutes(1));

        var client = HttpClient.newBuilder()
                .followRedirects(Redirect.ALWAYS)
                .build();

        var response = client.send(request.build(), BodyHandlers.ofInputStream());

        if (response.statusCode() == 200) {
            return response.body();
        }

        throw new IllegalArgumentException("The [" + uri + "] has returned code " + response.statusCode() + ", expected 200 OK");
    }

}
