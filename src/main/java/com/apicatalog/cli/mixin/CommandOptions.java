package com.apicatalog.cli.mixin;

import java.net.URI;

import picocli.CommandLine.Option;

public class CommandOptions {

    @Option(names = { "-h", "--help" }, hidden = true, usageHelp = true, description = "Display help message.")
    boolean help = false;

    @Option(names = "--debug", description = "Print detailed error information.")
    public boolean debug = false;
    
    @Option(names = { "-i", "--input" }, description = "Input document URI or file path.", paramLabel = "<uri|file>")
    public URI input = null;

}
