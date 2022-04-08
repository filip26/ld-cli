package com.apicatalog.cli.command;

import java.net.URI;
import java.util.concurrent.Callable;

import com.apicatalog.cli.Output;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdEmbed;
import com.apicatalog.jsonld.JsonLdVersion;
import com.apicatalog.jsonld.api.FramingApi;

import jakarta.json.JsonObject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(
        name = "frame", 
        mixinStandardHelpOptions = false, 
        description = "Frames JSON-LD document using the frame",
        sortOptions = true,
        descriptionHeading = "%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n"
        )
public final class FrameCmd implements Callable<Integer> {

    @Option(names = { "-h", "--help" }, hidden = true, usageHelp = true)
    boolean help = false;

    @Option(names = { "-p", "--pretty" }, description = "pretty print output JSON")
    boolean pretty = false;

    @Parameters(index = "0", arity = "1", description = "document URL")
    URI input = null;

    @Parameters(index = "1", arity = "1", description = "frame URL")
    URI frame = null;

    @Option(names = { "-c", "--context" }, description = "context URL")
    String context = null;
    
    @Option(names = { "-b", "--base" }, description = "base URL")
    String base = null;

    @Option(names = { "-m", "--mode" }, description = "processing mode", paramLabel = "1.0|1.1")
    String mode = "1.1";

    @Option(names = { "-o",
            "--ordered" }, description = "certain algorithm processing steps are ordered lexicographically")
    boolean ordered = false;

    @Option(names = { "-d", "--omit-default" }, description = "")
    boolean omitDefault = false;
    
    @Option(names = { "-x", "--explicit" }, description = "")
    boolean explicit = false;
    
    @Option(names = { "-g", "--omit-graph" }, description = "")
    boolean omitGraph = false;
    
    @Option(names = { "-a", "--require-all" }, description = "")
    boolean requiredAll = false;
    
    @Option(names = { "-e", "--embed" }, description = "", paramLabel = "ALWAYS|NEVER|ONCE")
    String embed = "ONCE";

    @Spec
    CommandSpec spec;

    private FrameCmd() {}

    @Override
    public Integer call() throws Exception {

        final FramingApi api;

        if (input != null) {
            api = JsonLd.frame(input, frame);

        } else {
            //TODO https://github.com/filip26/titanium-json-ld/issues/217
            //api = JsonLd.compact(JsonDocument.of(System.in), context);
            throw new IllegalStateException();
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

        Output.print(output, pretty);

        return spec.exitCodeOnSuccess();
    }
}