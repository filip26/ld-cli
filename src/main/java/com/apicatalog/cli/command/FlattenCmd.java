package com.apicatalog.cli.command;

import java.net.URI;
import java.util.concurrent.Callable;

import com.apicatalog.cli.JsonOutput;
import com.apicatalog.cli.mixin.CommandOptions;
import com.apicatalog.cli.mixin.JsonOutputOptions;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdOptions;
import com.apicatalog.jsonld.JsonLdVersion;
import com.apicatalog.jsonld.api.FlatteningApi;
import com.apicatalog.jsonld.document.JsonDocument;

import jakarta.json.JsonStructure;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "flatten", mixinStandardHelpOptions = false, description = "Flatten a JSON-LD document and optionally compact it using a context.", sortOptions = true, descriptionHeading = "%n", parameterListHeading = "%nParameters:%n", optionListHeading = "%nOptions:%n")
public final class FlattenCmd implements Callable<Integer> {

    @Mixin
    CommandOptions options;

    @Mixin
    JsonOutputOptions outputOptions;

    @Option(names = { "-c", "--context" }, description = "Context URI.", paramLabel = "<uri>")
    URI context = null;

    @Option(names = { "-e", "--expand-context" }, description = "Context URI to expand the document before flattening.", paramLabel = "<uri>")
    URI expandContext = null;

    @Option(names = { "-b", "--base" }, description = "Base URI of the input document.", paramLabel = "<uri>")
    URI base = null;

    @Option(names = { "-m", "--mode" }, description = "Processing mode.", paramLabel = "1.0|1.1")
    String mode = "1.1";

    @Option(names = { "-o",
            "--ordered" }, description = "Order certain algorithm steps lexicographically.")
    boolean ordered = false;

    @Option(names = { "-a", "--keep-arrays" }, description = "Keep arrays with just one element.")
    boolean compactArrays = true;

    @Spec
    CommandSpec spec;

    private FlattenCmd() {
    }

    @Override
    public Integer call() throws Exception {

        final FlatteningApi api;

        if (options.input != null) {
            api = JsonLd.flatten(options.input);

        } else {
            api = JsonLd.flatten(JsonDocument.of(System.in));
        }

        final JsonLdOptions options = new JsonLdOptions();
        options.setExpandContext(expandContext);

        api.options(options);

        if (mode != null) {
            api.mode(JsonLdVersion.of("json-ld-" + mode));
        }

        api.context(context);
        api.base(base);
        api.ordered(ordered);
        api.compactArrays(compactArrays);

        final JsonStructure output = api.get();

        JsonOutput.print(spec.commandLine().getOut(), output, outputOptions.pretty);

        return spec.exitCodeOnSuccess();
    }
}