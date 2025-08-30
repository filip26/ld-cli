package com.apicatalog.cli.command;

import java.net.URI;
import java.util.concurrent.Callable;

import com.apicatalog.cli.mixin.CommandOptions;
import com.apicatalog.cli.mixin.JsonInput;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdOptions.RdfDirection;
import com.apicatalog.jsonld.JsonLdVersion;
import com.apicatalog.jsonld.api.ToRdfApi;
import com.apicatalog.jsonld.http.media.MediaType;
import com.apicatalog.rdf.Rdf;
import com.apicatalog.rdf.RdfDataset;
import com.apicatalog.rdf.io.RdfWriter;

import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "tordf", mixinStandardHelpOptions = false, description = "Transform a JSON-LD document into an RDF N-Quads document.", sortOptions = true, descriptionHeading = "%n", parameterListHeading = "%nParameters:%n", optionListHeading = "%nOptions:%n")
public final class ToRdfCmd implements Callable<Integer> {

    @Mixin
    JsonInput input;

    @Option(names = { "-c", "--context" }, description = "Context URI or file path.", paramLabel = "<uri|file>")
    URI context = null;

    @Option(names = { "-b", "--base" }, description = "Base URI of the input document.", paramLabel = "<uri>")
    URI base = null;

    @Option(names = { "-m", "--mode" }, description = "Processing mode.", paramLabel = "1.0|1.1")
    String mode = "1.1";

    @Option(names = { "-o",
            "--ordered" }, description = "Order certain algorithm steps lexicographically.")
    boolean ordered = false;

    @Option(names = { "-d", "--direction" }, description = "Determine how base direction in value objects is represented.", paramLabel = "I18N_DATATYPE|COMPOUND_LITERAL")
    String rdfDirection;

    @Option(names = { "-n", "--no-blanks" }, description = "Omit blank nodes for triple predicates.")
    boolean generalizedRdf = true;

    @Mixin
    CommandOptions options;

    @Spec
    CommandSpec spec;

    private ToRdfCmd() {
    }

    @Override
    public Integer call() throws Exception {

        final ToRdfApi api = JsonLd.toRdf(input.fetch())
                .base(base)
                .ordered(ordered)
                .produceGeneralizedRdf(generalizedRdf);

        if (mode != null) {
            api.mode(JsonLdVersion.of("json-ld-" + mode));
        }

        if (context != null) {
            api.context(JsonInput.fetch(context));
        }

        if (rdfDirection != null) {
            api.rdfDirection(RdfDirection.valueOf(rdfDirection.toUpperCase()));
        }

        final RdfDataset output = api.get();

        final RdfWriter writer = Rdf.createWriter(MediaType.N_QUADS, spec.commandLine().getOut());
        writer.write(output);
        spec.commandLine().getOut().flush();

        return spec.exitCodeOnSuccess();
    }
}