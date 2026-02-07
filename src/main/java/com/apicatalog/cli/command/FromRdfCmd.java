package com.apicatalog.cli.command;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import com.apicatalog.cli.mixin.CommandOptions;
import com.apicatalog.cli.mixin.JsonOutput;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdVersion;
import com.apicatalog.jsonld.api.FromRdfApi;
import com.apicatalog.jsonld.document.RdfDocument;
import com.apicatalog.jsonld.http.media.MediaType;
import com.apicatalog.jsonld.loader.HttpLoader;

import jakarta.json.JsonStructure;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "fromrdf", mixinStandardHelpOptions = false, description = "Transform an N-Quads document into a JSON-LD document in expanded form.", sortOptions = true, descriptionHeading = "%n", parameterListHeading = "%nParameters:%n", optionListHeading = "%nOptions:%n")
public final class FromRdfCmd implements Callable<Integer> {

    @Option(names = { "-i", "--input" }, description = "Input document URI or file path.", paramLabel = "<uri|file>")
    public URI input = null;

    @Mixin
    JsonOutput output;

    @Option(names = { "-b", "--base" }, description = "Base URI of the input document.", paramLabel = "<uri>")
    URI base = null;

    @Option(names = { "-m", "--mode" }, description = "Processing mode.", paramLabel = "1.0|1.1")
    String mode = "1.1";

    @Option(names = { "-o",
            "--ordered" }, description = "Order certain algorithm steps lexicographically.")
    boolean ordered = false;

    @Option(names = { "-n", "--native-types" }, description = "Use native types for numbers and booleans when possible.")
    boolean nativeTypes = false;

    @Mixin
    CommandOptions options;

    @Spec
    CommandSpec spec;

    private FromRdfCmd() {
    }

    @Override
    public Integer call() throws Exception {

        final FromRdfApi api;

        if (input != null) {
            if (input.isAbsolute()) {
                ((HttpLoader) HttpLoader.defaultInstance()).fallbackContentType(MediaType.N_QUADS);
                api = JsonLd.fromRdf(input);
            } else {
                api = JsonLd.fromRdf(RdfDocument.of(new ByteArrayInputStream(Files.readAllBytes(Path.of(input.toString())))));
            }

        } else {
            api = JsonLd.fromRdf(RdfDocument.of(System.in));
        }

        if (mode != null) {
            api.mode(JsonLdVersion.of("json-ld-" + mode));
        }

        api.base(base);
        api.ordered(ordered);
        api.nativeTypes(nativeTypes);

        final var jsonld = api.get();

        output.print(spec.commandLine().getOut(), jsonld);

        return spec.exitCodeOnSuccess();
    }
}