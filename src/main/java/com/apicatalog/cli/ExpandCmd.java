package com.apicatalog.cli;

import java.io.StringWriter;
import java.util.Collections;
import java.util.concurrent.Callable;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdVersion;
import com.apicatalog.jsonld.api.ExpansionApi;
import com.apicatalog.jsonld.document.JsonDocument;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(name = "expand", mixinStandardHelpOptions = false, description = "Expand JSON-LD 1.1 document", sortOptions = false, descriptionHeading = "%n", parameterListHeading = "%nParameters:%n", optionListHeading = "%nOptions:%n")
final class ExpandCmd implements Callable<Integer> {

    @Option(names = { "-h", "--help" }, hidden = true, usageHelp = true)
    boolean help = false;

    @Option(names = { "-p", "--pretty" }, description = "pretty print output JSON")
    boolean pretty = false;

    @Parameters(index = "0", arity = "0..1", description = "input URL")
    String input = null;

    @Option(names = { "-c", "--context" }, description = "context URL")
    String context = null;

    @Option(names = { "-b", "--base" }, description = "base URL")
    String base = null;

    @Option(names = { "-m", "--mode" }, description = "processing mode, e.g. --mode=1.1", paramLabel = "1.0|1.1")
    String mode = "1.1";

    @Option(names = { "-o",
            "--ordered" }, description = "certain algorithm processing steps are ordered lexicographically")
    boolean ordered = false;

    @Spec
    CommandSpec spec;

    private ExpandCmd() {
    }

    @Override
    public Integer call() throws Exception {

        try {
            ExpansionApi api;

            if (input != null) {
                api = JsonLd.expand(input);

            } else {
                api = JsonLd.expand(JsonDocument.of(System.in));
            }

            api.mode(JsonLdVersion.of("json-ld-" + mode));
            api.context(context);
            api.base(base);
            api.ordered(ordered);

            JsonArray output = null;

            output = api.get();

            if (pretty) {
                final JsonWriterFactory writerFactory = Json
                        .createWriterFactory(Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true));

                StringWriter stringWriter = new StringWriter();

                try (final JsonWriter jsonWriter = writerFactory.createWriter(stringWriter)) {
                    jsonWriter.writeArray(output);
                }

                System.out.println(stringWriter.toString());

            } else {
                System.out.println(output.toString());
            }

        } catch (Throwable e) {
            System.err.println("ERROR: " + e.getMessage());
            return spec.exitCodeOnExecutionException();
        }

        return spec.exitCodeOnSuccess();
    }
}