package com.apicatalog.cli.command;

import java.io.StringWriter;
import java.net.URI;
import java.util.concurrent.Callable;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdOptions.RdfDirection;
import com.apicatalog.jsonld.JsonLdVersion;
import com.apicatalog.jsonld.api.ToRdfApi;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.http.media.MediaType;
import com.apicatalog.rdf.Rdf;
import com.apicatalog.rdf.RdfDataset;
import com.apicatalog.rdf.io.RdfWriter;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(
        name = "tordf", 
        mixinStandardHelpOptions = false, 
        description = "Transforms JSON-LD document into N-Quads document", 
        sortOptions = true,
        descriptionHeading = "%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n"
        )
public final class ToRdfCmd implements Callable<Integer> {

    @Option(names = { "-h", "--help" }, hidden = true, usageHelp = true)
    boolean help = false;

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

    @Option(names = { "-d", "--direction" }, 
            description = "determines how value objects containing a base direction are transformed",
            paramLabel = "I18N_DATATYPE|COMPOUND_LITERAL")
    String rdfDirection;

    @Option(names = { "-n", "--no-blanks" }, 
            description = "omit blank nodes for triple predicates"
            )
    boolean generalizedRdf = true;  

    @Spec
    CommandSpec spec;

    private ToRdfCmd() {}

    @Override
    public Integer call() throws Exception {

        final ToRdfApi api;

        if (input != null) {
            api = JsonLd.toRdf(input);

        } else {
            api = JsonLd.toRdf(JsonDocument.of(System.in));
        }

        if (mode != null) {
            api.mode(JsonLdVersion.of("json-ld-" + mode));
        }

        api.context(context);
        api.base(base);
        api.ordered(ordered);
        api.produceGeneralizedRdf(generalizedRdf);
        
        if (rdfDirection != null) {
            api.rdfDirection(RdfDirection.valueOf(rdfDirection.toUpperCase()));
        }

        final RdfDataset output = api.get();
        
        final StringWriter stringWriter = new StringWriter();
        
        final  RdfWriter writer = Rdf.createWriter(MediaType.N_QUADS, stringWriter);
        writer.write(output);

        System.out.println(stringWriter.toString());

        return spec.exitCodeOnSuccess();
    }
}