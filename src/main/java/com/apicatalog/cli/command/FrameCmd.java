package com.apicatalog.cli.command;

import java.net.URI;
import java.util.concurrent.Callable;

import com.apicatalog.cli.JsonOutput;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdEmbed;
import com.apicatalog.jsonld.JsonLdVersion;
import com.apicatalog.jsonld.api.FramingApi;
import com.apicatalog.jsonld.document.JsonDocument;

import jakarta.json.JsonObject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(name = "frame", mixinStandardHelpOptions = false, description = "Frame a JSON-LD document using the provided frame.", sortOptions = true, descriptionHeading = "%n", parameterListHeading = "%nParameters:%n", optionListHeading = "%nOptions:%n")
public final class FrameCmd implements Callable<Integer> {

    @Option(names = { "-h", "--help" }, hidden = true, usageHelp = true)
    boolean help = false;

    @Option(names = { "-p", "--pretty" }, description = "Pretty-print the output JSON.")
    boolean pretty = false;

    @Option(names = { "-i", "--input" }, description = "Input document URI or file path.", paramLabel = "<uri>")
    URI input = null;

    @Parameters(index = "0", arity = "1", description = "Frame URI.", paramLabel = "<uri>")
    URI frame = null;

    @Option(names = { "-c", "--context" }, description = "Context URI.", paramLabel = "<uri>")
    URI context = null;

    @Option(names = { "-b", "--base" }, description = "Base URI of the input document.", paramLabel = "<uri>")
    URI base = null;

    @Option(names = { "-m", "--mode" }, description = "Processing mode.", paramLabel = "1.0|1.1")
    String mode = "1.1";

    @Option(names = { "-o",
            "--ordered" }, description = "Order certain algorithm steps lexicographically.")
    boolean ordered = false;

    @Option(names = { "-d", "--omit-default" }, description = "Omit properties with default values.")
    boolean omitDefault = false;

    @Option(names = { "-x", "--explicit" }, description =  "Only include properties explicitly specified in the frame.")
    boolean explicit = false;

    @Option(names = { "-g", "--omit-graph" }, description = "Omit the top-level @graph.")
    boolean omitGraph = false;

    @Option(names = { "-a", "--require-all" }, description = "Require all properties in the frame to match.")
    boolean requiredAll = false;

    @Option(names = { "-e", "--embed" }, description = "Embedding behavior.", paramLabel = "ALWAYS|NEVER|ONCE")
    String embed = "ONCE";

    @Spec
    CommandSpec spec;

    private FrameCmd() {
    }

    @Override
    public Integer call() throws Exception {

        final FramingApi api;

        if (input != null) {
            api = JsonLd.frame(input, frame);

        } else {
            api = JsonLd.frame(JsonDocument.of(System.in), frame);
        }

        if (mode != null) {
            api.mode(JsonLdVersion.of("json-ld-" + mode));
        }

        api.context(context);
        api.base(base);
        api.ordered(ordered);
        api.explicit(explicit);
        api.omitDefault(omitDefault);
        api.omitGraph(omitGraph);
        api.requiredAll(requiredAll);
        api.embed(JsonLdEmbed.valueOf(embed.toUpperCase()));

        final JsonObject output = api.get();

        JsonOutput.print(output, pretty);

        return spec.exitCodeOnSuccess();
    }
}