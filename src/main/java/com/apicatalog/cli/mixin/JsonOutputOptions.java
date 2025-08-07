package com.apicatalog.cli.mixin;

import picocli.CommandLine.Option;

public class JsonOutputOptions {

    @Option(names = { "-p", "--pretty" }, description = "Pretty-print the output JSON.")
    public boolean pretty = false;
}
