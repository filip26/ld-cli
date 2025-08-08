package com.apicatalog.cli.command;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import com.apicatalog.cli.mixin.CommandOptions;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.http.media.MediaType;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.jsonld.loader.HttpLoader;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.rdf.RdfResource;
import com.apicatalog.rdf.canon.RdfCanon;
import com.apicatalog.rdf.canon.RdfCanonTicker;
import com.apicatalog.rdf.canon.RdfCanonTimeTicker;
import com.apicatalog.rdf.nquads.NQuadsAlphabet;
import com.apicatalog.rdf.nquads.NQuadsReader;
import com.apicatalog.rdf.nquads.NQuadsWriter;

import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "rdfc", mixinStandardHelpOptions = false, description = "Canonize an RDF N-Quads document using the RDFC-1.0 algorithm.", sortOptions = true, descriptionHeading = "%n", parameterListHeading = "%nParameters:%n", optionListHeading = "%nOptions:%n")
public final class RdfCanonCmd implements Callable<Integer> {

    @Option(names = { "-i", "--input" }, description = "Input document URI or file path.", paramLabel = "<uri|file>")
    public URI input = null;

    @Option(names = { "-t", "--timeout" }, description = "Timeout in milliseconds (default: 10000 = 10s). Terminates processing after the specified time.", paramLabel = "<milliseconds>")
    long timeout = 10 * 1000;

    @Option(names = { "-d", "--digest" }, description = "Digest algorithm to use.", paramLabel = "SHA256|SHA384")
    String digest = "SHA256";

    @Mixin
    CommandOptions options;

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

                ((HttpLoader) HttpLoader.defaultInstance()).fallbackContentType(MediaType.N_QUADS);

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

        canon.provide(new NQuadsWriter(spec.commandLine().getOut()));
        spec.commandLine().getOut().flush();

        return spec.exitCodeOnSuccess();
    }
}