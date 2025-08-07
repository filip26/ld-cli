package com.apicatalog.cli;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import com.apicatalog.cborld.registry.DocumentDictionary;
import com.apicatalog.cborld.registry.DocumentDictionaryBuilder;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.jsonld.loader.SchemeRouter;

import jakarta.json.Json;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.stream.JsonParser;

public class JsonCborDictionary {

    public static DocumentDictionary of(URI input) throws IOException, JsonLdError {
        if (input.isAbsolute()) {
            final DocumentLoader loader = SchemeRouter.defaultInstance();
            return of(loader.loadDocument(input, new DocumentLoaderOptions()));
        }
        return of(new ByteArrayInputStream(Files.readAllBytes(Path.of(input.toString()))));
    }

    public static DocumentDictionary of(Document doc) {
        return of(doc.getJsonContent().orElseThrow(() -> new IllegalArgumentException("Invalid dictionary")).asJsonObject());
    }

    public static DocumentDictionary of(InputStream is) {
        return of(parse(is));
    }

    public static DocumentDictionary of(JsonObject json) {
        var builder = DocumentDictionaryBuilder.create(json.getInt("code"));

        for (var item : json.entrySet()) {
            switch (item.getKey()) {
            case "code":
                continue;
            case "context":
                item.getValue().asJsonObject().entrySet()
                        .forEach(e -> builder.context(e.getKey(),
                                ((JsonNumber) e.getValue()).intValue()));
            default:
                item.getValue().asJsonObject().entrySet()
                        .forEach(e -> builder.type(
                                item.getKey(),
                                ((JsonNumber) e.getValue()).intValue(),
                                e.getKey()));
            }
        }

        return builder.build();
    }

    public static JsonObject parse(InputStream json) {
        try (final JsonParser parser = Json.createParser(json)) {

            if (!parser.hasNext()) {
                throw new IllegalArgumentException("Invalid dictionary definition");
            }

            parser.next();

            return parser.getObject();
        }
    }
}
