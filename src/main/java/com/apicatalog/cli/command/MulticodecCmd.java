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
import com.apicatalog.multicodec.Multicodec;
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
        @Option(names = { "-s", "--strip" }, description = "Strip multicodec (+ multibase) and return raw bytes")
        boolean strip;

        @Option(names = { "-a", "--analyze" }, description = "validate, detects a codec, byte lenght")
        boolean analyze = false;
    }

    @ArgGroup(exclusive = true, multiplicity = "1")
    ModeGroup mode;

    @Option(names = { "-mb", "--multibase" }, description = "Input is multibase encoded.")
    boolean multibase = false;

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

        if (mode.strip) {
            var document = input.fetch();

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
            var document = input.fetch();

            if (multibase) {
                var based = new String(document, StandardCharsets.UTF_8).strip();

                var base = MULTIBASE.getBase(based);

                if (base.isPresent()) {
                    var encoded = new String(document, StandardCharsets.UTF_8).strip();
                    document = base.get().decode(encoded);
                    MultibaseCmd.print(writer, base.get());

                } else {
                    MultibaseCmd.print(writer, null, document, null);
                    return spec.exitCodeOnSuccess();
                }
            }

            var codec = DECODER.getCodec(document);

            byte[] decoded = null;

            if (codec.isPresent()) {
                decoded = codec.get().decode(document);
            }

            if (output != null) {
                try (var printer = new PrintWriter(new FileOutputStream(output))) {
                    print(printer, codec.orElse(null), document, decoded);
                    printer.flush();
                }

            } else {
                print(spec.commandLine().getOut(), codec.orElse(null), document, decoded);
            }

            return spec.exitCodeOnSuccess();
        }

        spec.commandLine().usage(spec.commandLine().getOut());
        return spec.exitCodeOnUsageHelp();
    }

    static final void print(PrintWriter printer, Multicodec codec, byte[] document, byte[] decoded) {
        if (codec != null) {
            printer.print(codec.getClass().getSimpleName());
            printer.print(" [name: " + codec.name());
            printer.print(", code: " + codec.code() + " " + Hex.toString(codec.varint()));
            printer.print(", tag: " + codec.tag());
            printer.print(", status: " + codec.status());
            printer.println("]");
            printer.println("Size: " + (decoded != null ? decoded.length : 0) + " bytes");
            return;
        }
        printer.println("Unrecognized codec " + Hex.toString(decoded) + "(" + UVarInt.decode(decoded) + ").");
    }
}