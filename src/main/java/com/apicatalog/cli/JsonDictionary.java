package com.apicatalog.cli;

import java.io.InputStream;

import com.apicatalog.cborld.document.DocumentDictionary;
import com.apicatalog.cborld.document.DocumentDictionaryBuilder;
import com.apicatalog.jsonld.json.JsonProvider;

import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.stream.JsonParser;

public class JsonDictionary {

    public static DocumentDictionary of(InputStream is) {

        var json = parse(is);

        var builder = DocumentDictionaryBuilder.create(json.getInt("code"));

        for (var item : json.entrySet()) {
            switch (item.getKey()) {
            case "code":
                continue;
            case "context":
                item.getValue().asJsonObject().entrySet()
                        .forEach(e -> builder.context(
                                ((JsonNumber) e.getValue()).intValue(),
                                e.getKey()));
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
        try (final JsonParser parser = JsonProvider.instance().createParser(json)) {
            return parser.getObject();
        }
    }
}
