package com.apicatalog.cli.command;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.Callable;

import com.apicatalog.base.Base16;
import com.apicatalog.cborld.CborLd;
import com.apicatalog.cborld.CborLdVersion;
import com.apicatalog.cli.JsonCborDictionary;
import com.apicatalog.cli.JsonOutput;

import jakarta.json.JsonStructure;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(
        name = "decompress", 
        mixinStandardHelpOptions = false, 
        description = "Decompress CBOR-LD document into JSON-LD.", 
        sortOptions = true, 
        descriptionHeading = "%n", 
        parameterListHeading = "%nParameters:%n", 
        optionListHeading = "%nOptions:%n"
        )
public final class DecompressCmd implements Callable<Integer> {

    @Option(names = { "-h", "--help" }, hidden = true, usageHelp = true)
    boolean help = false;

    @Option(names = { "-p", "--pretty" }, description = "Pretty-print the output JSON.")
    boolean pretty = false;

    @Option(names = { "-i", "--input" }, description = "Input document URI or file path. If missing, -x is assumed.", paramLabel = "<uri>")
    URI input = null;

    @Option(names = { "-b", "--base" }, description = "Base URI of the input document.", paramLabel = "<uri>")
    URI base = null;

    @Option(names = { "-a", "--keep-arrays" }, description = "Preserve arrays that contain only one element.")
    boolean keepArrays = true;

    @Option(names = { "-d", "--dictionary" }, description = "Custom dictionary (JSON) URI(s). Can be specified multiple times.", paramLabel = "<uri>")
    URI[] dictionaries = null;

    @Option(names = { "-x", "--hex" }, description = "Treat input as a hexadecimal-encoded CBOR-LD document.")
    boolean hex = false;

    @Spec
    CommandSpec spec;

    static final java.net.http.HttpClient CLIENT = java.net.http.HttpClient.newBuilder().followRedirects(Redirect.ALWAYS).build();

    private DecompressCmd() {
    }

    @Override
    public Integer call() throws Exception {

        hex = hex || input == null;

        var encoded = decode(fetch(input));

        var decoder = CborLd.createDecoder(CborLdVersion.V1, CborLdVersion.V06, CborLdVersion.V05)
                .base(base)
                .compactArray(!keepArrays);

        if (dictionaries != null) {
            for (var dictionary : dictionaries) {
                // register dictionaries for all formats to keep it backward compatible
                decoder.dictionary(JsonCborDictionary.of(dictionary));
                decoder.dictionary(CborLdVersion.V06, JsonCborDictionary.of(dictionary));
            }
        }

        var output = decoder.build().decode(encoded);

        JsonOutput.print((JsonStructure) output, pretty);

        return spec.exitCodeOnSuccess();
    }

    static byte[] fetch(URI input) throws Exception {
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

    byte[] decode(byte[] data) {
        if (hex) {
            return Base16.decode(new String(data).strip());
        }
        return data;
    }
}