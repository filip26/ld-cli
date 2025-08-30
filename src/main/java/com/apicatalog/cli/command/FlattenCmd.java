package com.apicatalog.cli.command;

import java.net.URI;
import java.util.concurrent.Callable;

import com.apicatalog.cli.mixin.CommandOptions;
import com.apicatalog.cli.mixin.JsonInput;
import com.apicatalog.cli.mixin.JsonOutput;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdOptions;
import com.apicatalog.jsonld.JsonLdVersion;
import com.apicatalog.jsonld.api.FlatteningApi;

import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "flatten", mixinStandardHelpOptions = false, description = "Flatten a JSON-LD document and optionally compact it using a context.", sortOptions = true, descriptionHeading = "%n", parameterListHeading = "%nParameters:%n", optionListHeading = "%nOptions:%n")
public final class FlattenCmd implements Callable<Integer> {

    @Mixin
    JsonInput input;

    @Mixin
    JsonOutput output;

    @Option(names = { "-c", "--context" }, description = "Context URI or file path.", paramLabel = "<uri|file>")
    URI context = null;

    @Option(names = { "-e", "--expand-context" }, description = "Context to expand the document before flattening.", paramLabel = "<uri|file>")
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

    @Mixin
    CommandOptions options;

    @Spec
    CommandSpec spec;

    private FlattenCmd() {
    }

    @Override
    public Integer call() throws Exception {

        final FlatteningApi api = JsonLd.flatten(input.fetch());

        final JsonLdOptions options = new JsonLdOptions();
        if (expandContext != null) {
            options.setExpandContext(JsonInput.fetch(expandContext));
        }

        api.options(options)
                .base(base)
                .ordered(ordered)
                .compactArrays(compactArrays);

        if (mode != null) {
            api.mode(JsonLdVersion.of("json-ld-" + mode));
        }

        if (context != null) {
            api.context(JsonInput.fetch(context));
        }

        output.print(spec.commandLine().getOut(), api.get());

        return spec.exitCodeOnSuccess();
    }
}