package com.apicatalog.cli.command;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.apicatalog.cborld.hex.Hex;
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

@Command(name = "multibase", mixinStandardHelpOptions = false, description = " Detect, encode, decode, or list multibase encodings.", sortOptions = true, descriptionHeading = "%n", parameterListHeading = "%nParameters:%n", optionListHeading = "%nOptions:%n")
public final class MultibaseCmd implements Callable<Integer> {

    static final Map<String, Multibase> BASES = Stream.of(Multibase.provided())
            .collect(Collectors.toUnmodifiableMap(Multibase::name, Function.identity()));

    static final MultibaseDecoder DECODER = MultibaseDecoder.getInstance();

    static class ModeGroup {
        @Option(names = { "-e", "--encode" }, description = "Encode input using multibase.", paramLabel = "<base>")
        String encode = null;

        @Option(names = { "-d", "--decode" }, description = "Decode multibase input to raw bytes.")
        boolean decode;

        @Option(names = { "-r", "--rebase" }, description = "Re-base input using multibase.", paramLabel = "<base>")
        String rebase = null;

        @Option(names = { "-l", "--list" }, description = "List supported base encodings.")
        boolean list = false;

        @Option(names = { "-a", "--analyze" }, description = "Validate input, detect encoding, and report raw byte length.")
        boolean analyze = false;
    }

    @ArgGroup(exclusive = true, multiplicity = "1")
    ModeGroup mode;

    @Option(names = { "-o", "--output" }, description = "Output file.", paramLabel = "<file>")
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
            writer.println("Supported base encodings: " + BASES.size() + " total");
            writer.println();
            writer.printf("%s %s %s", "Prefix", "Length", "Name");
            writer.println();
            writer.println("------ ------ -----------------");
            BASES.values().stream()
                    .sorted((a, b) -> {
                        var c = a.length() - b.length();
                        if (c == 0) {
                            c = a.name().compareTo(b.name());
                        }
                        return c;
                    })
                    .forEach(base -> {
                        writer.printf("%6s %6d %s", base.prefix(), base.length(), base.name());
                        writer.println();
                    });
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

            //TODO improve when fixed https://github.com/filip26/copper-multibase/issues/97
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

        if (mode.analyze) {
            var document = input.fetch();

            var base = DECODER.getBase((char) document[0]);
            byte[] decoded = null;

            if (base.isPresent()) {

                var encoded = new String(document, StandardCharsets.UTF_8).strip();

                decoded = base.get().decode(encoded);
            }

            if (output != null) {
                try (var printer = new PrintWriter(new FileOutputStream(output))) {
                    print(printer, base.orElse(null), document, decoded);
                    printer.flush();
                }

            } else {
                print(spec.commandLine().getOut(), base.orElse(null), document, decoded);
            }

            return spec.exitCodeOnSuccess();
        }

        spec.commandLine().usage(spec.commandLine().getOut());
        return spec.exitCodeOnUsageHelp();
    }

    static final void print(PrintWriter printer, Multibase base, byte[] document, byte[] decoded) {
        if (base != null) {
            print(printer, base);
            printer.printf("%-12s%d bytes", "Length:", (decoded != null ? decoded.length : 0));
            printer.println();
            return;
        }
        printer.println("Unrecognized base encoding, prefix: " + (char) document[0] + " (" + Hex.toString(document[0]) + ").");
    }

    static final void print(PrintWriter printer, Multibase base) {
        printer.printf("%-12s", base.getClass().getSimpleName() + ":");
        printer.print("name=" + base.name());
        printer.print(", prefix=" + base.prefix());
        printer.print(", length=" + base.length());
        printer.println(" chars");
        return;
    }
}