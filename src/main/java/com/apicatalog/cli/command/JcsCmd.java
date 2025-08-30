package com.apicatalog.cli.command;

import java.util.concurrent.Callable;

import com.apicatalog.cli.mixin.CommandOptions;
import com.apicatalog.cli.mixin.JsonInput;
import com.apicatalog.jcs.JsonCanonicalizer;
import com.apicatalog.jsonld.document.Document;

import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "jcs", mixinStandardHelpOptions = false, description = "Canonize a JSON document using the JSON Canonicalization Scheme (JCS).", sortOptions = true, descriptionHeading = "%n", parameterListHeading = "%nParameters:%n", optionListHeading = "%nOptions:%n")
public final class JcsCmd implements Callable<Integer> {

    @Mixin
    JsonInput input;

    @Mixin
    CommandOptions options;

    @Spec
    CommandSpec spec;

    private JcsCmd() {
    }

    @Override
    public Integer call() throws Exception {

        final Document document = input.fetch();

        JsonCanonicalizer.canonize(
                document.getJsonContent()
                        .orElseThrow(() -> new IllegalArgumentException("Invalid input document. JSON document expected but got [" + document.getContentType() + "].")),
                spec.commandLine().getOut());

        spec.commandLine().getOut().flush();

        return spec.exitCodeOnSuccess();
    }
}