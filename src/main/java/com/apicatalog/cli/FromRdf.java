package com.apicatalog.cli;

import java.io.StringWriter;
import java.util.Collections;
import java.util.concurrent.Callable;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdVersion;
import com.apicatalog.jsonld.api.FromRdfApi;
import com.apicatalog.jsonld.document.JsonDocument;

import jakarta.json.Json;
import jakarta.json.JsonStructure;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(
        name = "fromrdf", 
        mixinStandardHelpOptions = false, 
        description = "Transforms N-Quads document into a JSON-LD document in expanded form", 
        sortOptions = true,
        descriptionHeading = "%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n"
        )
final class FromRdf implements Callable<Integer> {

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

    @Option(names = { "-o", "--ordered" }, 
            description = "certain algorithm processing steps are ordered lexicographically")
    boolean ordered = false;

    @Option(names = { "-n", "--native-types" }, 
            description = "use native types"
            )
    boolean nativeTypes = false;  

    @Spec
    CommandSpec spec;

    private FromRdf() {}

    @Override
    public Integer call() throws Exception {

        final FromRdfApi api;

        if (input != null) {
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

        if (pretty) {
            final JsonWriterFactory writerFactory = Json
                    .createWriterFactory(Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true));

            StringWriter stringWriter = new StringWriter();

            try (final JsonWriter jsonWriter = writerFactory.createWriter(stringWriter)) {
                jsonWriter.write(output);
            }

            System.out.println(stringWriter.toString());

        } else {
            System.out.println(output.toString());
        }

        return spec.exitCodeOnSuccess();
    }
}