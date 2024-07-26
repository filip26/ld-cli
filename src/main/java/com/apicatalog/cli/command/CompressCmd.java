package com.apicatalog.cli.command;

import java.io.FileOutputStream;
import java.net.URI;
import java.util.concurrent.Callable;

import com.apicatalog.cborld.CborLd;
import com.apicatalog.cborld.barcode.BarcodesConfig;
import com.apicatalog.cborld.config.DefaultConfig;
import com.apicatalog.cborld.config.V05Config;
import com.apicatalog.cborld.encoder.EncoderConfig;
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

@Command(name = "compress", mixinStandardHelpOptions = false, description = "Compress JSON-LD document into CBOR-LD", sortOptions = true, descriptionHeading = "%n", parameterListHeading = "%nParameters:%n", optionListHeading = "%nOptions:%n")
public final class CompressCmd implements Callable<Integer> {

    @Option(names = { "-h", "--help" }, hidden = true, usageHelp = true)
    boolean help = false;

    @Option(names = { "-i", "--input" }, description = "input document IRI")
    URI input = null;

    @Parameters(index = "0", arity = "1", description = "output document filename")
    String output = null;

    @Option(names = { "-b", "--base" }, description = "input document base IRI")
    URI base = null;

    @Option(names = { "-a", "--keep-arrays" }, description = "keep arrays with just one element")
    boolean keepArrays = false;

    @Option(names = { "-m", "--mode" }, description = "processing mode", paramLabel = "default|barcodes|v05")
    String mode = "default";

    @Spec
    CommandSpec spec;

    private CompressCmd() {
    }

    @Override
    public Integer call() throws Exception {

        final Document document;

        if (input != null) {
            final DocumentLoader loader = SchemeRouter.defaultInstance();
            document = loader.loadDocument(input, new DocumentLoaderOptions());

        } else {
            document = JsonDocument.of(System.in);
        }

        final JsonStructure json = document.getJsonContent()
                .orElseThrow(() -> new IllegalArgumentException("Invalid input document. JSON document expected but got [" + document.getContentType() + "]."));

        if (JsonUtils.isNotObject(json)) {
            throw new IllegalArgumentException("The input docunent root is not JSON object but [" + json.getValueType() + "].");
        }

        final EncoderConfig config = switch (mode) {
        case "barcodes" -> BarcodesConfig.INSTANCE;
        case "v05" -> V05Config.INSTANCE;
        default -> DefaultConfig.INSTANCE;
        };

        final byte[] encoded = CborLd.createEncoder(config)
                .base(base)
                .compactArray(!keepArrays)
                .build()
                .encode(json.asJsonObject());

        try (FileOutputStream os = new FileOutputStream(output)) {
            os.write(encoded);
            os.flush();
        }

        return spec.exitCodeOnSuccess();
    }
}