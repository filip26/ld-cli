package com.apicatalog.cli.command;

import java.net.URI;
import java.util.concurrent.Callable;

import com.apicatalog.cli.JsonOutput;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdVersion;
import com.apicatalog.jsonld.api.FromRdfApi;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.http.media.MediaType;
import com.apicatalog.jsonld.loader.HttpLoader;

import jakarta.json.JsonStructure;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(
        name = "fromrdf", 
        mixinStandardHelpOptions = false, 
        description = "Transform N-Quads document into a JSON-LD document in an expanded form", 
        sortOptions = true,
        descriptionHeading = "%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n"
        )
public final class FromRdfCmd implements Callable<Integer> {

    @Option(names = { "-h", "--help" }, hidden = true, usageHelp = true)
    boolean help = false;

    @Option(names = { "-p", "--pretty" }, description = "pretty print output JSON")
    boolean pretty = false;

    @Option(names = { "-i", "--input" }, description = "input document IRI")
    URI input = null;

    @Option(names = { "-c", "--context" }, description = "context IRI")
    URI context = null;

    @Option(names = { "-b", "--base" }, description = "input document base IRI")
    URI base = null;

    @Option(names = { "-m", "--mode" }, description = "processing mode", paramLabel = "1.0|1.1")
    String mode = "1.1";

    @Option(names = { "-o", "--ordered" }, 
            description = "certain algorithm processing steps are ordered lexicographically")
    boolean ordered = false;

    @Option(names = { "-n", "--native-types" }, 
            description = "use native types"
            )
    boolean nativeTypes = false;  

    @Spec
    CommandSpec spec;

    private FromRdfCmd() {}

    @Override
    public Integer call() throws Exception {

        final FromRdfApi api;

        if (input != null) {
            ((HttpLoader) HttpLoader.defaultInstance()).fallbackContentType(MediaType.N_QUADS);
            api = JsonLd.fromRdf(input);

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