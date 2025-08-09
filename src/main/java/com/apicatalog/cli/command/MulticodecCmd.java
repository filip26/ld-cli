package com.apicatalog.cli.command;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.apicatalog.cborld.hex.Hex;
import com.apicatalog.cli.mixin.ByteInput;
import com.apicatalog.cli.mixin.CommandOptions;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multibase.MultibaseDecoder;
import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.multicodec.Multicodec.Tag;
import com.apicatalog.multicodec.codec.MulticodecRegistry;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.uvarint.UVarInt;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "multicodec", mixinStandardHelpOptions = false, description = "", sortOptions = true, descriptionHeading = "%n", parameterListHeading = "%nParameters:%n", optionListHeading = "%nOptions:%n")
public final class MulticodecCmd implements Callable<Integer> {

    static final Map<String, Multibase> BASES = Stream.of(Multibase.provided())
            .collect(Collectors.toUnmodifiableMap(Multibase::name, Function.identity()));

    static final MulticodecDecoder DECODER = MulticodecDecoder.getInstance();
    static final MultibaseDecoder MULTIBASE = MultibaseDecoder.getInstance();

    static class ModeGroup {
        @Option(names = { "-e", "--enrich" }, description = "Enrich raw input of a codec", paramLabel = "<codec>")
        String enrich;

        @Option(names = { "-s", "--strip" }, description = "Strip multicodec (+ multibase) and return raw bytes.")
        boolean strip;

        @Option(names = { "-a", "--analyze" }, description = "validate, detects a codec, byte lenght.")
        boolean analyze = false;

        @Option(names = { "-l", "--list" }, description = "list of all codecs.")
        boolean list = false;

        @Option(names = { "--tags" }, description = "list of all tags.")
        boolean tags = false;

    }

    @ArgGroup(exclusive = true, multiplicity = "1")
    ModeGroup mode;

    @Option(names = { "--multibase" }, description = "Input is multibase encoded.")
    boolean multibase = false;

    @Option(names = { "--output-multibase" }, description = "Output is multibase encoded with the provided base.", paramLabel = "<base>")
    String outputBase = null;

    @Option(names = { "-o", "--output" }, description = "Output file name.", paramLabel = "<file>")
    String output = null;

    @Mixin
    ByteInput input;

    @Mixin
    CommandOptions options;

    @Spec
    CommandSpec spec;

    private MulticodecCmd() {
    }

    @Override
    public Integer call() throws Exception {

        var writer = spec.commandLine().getOut();

        if (mode.list) {
            writer.println("Supported codecs: " + DECODER.getRegistry().codecs().size() + " total");
            writer.println();
            writer.printf("%-9s%-32s%-14s%s", "Code", "Name", "Tag", "Status");
            writer.println();
            writer.println("-------- ------------------------------- ------------- ----------");
            DECODER.getRegistry().codecs().values()
                    .stream().sorted((a, b) -> (int) (a.code() - b.code()))
                    .forEach(codec -> {
                        writer.format("%8s %-31s %-13s %s",
                                codec.code(),
                                codec.name(),
                                codec.tag() != null ? codec.tag() : "",
                                codec.status() != null ? codec.status() : "");
                        writer.println();
                    });
            return spec.exitCodeOnSuccess();
        }

        if (mode.tags) {
            writer.println("Supported tags: " + Tag.values().length + " total");
            writer.println();
            writer.printf("%-14s%s", "Name", "Codecs");
            writer.println();
            writer.println("------------- ------");
            Stream.of(Tag.values())
                    .sorted((a, b) -> a.name().compareTo(b.name()))
                    .forEach(tag -> {
                        writer.format("%-13s %6d",
                                tag,
                                MulticodecRegistry.provided(tag).size());
                        writer.println();

                    });
            return spec.exitCodeOnSuccess();
        }

        var document = input.fetch();
        String encoded = null;
        Optional<Multibase> base = Optional.empty();

        if (multibase) {
            encoded = new String(document, StandardCharsets.UTF_8).strip();

            base = MULTIBASE.getBase(encoded);

            if (base.isPresent()) {
                encoded = new String(document, StandardCharsets.UTF_8).strip();
                document = base.get().decode(encoded);
            } else {
                MultibaseCmd.print(spec.commandLine().getErr(), null, document, null);
                return mode.analyze 
                        ? spec.exitCodeOnSuccess()
                        : spec.exitCodeOnExecutionException();
            }
        }

        if (mode.strip) {
            var decoded = DECODER.decode(document);

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

        if (mode.analyze) {
            var codec = DECODER.getCodec(document);

            byte[] decoded = null;

            if (codec.isPresent()) {
                decoded = codec.get().decode(document);
            }

            if (output != null) {
                try (var printer = new PrintWriter(new FileOutputStream(output))) {
                    base.ifPresent(b -> MultibaseCmd.print(printer, b));
                    print(printer, codec.orElse(null), base.orElse(null), encoded, document, decoded);
                    printer.flush();
                }

            } else {
                base.ifPresent(b -> MultibaseCmd.print(spec.commandLine().getOut(), b));
                print(spec.commandLine().getOut(), codec.orElse(null), base.orElse(null), encoded, document, decoded);
            }

            return spec.exitCodeOnSuccess();
        }

        spec.commandLine().usage(spec.commandLine().getOut());
        return spec.exitCodeOnUsageHelp();
    }

    static final void print(PrintWriter printer, Multicodec codec, Multibase base, String encoded, byte[] document, byte[] decoded) {
        if (codec != null) {
            printer.printf("%-12s", codec.getClass().getSimpleName() + ":");
            printer.print("name=" + codec.name());
            printer.print(", code=" + codec.code());
            printer.print(", varint=" + Hex.toString(codec.varint()));
            printer.print(", tag=" + codec.tag());
            printer.println(", status=" + codec.status());
//            if (encoded != null && base != null) {
//                printer.printf("%-12s%s", "Prefix:",  encoded.substring(0, 1 + codec.varint().length) + " (" + base.name() + " encoded)");
//                printer.println();
//            }
            printer.printf("%-12s%d bytes", "Length:", (decoded != null ? decoded.length : 0));
            printer.println();
            return;
        }
        printer.println("Unrecognized codec " + Hex.toString(decoded) + "(" + UVarInt.decode(decoded) + ").");
    }
}