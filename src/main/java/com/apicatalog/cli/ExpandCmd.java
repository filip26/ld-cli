package com.apicatalog.cli;

import java.util.concurrent.Callable;

import com.apicatalog.jsonld.JsonLd;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(
        name = "expand",
        mixinStandardHelpOptions = false,
        description =  "Expand JSON-LD 1.1 document",
        sortOptions = false,
        descriptionHeading = "%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n"
        )
final class ExpandCmd implements Callable<Integer> {

    @Option(names = { "-h", "--help" },  hidden = true, usageHelp = true)
    boolean help = false;

    @Parameters(index = "0", arity = "0..1", description = "input URL")
    String input;

    @Spec CommandSpec spec;

    private ExpandCmd() {
    }

    @Override
    public Integer call() throws Exception {
        
        System.out.println("Fetching " + input);
        System.out.println(JsonLd.expand(input).get().toString());
        
        return spec.exitCodeOnSuccess();
    }
}