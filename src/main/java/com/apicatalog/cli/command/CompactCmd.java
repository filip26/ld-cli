package com.apicatalog.cli.command;

import java.net.URI;
import java.util.concurrent.Callable;

import com.apicatalog.cli.JsonOutput;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdVersion;
import com.apicatalog.jsonld.api.CompactionApi;
import com.apicatalog.jsonld.document.JsonDocument;

import jakarta.json.JsonObject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(name = "compact", mixinStandardHelpOptions = false, description = "Compact a JSON-LD document using the provided context.", sortOptions = true, descriptionHeading = "%n", parameterListHeading = "%nParameters:%n", optionListHeading = "%nOptions:%n")
public final class CompactCmd implements Callable<Integer> {

    @Option(names = { "-h", "--help" }, hidden = true, usageHelp = true)
    boolean help = false;

    @Option(names = { "-p", "--pretty" }, description = "Pretty-print the output JSON.")
    boolean pretty = false;

    @Option(names = { "-i", "--input" }, description = "Input document URI or file path.", paramLabel = "<uri>")
    URI input = null;

    @Parameters(index = "0", arity = "1", description = "Context URI or file path.", paramLabel = "<uri>")
    URI context = null;

    @Option(names = { "-b", "--base" }, description = "Base URI of the input document.", paramLabel = "<uri>")
    URI base = null;

    @Option(names = { "-m", "--mode" }, description = "Processing mode.", paramLabel = "1.0|1.1")
    String mode = "1.1";

    @Option(names = { "-o",
            "--ordered" }, description = "Order certain algorithm steps lexicographically.")
    boolean ordered = false;

    @Option(names = { "-a", "--keep-arrays" }, description = "Preserve arrays with a single element.")
    boolean keepArrays = false;

    @Option(names = { "-r", "--keep-uris" }, description = "Preserve absolute  absolute URIs.")
    boolean keepAbsoluteURI = false;

    @Spec
    CommandSpec spec;

    private CompactCmd() {
    }

    @Override
    public Integer call() throws Exception {

        final CompactionApi api;

        if (input != null) {
            api = JsonLd.compact(input, context);

        } else {
            api = JsonLd.compact(JsonDocument.of(System.in), context);
        }

        if (mode != null) {
            api.mode(JsonLdVersion.of("json-ld-" + mode));
        }

        api.base(base);
        api.ordered(ordered);
        api.compactArrays(!keepArrays);
        api.compactToRelative(!keepAbsoluteURI);

        final JsonObject output = api.get();

        JsonOutput.print(output, pretty);

        return spec.exitCodeOnSuccess();
    }
}