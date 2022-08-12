package com.apicatalog.cli.command;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.util.concurrent.Callable;

import com.apicatalog.cborld.CborLd;
import com.apicatalog.cborld.config.DefaultConfig;
import com.apicatalog.cborld.db.DbConfig;
import com.apicatalog.cborld.decoder.DecoderConfig;
import com.apicatalog.cli.JsonOutput;

import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(
        name = "decompress", 
        mixinStandardHelpOptions = false, 
        description = "Decompress CBOR-LD document into JSON-LD",
        sortOptions = true,
        descriptionHeading = "%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n"
        )
public final class DecompressCmd implements Callable<Integer> {

    @Option(names = { "-h", "--help" }, hidden = true, usageHelp = true)
    boolean help = false;

    @Option(names = { "-p", "--pretty" }, description = "pretty print output JSON")
    boolean pretty = false;

    @Parameters(index = "0", arity = "1", description = "input document filename")
    File input = null;

    @Option(names = { "-b", "--base" }, description = "input document base IRI")
    URI base = null;
    
    @Option(names = { "-a", "--keep-arrays" }, description = "keep arrays with just one element")
    boolean keepArrays = false;
    
    @Option(names = { "-m", "--mode" }, description = "processing mode", paramLabel = "default|digitalbazaar")
    String mode = "default";

    @Spec
    CommandSpec spec;

    private DecompressCmd() {}

    @Override
    public Integer call() throws Exception {

        byte[] encoded = Files.readAllBytes(input.toPath());
        
        DecoderConfig config = DefaultConfig.INSTANCE;
        
        if ("digitalbazaar".equalsIgnoreCase(mode)) {
            config = DbConfig.INSTANCE;
        }
        
        final JsonValue output = CborLd.decoder(encoded)
                                    .config(config)
                                    .base(base)
                                    .compactArray(!keepArrays)
                                    .decode();

        JsonOutput.print((JsonStructure)output, pretty);

        return spec.exitCodeOnSuccess();
    }
}