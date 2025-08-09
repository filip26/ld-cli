package com.apicatalog.cli.mixin;

import picocli.CommandLine.Option;

public class CommandOptions {

    @Option(names = { "-h", "--help" }, hidden = true, usageHelp = true, description = "Displays a help message about a command.")
    boolean help = false;

    @Option(names = "--debug", description = "Print detailed error information.")
    public boolean debug = false;
}
