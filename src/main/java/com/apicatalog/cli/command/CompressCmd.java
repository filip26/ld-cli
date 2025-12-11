package com.apicatalog.cli.command;

import java.io.FileOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import com.apicatalog.base.Base16;
import com.apicatalog.cborld.CborLd;
import com.apicatalog.cborld.CborLdVersion;
import com.apicatalog.cli.JsonCborDictionary;
import com.apicatalog.cli.mixin.CommandOptions;
import com.apicatalog.cli.mixin.JsonInput;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;

import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "compress", mixinStandardHelpOptions = false, description = "Compress JSON-LD document into CBOR-LD.", sortOptions = true, descriptionHeading = "%n", parameterListHeading = "%nParameters:%n", optionListHeading = "%nOptions:%n")
public final class CompressCmd implements Callable<Integer> {

    @Mixin
    JsonInput input;

    @Option(names = { "-o", "--output" }, description = "Output file name.", paramLabel = "<file>")
    String output = null;

    @Option(names = { "-b", "--base" }, description = "Base URI of the input document.", paramLabel = "<uri>")
    URI base = null;

    @Option(names = { "-a", "--keep-arrays" }, description = "Preserve arrays that contain only one element.")
    boolean keepArrays = true;

    @Option(names = { "-m", "--mode" }, description = "Encoding version to use.", paramLabel = "v1|v06|v05")
    String mode = "v1";

    @Option(names = { "-d", "--dictionary" }, description = "Custom dictionary location (JSON).", paramLabel = "<uri|file>")
    URI dictionary = null;

    @Option(names = { "-x", "--hex" }, description = "Output result as hexadecimal-encoded. Automatically enabled for stdout.")
    boolean hex = false;

    @Mixin
    CommandOptions options;

    @Spec
    CommandSpec spec;

    private CompressCmd() {
    }

    @Override
    public Integer call() throws Exception {

        final JsonDocument document = input.fetch();

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
            spec.commandLine().getOut().print(Base16.encode(encoded, Base16.ALPHABET_LOWER));
            spec.commandLine().getOut().flush();

        } else {
            try (var os = new FileOutputStream(output)) {
                if (hex) {
                    os.write(Base16.encode(encoded, Base16.ALPHABET_LOWER).getBytes(StandardCharsets.UTF_8));
                } else {
                    os.write(encoded);
                }

                os.flush();
            }
        }

        return spec.exitCodeOnSuccess();
    }
}