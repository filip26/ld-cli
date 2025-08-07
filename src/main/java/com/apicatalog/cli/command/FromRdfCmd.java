package com.apicatalog.cli.command;

import java.net.URI;
import java.util.concurrent.Callable;

import com.apicatalog.cli.JsonOutput;
import com.apicatalog.cli.mixin.CommandOptions;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdVersion;
import com.apicatalog.jsonld.api.FromRdfApi;
import com.apicatalog.jsonld.document.JsonDocument;
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

    @Mixin
    CommandOptions options;

    @Option(names = { "-p", "--pretty" }, description = "Pretty-print the output JSON.")
    boolean pretty = false;

    @Option(names = { "-c", "--context" }, description = "Context URI.", paramLabel = "<uri>")
    URI context = null;

    @Option(names = { "-b", "--base" }, description = "Base URI of the input document.", paramLabel = "<uri>")
    URI base = null;

    @Option(names = { "-m", "--mode" }, description = "Processing mode.", paramLabel = "1.0|1.1")
    String mode = "1.1";

    @Option(names = { "-o",
            "--ordered" }, description = "Order certain algorithm steps lexicographically.")
    boolean ordered = false;

    @Option(names = { "-n", "--native-types" }, description = "Use native types for numbers and booleans when possible.")
    boolean nativeTypes = false;

    @Spec
    CommandSpec spec;

    private FromRdfCmd() {
    }

    @Override
    public Integer call() throws Exception {

        final FromRdfApi api;

        if (options.input != null) {
            ((HttpLoader) HttpLoader.defaultInstance()).fallbackContentType(MediaType.N_QUADS);
            api = JsonLd.fromRdf(options.input);

        } else {
            api = JsonLd.fromRdf(JsonDocument.of(System.in));
        }

        if (mode != null) {
            api.mode(JsonLdVersion.of("json-ld-" + mode));
        }

        api.base(base);
        api.ordered(ordered);
        api.nativeTypes(nativeTypes);

        final JsonStructure output = api.get();

        JsonOutput.print(output, pretty);

        return spec.exitCodeOnSuccess();
    }
}