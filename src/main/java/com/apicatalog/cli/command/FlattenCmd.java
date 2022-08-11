package com.apicatalog.cli.command;

import java.net.URI;
import java.util.concurrent.Callable;

import com.apicatalog.cli.JsonOutput;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdVersion;
import com.apicatalog.jsonld.api.FlatteningApi;
import com.apicatalog.jsonld.document.JsonDocument;

import jakarta.json.JsonStructure;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(
        name = "flatten", 
        mixinStandardHelpOptions = false, 
        description = "Flattens JSON-LD document and optionally compacts it using a context",
        sortOptions = true,
        descriptionHeading = "%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n"
        )
public final class FlattenCmd implements Callable<Integer> {

    @Option(names = { "-h", "--help" }, hidden = true, usageHelp = true)
    boolean help = false;

    @Option(names = { "-p", "--pretty" }, description = "pretty print output JSON")
    boolean pretty = false;

    @Parameters(index = "0", arity = "0..1", description = "document URL")
    URI input = null;

    @Option(names = { "-c", "--context" }, description = "context URL")
    URI context = null;

    @Option(names = { "-b", "--base" }, description = "base URL")
    String base = null;

    @Option(names = { "-m", "--mode" }, description = "processing mode", paramLabel = "1.0|1.1")
    String mode = "1.1";

    @Option(names = { "-o",
            "--ordered" }, description = "certain algorithm processing steps are ordered lexicographically")
    boolean ordered = false;

    @Option(names = { "-a", "--keep-arrays" }, description = "keep arrays with just one element")
    boolean compactArrays = true;

    @Spec
    CommandSpec spec;

    private FlattenCmd() {}

    @Override
    public Integer call() throws Exception {

        final FlatteningApi api;

        if (input != null) {
            api = JsonLd.flatten(input);

        } else {
            api = JsonLd.flatten(JsonDocument.of(System.in));
        }

        if (mode != null) {
            api.mode(JsonLdVersion.of("json-ld-" + mode));
        }

        api.context(context);
        api.base(base);
        api.ordered(ordered);
        api.compactArrays(compactArrays);

        final JsonStructure output = api.get();

        JsonOutput.print(output, pretty);

        return spec.exitCodeOnSuccess();
    }
}