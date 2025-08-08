package com.apicatalog.cli.command;

import java.net.URI;
import java.util.concurrent.Callable;

import com.apicatalog.base.Base16;
import com.apicatalog.cborld.CborLd;
import com.apicatalog.cborld.CborLdVersion;
import com.apicatalog.cli.JsonCborDictionary;
import com.apicatalog.cli.mixin.ByteInput;
import com.apicatalog.cli.mixin.CommandOptions;
import com.apicatalog.cli.mixin.JsonOutput;

import jakarta.json.JsonStructure;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "decompress", mixinStandardHelpOptions = false, description = "Decompress CBOR-LD document into JSON-LD.", sortOptions = true, descriptionHeading = "%n", parameterListHeading = "%nParameters:%n", optionListHeading = "%nOptions:%n")
public final class DecompressCmd implements Callable<Integer> {

    @Mixin
    ByteInput input;

    @Mixin
    JsonOutput output;

    @Option(names = { "-b", "--base" }, description = "Base URI of the input document.", paramLabel = "<uri>")
    URI base = null;

    @Option(names = { "-a", "--keep-arrays" }, description = "Preserve arrays that contain only one element.")
    boolean keepArrays = true;

    @Option(names = { "-d", "--dictionary" }, description = "Custom dictionary (JSON) URI(s). Can be specified multiple times.", paramLabel = "<uri|file>")
    URI[] dictionaries = null;

    @Option(names = { "-x", "--hex" }, description = "Treat input as a hexadecimal-encoded CBOR-LD document.")
    boolean hex = false;

    @Mixin
    CommandOptions options;

    @Spec
    CommandSpec spec;

    private DecompressCmd() {
    }

    @Override
    public Integer call() throws Exception {

        var encoded = decode(input.fetch());

        var decoder = CborLd.createDecoder(CborLdVersion.V1, CborLdVersion.V06, CborLdVersion.V05)
                .base(base)
                .compactArray(!keepArrays);

        if (dictionaries != null) {
            for (var dictionary : dictionaries) {
                // register dictionaries for all formats to keep it backward compatible
                decoder.dictionary(JsonCborDictionary.of(dictionary));
                decoder.dictionary(CborLdVersion.V06, JsonCborDictionary.of(dictionary));
            }
        }

        var decoded = decoder.build().decode(encoded);

        output.print(spec.commandLine().getOut(), (JsonStructure) decoded);

        return spec.exitCodeOnSuccess();
    }


    byte[] decode(byte[] data) {
        if (hex) {
            return Base16.decode(new String(data).strip());
        }
        return data;
    }
}