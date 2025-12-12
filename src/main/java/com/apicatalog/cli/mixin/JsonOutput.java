package com.apicatalog.cli.mixin;

import java.io.PrintWriter;
import java.util.Map;

import jakarta.json.Json;
import jakarta.json.JsonStructure;
import jakarta.json.stream.JsonGenerator;
import picocli.CommandLine.Option;

public class JsonOutput {

    @Option(names = { "-p", "--pretty" }, description = "Pretty-print the output JSON.")
    public boolean pretty = false;

    public final void print(PrintWriter writer, JsonStructure document) {
        if (!pretty) {
            writer.println(document.toString());
            return;
        }

        final var writerFactory = Json.createWriterFactory(Map.of(JsonGenerator.PRETTY_PRINTING, true));

        try (final var jsonWriter = writerFactory.createWriter(writer)) {
            jsonWriter.write(document);
        }
        writer.println();
        writer.flush();
    }
}
