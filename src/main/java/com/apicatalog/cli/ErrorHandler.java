package com.apicatalog.cli;

import com.apicatalog.cli.mixin.CommandOptions;

import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.ParseResult;

public class ErrorHandler implements IExecutionExceptionHandler {
    @Override
    public int handleExecutionException(Exception ex,
            CommandLine cmd,
            ParseResult parseResult) {

        cmd.getErr().println(ex.getMessage());

        var options = ((CommandOptions) cmd.getMixins().get("options"));

        if (options.debug) {
            ex.printStackTrace(cmd.getErr());
        }

        return cmd.getCommandSpec().exitCodeOnExecutionException();
    }
}