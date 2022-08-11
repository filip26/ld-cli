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

@Command(
        name = "compact", 
        mixinStandardHelpOptions = false, 
        description = "Compacts JSON-LD document using the context",
        sortOptions = true,
        descriptionHeading = "%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n"
        )
public final class CompactCmd implements Callable<Integer> {

    @Option(names = { "-h", "--help" }, hidden = true, usageHelp = true)
    boolean help = false;

    @Option(names = { "-p", "--pretty" }, description = "pretty print output JSON")
    boolean pretty = false;

    @Parameters(index = "0", arity = "1", description = "document URL")
    URI input = null;

    @Parameters(index = "1", arity = "1", description = "context URL")
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
    
    @Option(names = { "-r", "--keep-iris" }, description = "keep absolute IRIs")
    boolean compactToRelative = true;
    
    @Spec
    CommandSpec spec;

    private CompactCmd() {}

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
        api.compactArrays(compactArrays);
        api.compactToRelative(compactToRelative);

        final JsonObject output = api.get();

        JsonOutput.print(output, pretty);

        return spec.exitCodeOnSuccess();
    }
}