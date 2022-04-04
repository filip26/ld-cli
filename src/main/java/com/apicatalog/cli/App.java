package com.apicatalog.cli;

import com.apicatalog.jsonld.http.media.MediaType;
import com.apicatalog.jsonld.loader.HttpLoader;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;

@Command(
    name = "jsonld",
    description = "JSON-LD 1.1 Command Line Processor",
    subcommands = { ExpandCmd.class },
    mixinStandardHelpOptions = false,
    descriptionHeading = "%n",
    parameterListHeading = "%nParameters:%n",
    optionListHeading = "%nOptions:%n",
    commandListHeading = "%nCommands:%n",
    version = {
            "json-ld-cli       0.0.1  https://github.com/filip26/json-ld-cli",
            "titanium-json-ld  1.2.0  https://github.com/filip26/titanium-json-ld"
            }
    )
public final class App {

    @Option(names = { "-h", "--help" }, usageHelp = true, description = "display a help message")
    boolean help = false;

    @Option(names = {"-v", "--version"}, versionHelp = true, description = "display a version")
    boolean version;    

    static {
        ((HttpLoader) HttpLoader.defaultInstance()).setFallbackContentType(MediaType.JSON);
    }
    
    public static void main(String[] args) {

        final CommandLine cli = new CommandLine(new App());
        cli.setCaseInsensitiveEnumValuesAllowed(true);

        try {

            final ParseResult result = cli.parseArgs(args);

            if (cli.isUsageHelpRequested()) {

                if (result.subcommand() != null) {
                    result.subcommand().commandSpec().commandLine().usage(cli.getOut());
                    return;
                }

                cli.usage(cli.getOut());
                System.exit(cli.getCommandSpec().exitCodeOnUsageHelp());
                return;
            }

            if (cli.isVersionHelpRequested()) {
                cli.printVersionHelp(cli.getOut());
                System.exit(cli.getCommandSpec().exitCodeOnVersionHelp());
                return;
            }

            System.exit(cli.execute(args));

        } catch (Exception ex) {
            cli.getErr().println(ex.getMessage());
            System.exit(cli.getCommandSpec().exitCodeOnExecutionException());
        }
    }
}
