package com.apicatalog.cli.command;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import com.apicatalog.base.Base16;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.rdf.RdfDataset;
import com.apicatalog.rdf.canon.RdfCanon;
import com.apicatalog.rdf.canon.RdfCanonTicker;
import com.apicatalog.rdf.canon.RdfCanonTimeTicker;
import com.apicatalog.rdf.nquads.NQuadsReader;

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

    @Option(names = { "-o", "--output" }, description = "output document filename")
    String output = null;

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
        if ("SHA384".equals(digest)) {
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
                RdfDataset x = document.getRdfContent()
                        .orElseThrow(() -> new IllegalArgumentException("Invalid input document. N-QUADS document expected but got [" + document.getContentType() + "]."));
                x.toList().forEach(s -> {
//                    canon.quad(s.get, hashAlgo, hashAlgo, hashAlgo, hashAlgo, hashAlgo, hashAlgo)
                });
                
            } else {
                try (final Reader reader = Files.newBufferedReader(Path.of(input.toString()), StandardCharsets.UTF_8)) {
                    new NQuadsReader(reader).provide(canon);
                }
            }

        } else {
            try (final Reader reader = new InputStreamReader(System.in)) {
                new NQuadsReader(reader).provide(canon);
            };
        }
        
//
//        if (JsonUtils.isNotObject(json)) {
//            throw new IllegalArgumentException("The input docunent root is not JSON object but [" + json.getValueType() + "].");
//        }
//
//        var config = switch (mode) {
//        case "v05" -> V05Config.INSTANCE;
//        default -> DefaultConfig.INSTANCE;
//        };
//
//        var encoder = CborLd.createEncoder(config)
//                .base(base)
//                .compactArray(!timeout);
//
//        if (dictionary != null) {
//            encoder.dictionary(JsonCborDictionary.of(dictionary));
//        }
//
//        var encoded = encoder
//                .build()
//                .encode(json.asJsonObject());
//
//        if (output == null) {
//            System.out.write(encode(encoded, true));
//
//        } else {
//            try (var os = new FileOutputStream(output)) {
//                os.write(encode(encoded, hex));
//                os.flush();
//            }
//        }

        return spec.exitCodeOnSuccess();
    }

    static byte[] encode(byte[] encoded, boolean hex) throws IOException {
        return hex
                ? Base16.encode(encoded, Base16.ALPHABET_LOWER).getBytes()
                : encoded;
    }

    static final String toString(byte value) {
        return String.format("%02x", value);
    }
}