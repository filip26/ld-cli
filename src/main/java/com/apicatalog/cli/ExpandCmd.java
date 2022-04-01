package com.apicatalog.cli;

import java.io.StringWriter;
import java.util.Collections;
import java.util.concurrent.Callable;

import com.apicatalog.jsonld.JsonLd;
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
    String input;
    
    @Spec
    CommandSpec spec;

    private ExpandCmd() {
    }

    @Override
    public Integer call() throws Exception {

        ExpansionApi api;
        
        if (input != null) {
            System.out.println("Fetching " + input);
            api = JsonLd.expand(input);

        } else {
            api = JsonLd.expand(JsonDocument.of(System.in));
        }
        
        JsonArray output = null;
        
        try {
            output = api.get();
        
        } catch (Throwable e) {
            System.err.println(e.getMessage());
            return spec.exitCodeOnExecutionException();            
        }
        
        if (pretty) {
            final JsonWriterFactory writerFactory = Json.createWriterFactory(Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true));

            StringWriter stringWriter = new StringWriter();

            try (final JsonWriter jsonWriter = writerFactory.createWriter(stringWriter)) {
                jsonWriter.writeArray(output);
            }
            
            System.out.println(stringWriter.toString());
            
        } else {
            System.out.println(output.toString());            
        }
                
        return spec.exitCodeOnSuccess();
    }
}