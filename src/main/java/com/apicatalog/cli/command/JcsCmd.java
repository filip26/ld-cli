package com.apicatalog.cli.command;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import com.apicatalog.jcs.JsonCanonicalizer;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.jsonld.loader.SchemeRouter;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "jcs", mixinStandardHelpOptions = false, description = "Canonize a JSON document using the JSON Canonicalization Scheme (JCS).", sortOptions = true, descriptionHeading = "%n", parameterListHeading = "%nParameters:%n", optionListHeading = "%nOptions:%n")
public final class JcsCmd implements Callable<Integer> {

    @Option(names = { "-h", "--help" }, hidden = true, usageHelp = true)
    boolean help = false;

    @Option(names = { "-i", "--input" }, description = "Input document URI or file path.", paramLabel = "<uri>")
    URI input = null;

    @Spec
    CommandSpec spec;

    private JcsCmd() {
    }

    @Override
    public Integer call() throws Exception {

        final Document document;

        if (input != null) {
            if (input.isAbsolute()) {
                var loader = SchemeRouter.defaultInstance();
                document = loader.loadDocument(input, new DocumentLoaderOptions());

            } else {
                try (final Reader reader = Files.newBufferedReader(Path.of(input.toString()), StandardCharsets.UTF_8)) {
                    document = JsonDocument.of(reader);
                }
            }

        } else {
            try (final Reader reader = new InputStreamReader(System.in)) {
                document = JsonDocument.of(reader);
            }
        }

        try (final Writer writer = new OutputStreamWriter(System.out, StandardCharsets.UTF_8)) {
            JsonCanonicalizer.canonize(
                    document.getJsonContent()
                            .orElseThrow(() -> new IllegalArgumentException("Invalid input document. JSON document expected but got [" + document.getContentType() + "].")),
                    writer);
            writer.flush();

        } catch (Exception e) {
            e.printStackTrace();
            return spec.exitCodeOnExecutionException();
        }

        return spec.exitCodeOnSuccess();
    }
}