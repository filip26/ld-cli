package com.apicatalog.cli.command;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import com.apicatalog.cborld.CborLd;
import com.apicatalog.cborld.barcode.BarcodesConfig;
import com.apicatalog.cborld.config.DefaultConfig;
import com.apicatalog.cborld.config.V05Config;
import com.apicatalog.cborld.hex.Hex;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.jsonld.loader.SchemeRouter;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "compress", mixinStandardHelpOptions = false, description = "Compress JSON-LD document into CBOR-LD", sortOptions = true, descriptionHeading = "%n", parameterListHeading = "%nParameters:%n", optionListHeading = "%nOptions:%n")
public final class CompressCmd implements Callable<Integer> {

    @Option(names = { "-h", "--help" }, hidden = true, usageHelp = true)
    boolean help = false;

    @Option(names = { "-i", "--input" }, description = "input document IRI or filepath")
    URI input = null;

    @Option(names = { "-o", "--output" }, description = "output document filename, -x is implicit when missing")
    String output = null;

    @Option(names = { "-b", "--base" }, description = "input document base IRI")
    URI base = null;

    @Option(names = { "-a", "--keep-arrays" }, description = "keep arrays with just one element")
    boolean keepArrays = false;

    @Option(names = { "-m", "--mode" }, description = "processing mode", paramLabel = "default|barcodes|v05")
    String mode = "default";

    @Option(names = { "-x", "--hex" }, description = "print encoded as hexadecimal bytes")
    boolean hex = false;

    @Spec
    CommandSpec spec;

    private CompressCmd() {
    }

    @Override
    public Integer call() throws Exception {

        final Document document;

        if (input != null) {
            if (input.isAbsolute()) {
                final DocumentLoader loader = SchemeRouter.defaultInstance();
                document = loader.loadDocument(input, new DocumentLoaderOptions());
            } else {
                document = JsonDocument.of(new ByteArrayInputStream(Files.readAllBytes(Path.of(input.toString()))));
            }

        } else {
            document = JsonDocument.of(System.in);
        }

        var json = document.getJsonContent()
                .orElseThrow(() -> new IllegalArgumentException("Invalid input document. JSON document expected but got [" + document.getContentType() + "]."));

        if (JsonUtils.isNotObject(json)) {
            throw new IllegalArgumentException("The input docunent root is not JSON object but [" + json.getValueType() + "].");
        }

        var config = switch (mode) {
        case "barcodes" -> BarcodesConfig.INSTANCE;
        case "v05" -> V05Config.INSTANCE;
        default -> DefaultConfig.INSTANCE;
        };

        var encoded = CborLd.createEncoder(config)
                .base(base)
                .compactArray(!keepArrays)
                .build()
                .encode(json.asJsonObject());

        if (output == null) {
            write(encoded, System.out, true);

        } else {
            try (var os = new FileOutputStream(output)) {
                write(encoded, os, hex);
                os.flush();
            }
        }

        return spec.exitCodeOnSuccess();
    }

    byte[] encode(byte[] encoded) {
        return hex
                ? Hex.toString(encoded, encoded.length).getBytes()
                : encoded;
    }

    static void write(byte[] encoded, OutputStream os, boolean hex) throws IOException {
        if (hex) {
            for (int i = 0; i < encoded.length; i++) {
                os.write(toString(encoded[i]).getBytes());
            }
            return;
        }
        os.write(encoded);
    }

    static final String toString(byte value) {
        return String.format("%02x", value);
    }

}