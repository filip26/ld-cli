package com.apicatalog.cli.command;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.apicatalog.cli.mixin.ByteInput;
import com.apicatalog.cli.mixin.CommandOptions;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multibase.MultibaseDecoder;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "multibase", mixinStandardHelpOptions = false, description = "", sortOptions = true, descriptionHeading = "%n", parameterListHeading = "%nParameters:%n", optionListHeading = "%nOptions:%n")
public final class MultibaseCmd implements Callable<Integer> {

    static final Map<String, Multibase> BASES = Stream.of(Multibase.provided())
            .collect(Collectors.toUnmodifiableMap(Multibase::name, Function.identity()));

    static final MultibaseDecoder DECODER = MultibaseDecoder.getInstance();

    static class ModeGroup {
        @Option(names = { "-e", "--encode" }, description = "Encode input with base", paramLabel = "<base>")
        String encode = null;

        @Option(names = { "-d", "--decode" }, description = "Decode input to file.")
        boolean decode;

        @Option(names = { "-r", "--rebase" }, description = "", paramLabel = "<base>")
        String rebase = null;

        @Option(names = { "-l", "--list" }, description = "list supported base encodings.")
        boolean list = false;
    }

    @ArgGroup(exclusive = true, multiplicity = "1")
    ModeGroup mode;

    @Option(names = { "-o", "--output" }, description = "Output file name (required for --decode).", paramLabel = "<file>")
    String output = null;

    @Mixin
    ByteInput input;

    @Mixin
    CommandOptions options;

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
            for (var base : BASES.values()) {
                writer.format("%-7s%-7d%s", base.prefix(), base.length(), base.name());
                writer.println();
            }
            return spec.exitCodeOnSuccess();
        }

        if (mode.decode) {
            var document = input.fetch();

            var decoded = DECODER.decode(new String(document, StandardCharsets.UTF_8).strip());

            if (output != null) {
                try (var os = new FileOutputStream(output)) {
                    os.write(decoded);
                    os.flush();
                }

            } else {
                System.out.write(decoded);
            }

            return spec.exitCodeOnSuccess();
        }

        if (mode.encode != null) {

            Multibase base = BASES.get(mode.encode);
            
            if (base == null) {
                throw new IllegalArgumentException("Unsupported base " + mode.encode + ". List supported bases with --list.");
            }

            var encoded = base.encode(input.fetch());

            if (output != null) {
                try (var os = new FileOutputStream(output)) {
                    os.write(encoded.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }

            } else {
                spec.commandLine().getOut().print(encoded);
                spec.commandLine().getOut().flush();
            }

            return spec.exitCodeOnSuccess();
        }
        
        if (mode.rebase != null) {
            Multibase base = BASES.get(mode.rebase);
            
            if (base == null) {
                throw new IllegalArgumentException("Unsupported base " + mode.encode + ". List supported bases with --list.");
            }

            var document = input.fetch();

            var decoded = DECODER.decode(new String(document, StandardCharsets.UTF_8).strip());

            var encoded = base.encode(decoded);

            if (output != null) {
                try (var os = new FileOutputStream(output)) {
                    os.write(encoded.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }

            } else {
                spec.commandLine().getOut().print(encoded);
                spec.commandLine().getOut().flush();
            }

            return spec.exitCodeOnSuccess();            
        }
        
        spec.commandLine().usage(spec.commandLine().getOut());
        return spec.exitCodeOnUsageHelp();
    }
}