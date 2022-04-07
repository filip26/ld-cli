package com.apicatalog.cli;

import java.io.StringWriter;
import java.net.URI;
import java.util.Collections;
import java.util.concurrent.Callable;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdVersion;
import com.apicatalog.jsonld.api.CompactionApi;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;
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
final class CompactCmd implements Callable<Integer> {

    @Option(names = { "-h", "--help" }, hidden = true, usageHelp = true)
    boolean help = false;

    @Option(names = { "-p", "--pretty" }, description = "pretty print output JSON")
    boolean pretty = false;

    @Parameters(index = "0", arity = "1", description = "input URL")
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
            //TODO https://github.com/filip26/titanium-json-ld/issues/217
            //api = JsonLd.compact(JsonDocument.of(System.in), context);
            throw new IllegalStateException();
        }

        if (mode != null) {
            api.mode(JsonLdVersion.of("json-ld-" + mode));
        }

        api.base(base);
        api.ordered(ordered);
        api.compactArrays(compactArrays);
        api.compactToRelative(compactToRelative);

        final JsonObject output = api.get();

        if (pretty) {
            final JsonWriterFactory writerFactory = Json
                    .createWriterFactory(Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true));

            StringWriter stringWriter = new StringWriter();

            try (final JsonWriter jsonWriter = writerFactory.createWriter(stringWriter)) {
                jsonWriter.writeObject(output);
            }

            System.out.println(stringWriter.toString());

        } else {
            System.out.println(output.toString());
        }

        return spec.exitCodeOnSuccess();
    }
}