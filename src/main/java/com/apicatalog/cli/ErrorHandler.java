package com.apicatalog.cli;

import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.ParseResult;

public class ErrorHandler implements IExecutionExceptionHandler {
    @Override
    public int handleExecutionException(Exception ex,
            CommandLine cmd,
            ParseResult parseResult) {

        cmd.getErr().println(ex.getMessage());

        boolean debug = parseResult != null && Boolean.TRUE.equals(parseResult.matchedOptionValue("debug", Boolean.FALSE));

        if (debug) {
            ex.printStackTrace(cmd.getErr());
        }

        return cmd.getCommandSpec().exitCodeOnExecutionException();
    }
}