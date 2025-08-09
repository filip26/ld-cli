package com.apicatalog.cli.mixin;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.jsonld.loader.SchemeRouter;

import picocli.CommandLine.Option;

public class JsonInput {

    @Option(names = { "-i", "--input" }, description = "Input document URI or file path.", paramLabel = "<uri|file>")
    public URI input = null;

    public JsonDocument fetch() throws JsonLdError, IOException {
        if (input.isAbsolute()) {
            final DocumentLoader loader = SchemeRouter.defaultInstance();
            return (JsonDocument) loader.loadDocument(input, new DocumentLoaderOptions());
        }
        return JsonDocument.of(new ByteArrayInputStream(Files.readAllBytes(Path.of(input.toString()))));
    }
}
