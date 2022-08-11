package com.apicatalog.cli.command;

import java.io.FileOutputStream;
import java.net.URI;
import java.util.concurrent.Callable;

import com.apicatalog.cborld.CborLd;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.jsonld.loader.SchemeRouter;

import jakarta.json.JsonStructure;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(
        name = "compress", 
        mixinStandardHelpOptions = false, 
        description = "Compress JSON-LD document into CBOR-LD",
        sortOptions = true,
        descriptionHeading = "%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n"
        )
public final class CompressCmd implements Callable<Integer> {

    @Option(names = { "-h", "--help" }, hidden = true, usageHelp = true)
    boolean help = false;

    @Option(names = { "-i", "--input" }, description = "input document IRI")
    URI input = null;

    @Parameters(index = "0", arity = "1", description = "output document filename" )
    String output = null;

    @Option(names = { "-b", "--base" }, description = "input document base IRI")
    URI base = null;
    
    @Spec
    CommandSpec spec;

    private CompressCmd() {}

    @Override
    public Integer call() throws Exception {

        final Document document;

        if (input != null) {
            final DocumentLoader loader = SchemeRouter.defaultInstance();
            document = loader.loadDocument(input, new DocumentLoaderOptions());
            
        } else {
            document = JsonDocument.of(System.in);            
        }

        final JsonStructure json = document.getJsonContent().orElseThrow(() -> new IllegalArgumentException("Invalid input document. JSON document expected but got [" + document.getContentType() + "]."));
        
        if (JsonUtils.isNotObject(json)) {
            throw new IllegalArgumentException("The input docunent root is not JSON object but [" + json.getValueType() + "].");
        }
        
        final byte[] encoded = CborLd.encoder(json.asJsonObject())
                                    .base(base)
                                    .encode();

        try (FileOutputStream os = new FileOutputStream(output)) {
            os.write(encoded);
            os.flush();
        }
        
        return spec.exitCodeOnSuccess();            
    }
}