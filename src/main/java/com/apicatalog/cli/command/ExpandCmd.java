package com.apicatalog.cli.command;

import java.net.URI;
import java.util.concurrent.Callable;

import com.apicatalog.cli.JsonOutput;
import com.apicatalog.cli.mixin.CommandOptions;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdVersion;
import com.apicatalog.jsonld.api.ExpansionApi;
import com.apicatalog.jsonld.document.JsonDocument;

import jakarta.json.JsonArray;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "expand", mixinStandardHelpOptions = false, description = "Expand a JSON-LD document.", sortOptions = true, descriptionHeading = "%n", parameterListHeading = "%nParameters:%n", optionListHeading = "%nOptions:%n")
public final class ExpandCmd implements Callable<Integer> {

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

    @Spec
    CommandSpec spec;

    private ExpandCmd() {
    }

    @Override
    public Integer call() throws Exception {

        final ExpansionApi api;

        if (options.input != null) {
            api = JsonLd.expand(options.input);

        } else {
            api = JsonLd.expand(JsonDocument.of(System.in));
        }

        if (mode != null) {
            api.mode(JsonLdVersion.of("json-ld-" + mode));
        }

        api.context(context);
        api.base(base);
        api.ordered(ordered);

        final JsonArray output = api.get();

        JsonOutput.print(spec.commandLine().getOut(), output, pretty);

        return spec.exitCodeOnSuccess();
    }
}