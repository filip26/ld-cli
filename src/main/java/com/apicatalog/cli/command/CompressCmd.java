package com.apicatalog.cli.command;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import com.apicatalog.base.Base16;
import com.apicatalog.cborld.CborLd;
import com.apicatalog.cborld.CborLdVersion;
import com.apicatalog.cli.JsonCborDictionary;
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

    @Option(names = { "-m", "--mode" }, description = "processing mode", paramLabel = "v1|v06|v05")
    String mode = "v1";

    @Option(names = { "-d", "--dictionary" }, description = "a custom dictionary (JSON) location")
    URI dictionary = null;

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

        var version = switch (mode) {
        case "v05" -> CborLdVersion.V05;
        case "v06" -> CborLdVersion.V06;
        case "v1" -> CborLdVersion.V1;
        default -> CborLdVersion.V1;
        };

        var encoder = CborLd.createEncoder(version)
                .base(base)
                .compactArray(!keepArrays);

        if (dictionary != null) {
            encoder.dictionary(JsonCborDictionary.of(dictionary));
        }

        var encoded = encoder
                .build()
                .encode(json.asJsonObject());

        if (output == null) {
            System.out.write(encode(encoded, true));

        } else {
            try (var os = new FileOutputStream(output)) {
                os.write(encode(encoded, hex));
                os.flush();
            }
        }

        return spec.exitCodeOnSuccess();
    }

    static byte[] encode(byte[] encoded, boolean hex) throws IOException {
        return hex
                ? Base16.encode(encoded, Base16.ALPHABET_LOWER).getBytes()
                : encoded;
    }
}