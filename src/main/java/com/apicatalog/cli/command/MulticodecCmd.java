package com.apicatalog.cli.command;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import com.apicatalog.cborld.hex.Hex;
import com.apicatalog.cli.mixin.ByteInput;
import com.apicatalog.cli.mixin.CommandOptions;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multibase.MultibaseDecoder;
import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.multicodec.Multicodec.Tag;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.multicodec.codec.MulticodecRegistry;
import com.apicatalog.uvarint.UVarInt;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "multicodec", mixinStandardHelpOptions = false, description = "", sortOptions = true, descriptionHeading = "%n", parameterListHeading = "%nParameters:%n", optionListHeading = "%nOptions:%n")
public final class MulticodecCmd implements Callable<Integer> {

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
        String based = null;
        Optional<Multibase> base = Optional.empty();

        if (multibase) {
            based = new String(document, StandardCharsets.UTF_8).strip();

            base = MULTIBASE.getBase(based);

            if (base.isPresent()) {
                based = new String(document, StandardCharsets.UTF_8).strip();
                document = base.get().decode(based);
            } else {
                MultibaseCmd.print(spec.commandLine().getErr(), null, document, null);
                return mode.analyze
                        ? spec.exitCodeOnSuccess()
                        : spec.exitCodeOnExecutionException();
            }
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
                    print(printer, codec.orElse(null), base.orElse(null), based, document, decoded);
                    printer.flush();
                }

            } else {
                base.ifPresent(b -> MultibaseCmd.print(spec.commandLine().getOut(), b));
                print(spec.commandLine().getOut(), codec.orElse(null), base.orElse(null), based, document, decoded);
            }

            return spec.exitCodeOnSuccess();
        }

        if (mode.strip) {
            output(DECODER.decode(document));
            return spec.exitCodeOnSuccess();
        }

        if (mode.enrich != null) {
            // TODO improve when fixed
            // https://github.com/filip26/copper-multicodec/issues/107
            var codec = DECODER.getRegistry().codecs().values().stream()
                    .filter(c -> c.name().equals(mode.enrich))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unsupported codec " + mode.enrich + ". List supported codecs with multicodec --list."));

            output(codec.encode(document));
            
            return spec.exitCodeOnSuccess();
        }

        spec.commandLine().usage(spec.commandLine().getOut());
        return spec.exitCodeOnUsageHelp();
    }

    void output(byte[] encoded) throws IOException {
        if (outputBase != null) {
            // TODO improve when fixed https://github.com/filip26/copper-multibase/issues/97
            print(Stream.of(Multibase.provided())
                    .filter(b -> b.name().equals(outputBase))
                    .findFirst()
                    .map(b -> b.encode(encoded).getBytes(StandardCharsets.UTF_8))
                    .orElseThrow(() -> new IllegalArgumentException("Unsupported base " + mode.enrich + ". List supported bases with multibase --list.")));
            return;
        }
        print(encoded);
    }

    void print(byte[] result) throws IOException {
        if (output != null) {
            try (var os = new FileOutputStream(output)) {
                os.write(result);
                os.flush();
            }

        } else {
            System.out.write(result);
        }
    }

    static final void print(PrintWriter printer, Multicodec codec, Multibase base, String encoded, byte[] document, byte[] decoded) {
        if (codec != null) {
            printer.printf("%-12s", codec.getClass().getSimpleName() + ":");
            printer.print("name=" + codec.name());
            printer.print(", code=" + codec.code());
            printer.print(", varint=" + Hex.toString(codec.varint()));
            printer.print(", tag=" + codec.tag());
            printer.println(", status=" + codec.status());
            printer.printf("%-12s%d bytes", "Length:", (decoded != null ? decoded.length : 0));
            printer.println();
            return;
        }
        printer.println("Unrecognized codec " + Hex.toString(decoded) + "(" + UVarInt.decode(decoded) + ").");
    }
}