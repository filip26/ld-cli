package com.apicatalog.cli.command;

import java.util.concurrent.Callable;

import com.apicatalog.cli.JsonOutput;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdVersion;
import com.apicatalog.jsonld.api.ExpansionApi;
import com.apicatalog.jsonld.document.JsonDocument;

import jakarta.json.JsonArray;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(
        name = "expand", 
        mixinStandardHelpOptions = false, 
        description = "Expands JSON-LD document",
        sortOptions = true,
        descriptionHeading = "%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n"
        )
public final class ExpandCmd implements Callable<Integer> {

    @Option(names = { "-h", "--help" }, hidden = true, usageHelp = true)
    boolean help = false;

    @Option(names = { "-p", "--pretty" }, description = "pretty print output JSON")
    boolean pretty = false;

    @Parameters(index = "0", arity = "0..1", description = "document URL")
    String input = null;

    @Option(names = { "-c", "--context" }, description = "context URL")
    String context = null;

    @Option(names = { "-b", "--base" }, description = "base URL")
    String base = null;

    @Option(names = { "-m", "--mode" }, description = "processing mode", paramLabel = "1.0|1.1")
    String mode = "1.1";

    @Option(names = { "-o",
            "--ordered" }, description = "certain algorithm processing steps are ordered lexicographically")
    boolean ordered = false;

    @Spec
    CommandSpec spec;

    private ExpandCmd() {}

    @Override
    public Integer call() throws Exception {

        final ExpansionApi api;

        if (input != null) {
            api = JsonLd.expand(input);

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

        JsonOutput.print(output, pretty);

        return spec.exitCodeOnSuccess();
    }
}