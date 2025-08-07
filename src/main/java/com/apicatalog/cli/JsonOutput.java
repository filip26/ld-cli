package com.apicatalog.cli;

import java.io.PrintWriter;
import java.util.Collections;

import jakarta.json.Json;
import jakarta.json.JsonStructure;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;

public class JsonOutput {

    public static final void print(PrintWriter writer, JsonStructure document, boolean pretty) {
        if (!pretty) {
            writer.println(document.toString());
            return;
        }

        final JsonWriterFactory writerFactory = Json
                .createWriterFactory(Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true));

        try (final JsonWriter jsonWriter = writerFactory.createWriter(writer)) {
            jsonWriter.write(document);
        }
        writer.flush();
    }
}
