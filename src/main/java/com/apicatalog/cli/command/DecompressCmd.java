package com.apicatalog.cli.command;

import java.io.FileOutputStream;
import java.net.URI;
import java.util.concurrent.Callable;

import com.apicatalog.cborld.CborLd;
import com.apicatalog.cborld.context.ContextError;
import com.apicatalog.cborld.encoder.Encoder;
import com.apicatalog.cborld.encoder.EncoderError;
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
        name = "decompress", 
        mixinStandardHelpOptions = false, 
        description = "Decompress CBOR-LD document as JSON-LD",
        sortOptions = true,
        descriptionHeading = "%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n"
        )
public final class DecompressCmd implements Callable<Integer> {

    @Option(names = { "-h", "--help" }, hidden = true, usageHelp = true)
    boolean help = false;

    @Option(names = { "-i", "--input" }, description = "input document IRI")
    URI input = null;

    @Parameters(index = "0", arity = "1", description = "output document filename" )
    String output = null;

    @Option(names = { "-b", "--base" }, description = "input document base IRI")
    String base = null;
    
    @Spec
    CommandSpec spec;

    private DecompressCmd() {}

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

        try {
            final Encoder api = CborLd.encoder(json.asJsonObject());
            
            if (base != null) {
                api.base(URI.create(base));
            }

            final byte[] encoded = api.encode();

            try (FileOutputStream os = new FileOutputStream(output)) {
                os.write(encoded);
                os.flush();
            }
            
            return spec.exitCodeOnSuccess();
            
        } catch (EncoderError | ContextError e) {
            //FIXME
            throw new Exception(e);
        }

    }
}