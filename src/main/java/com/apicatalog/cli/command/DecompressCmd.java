package com.apicatalog.cli.command;

import java.net.URI;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.Callable;

import com.apicatalog.cborld.CborLd;
import com.apicatalog.cborld.barcode.BarcodesConfig;
import com.apicatalog.cborld.config.DefaultConfig;
import com.apicatalog.cborld.config.V05Config;
import com.apicatalog.cborld.decoder.DecoderConfig;
import com.apicatalog.cli.JsonOutput;

import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(name = "decompress", mixinStandardHelpOptions = false, description = "Decompress CBOR-LD document into JSON-LD", sortOptions = true, descriptionHeading = "%n", parameterListHeading = "%nParameters:%n", optionListHeading = "%nOptions:%n")
public final class DecompressCmd implements Callable<Integer> {

    @Option(names = { "-h", "--help" }, hidden = true, usageHelp = true)
    boolean help = false;

    @Option(names = { "-p", "--pretty" }, description = "pretty print output JSON")
    boolean pretty = false;

    @Parameters(index = "0", arity = "1", description = "input document IRI or filepath")
    URI input = null;

    @Option(names = { "-b", "--base" }, description = "input document base IRI")
    URI base = null;

    @Option(names = { "-a", "--keep-arrays" }, description = "keep arrays with just one element")
    boolean keepArrays = false;

    @Option(names = { "-m", "--mode" }, description = "processing mode", paramLabel = "default|barcodes|v05")
    String mode = "default";

    @Spec
    CommandSpec spec;

    static final java.net.http.HttpClient CLIENT = java.net.http.HttpClient.newBuilder().followRedirects(Redirect.ALWAYS).build();

    private DecompressCmd() {
    }

    @Override
    public Integer call() throws Exception {

        final byte[] encoded = fetch();

        final DecoderConfig config = switch (mode) {
        case "barcodes" -> BarcodesConfig.INSTANCE;
        case "v05" -> V05Config.INSTANCE;
        default -> DefaultConfig.INSTANCE;
        };

        final JsonValue output = CborLd.createDecoder(config)
                .base(base)
                .compactArray(!keepArrays)
                .build()
                .decode(encoded);

        JsonOutput.print((JsonStructure) output, pretty);

        return spec.exitCodeOnSuccess();
    }

    protected byte[] fetch() throws Exception {
        if (input.isAbsolute()) {
            if ("file".equalsIgnoreCase(input.getScheme())) {
                return Files.readAllBytes(Path.of(input));
            }
            return fetch(input);
        }
        return Files.readAllBytes(Path.of(input.toString()));
    }

    protected byte[] fetch(URI uri) throws Exception {

        var request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .header("Accept", "*/*")
                .timeout(Duration.ofMinutes(1));

        var response = CLIENT.send(request.build(), BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            throw new IllegalArgumentException("The [" + uri + "] has returned code " + response.statusCode() + ", expected 200 OK");
        }

        try (var is = response.body()) {
            return is.readAllBytes();
        }
    }
}