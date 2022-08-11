package com.apicatalog.cli;

import java.io.StringWriter;
import java.util.Collections;

import jakarta.json.Json;
import jakarta.json.JsonStructure;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;

public class JsonOutput {

    public static final void print(JsonStructure document, boolean pretty) {
        if (!pretty) {
            System.out.println(document.toString());
            return;
        }

        final JsonWriterFactory writerFactory = Json
                .createWriterFactory(Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true));

        final StringWriter stringWriter = new StringWriter();

        try (final JsonWriter jsonWriter = writerFactory.createWriter(stringWriter)) {
            jsonWriter.write(document);
        }

        System.out.println(stringWriter.toString());
    }
}
