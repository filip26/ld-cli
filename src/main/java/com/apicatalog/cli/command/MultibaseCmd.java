package com.apicatalog.cli.command;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import com.apicatalog.cli.mixin.CommandOptions;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.multibase.Multibase;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "multibase", mixinStandardHelpOptions = false, description = "", sortOptions = true, descriptionHeading = "%n", parameterListHeading = "%nParameters:%n", optionListHeading = "%nOptions:%n")
public final class MultibaseCmd implements Callable<Integer> {

    static final Collection<Multibase> BASES = List.of(Multibase.BASE_58_BTC,
            Multibase.BASE_64,
            Multibase.BASE_64_PAD,
            Multibase.BASE_64_URL,
            Multibase.BASE_64_URL_PAD,
            Multibase.BASE_32,
            Multibase.BASE_32_UPPER,
            Multibase.BASE_32_PAD,
            Multibase.BASE_32_PAD_UPPER,
            Multibase.BASE_32_HEX,
            Multibase.BASE_32_HEX_UPPER,
            Multibase.BASE_32_HEX_PAD,
            Multibase.BASE_32_HEX_PAD_UPPER,
            Multibase.BASE_16,
            Multibase.BASE_16_UPPER,
            Multibase.BASE_2);

    @Mixin
    CommandOptions options;

    static class ModeGroup {
        @Option(names = { "-e", "--encode" }, description = "", paramLabel = "<base>")
        String encode = null;

        @Option(names = { "-d", "--decode" }, description = "")
        boolean decode = false;

        @Option(names = { "-r", "--rebase" }, description = "", paramLabel = "<base>")
        String rebase = null;

        @Option(names = { "-l", "--list" }, description = "list supported base encodings.")
        boolean list = false;
    }

    @ArgGroup(exclusive = true, multiplicity = "1")
    ModeGroup mode;
    
    @Spec
    CommandSpec spec;

    private MultibaseCmd() {
    }

    @Override
    public Integer call() throws Exception {

        var writer = spec.commandLine().getOut();
        
        if (mode.list) {
            writer.println("Supported base encodings:");
            writer.println();
            writer.printf("%s %s %s", "Prefix", "Length", "Name");
            writer.println();
            writer.println("------ ------ -----------------");
            for (var base : BASES) {
                writer.format("%-7s%-7d%s", base.prefix(), base.length(), base.name());
                writer.println();
            }
            return spec.exitCodeOnSuccess();
        }
        
        if (mode.decode) {
            
        }

        final Document document;

        if (options.input != null) {
//            if (options.input.isAbsolute()) {
//                var loader = SchemeRouter.defaultInstance();
//                document = loader.loadDocument(options.input, new DocumentLoaderOptions());
//
//            } else {
//                try (final Reader reader = Files.newBufferedReader(Path.of(options.input.toString()), StandardCharsets.UTF_8)) {
//                    document = JsonDocument.of(reader);
//                }
//            }

        } else {
            try (final Reader reader = new InputStreamReader(System.in)) {
                document = JsonDocument.of(reader);
            }
        }

//        JsonCanonicalizer.canonize(
//                document.getJsonContent()
//                        .orElseThrow(() -> new IllegalArgumentException("Invalid input document. JSON document expected but got [" + document.getContentType() + "].")),
//                spec.commandLine().getOut());
//
//        spec.commandLine().getOut().flush();
//        
        return spec.exitCodeOnSuccess();
    }
}