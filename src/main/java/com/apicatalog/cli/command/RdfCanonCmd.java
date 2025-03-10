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

import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.rdf.RdfResource;
import com.apicatalog.rdf.canon.RdfCanon;
import com.apicatalog.rdf.canon.RdfCanonTicker;
import com.apicatalog.rdf.canon.RdfCanonTimeTicker;
import com.apicatalog.rdf.nquads.NQuadsAlphabet;
import com.apicatalog.rdf.nquads.NQuadsReader;
import com.apicatalog.rdf.nquads.NQuadsWriter;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "rdfc", mixinStandardHelpOptions = false, description = "Canonicalize an RDF N-Quads document with RDFC-1.0", sortOptions = true, descriptionHeading = "%n", parameterListHeading = "%nParameters:%n", optionListHeading = "%nOptions:%n")
public final class RdfCanonCmd implements Callable<Integer> {

    @Option(names = { "-h", "--help" }, hidden = true, usageHelp = true)
    boolean help = false;

    @Option(names = { "-i", "--input" }, description = "input document IRI or filepath")
    URI input = null;

//    @Option(names = { "-o", "--output" }, description = "output document filename")
//    String output = null;

    @Option(names = { "-t", "--timeout" }, description = "terminates after the specified time in milliseconds (default: 10s)")
    long timeout = 10 * 1000;

    @Option(names = { "-d", "--digest" }, description = "the name of the hash algorithm to use", paramLabel = "SHA256|SHA384")
    String digest = "SHA256";

    @Spec
    CommandSpec spec;

    private RdfCanonCmd() {
    }

    @Override
    public Integer call() throws Exception {

        String hashAlgo = "SHA-256";
        if ("SHA384".equalsIgnoreCase(digest)) {
            hashAlgo = "SHA-384";
        }

        RdfCanonTicker ticker = timeout > 0
                ? new RdfCanonTimeTicker(timeout)
                : RdfCanonTicker.EMPTY;

        RdfCanon canon = RdfCanon.create(hashAlgo, ticker);

        if (input != null) {
            if (input.isAbsolute()) {
                var loader = SchemeRouter.defaultInstance();
                Document document = loader.loadDocument(input, new DocumentLoaderOptions());
                document.getRdfContent()
                        .orElseThrow(() -> new IllegalArgumentException("Invalid input document. N-QUADS document expected but got [" + document.getContentType() + "]."))
                        .toList().forEach(s -> {
                            if (s.getObject().isLiteral()) {
                                var literal = s.getObject().asLiteral();

                                var datatype = literal.getDatatype();
                                var language = literal.getLanguage().orElse(null);
                                String direction = null;

                                if (datatype.startsWith(NQuadsAlphabet.I18N_BASE)) {

                                    datatype = NQuadsAlphabet.I18N_BASE;

                                    String[] langDir = datatype.substring(NQuadsAlphabet.I18N_BASE.length()).split("_");

                                    if (langDir.length > 1) {
                                        direction = langDir[1];
                                    }
                                    if (langDir.length > 0) {
                                        language = langDir[0];
                                    }
                                }

                                canon.quad(
                                        s.getSubject().getValue(),
                                        s.getPredicate().getValue(),
                                        s.getObject().getValue(),
                                        datatype,
                                        language,
                                        direction,
                                        s.getGraphName().map(RdfResource::getValue).orElse(null));
                                return;
                            }
                            canon.quad(
                                    s.getSubject().getValue(),
                                    s.getPredicate().getValue(),
                                    s.getObject().getValue(),
                                    null,
                                    null,
                                    null,
                                    s.getGraphName().map(RdfResource::getValue).orElse(null));
                        });

            } else {
                try (final Reader reader = Files.newBufferedReader(Path.of(input.toString()), StandardCharsets.UTF_8)) {
                    new NQuadsReader(reader).provide(canon);
                }
            }

        } else {
            try (final Reader reader = new InputStreamReader(System.in)) {
                new NQuadsReader(reader).provide(canon);
            }
        }

        try (final Writer writer = new OutputStreamWriter(System.out, StandardCharsets.UTF_8)) {
            canon.provide(new NQuadsWriter(writer));
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
            return spec.exitCodeOnExecutionException();
        }

        return spec.exitCodeOnSuccess();
    }
}