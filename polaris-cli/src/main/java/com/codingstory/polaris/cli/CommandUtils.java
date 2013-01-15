package com.codingstory.polaris.cli;

import com.codingstory.polaris.indexing.IndexBuilder;
import com.codingstory.polaris.parser.ParserOptions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.transport.TSocket;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CommandUtils {
    private CommandUtils() {}

    private static final Pattern ADDRESS_PATTERN = Pattern.compile("([A-Za-z0-9\\.]+):([0-9]+)");
    private static final Log LOG = LogFactory.getLog(CommandUtils.class);

    public static void die(String message) {
        System.err.println("polaris: " + message);
        System.exit(1);
    }

    public static TSocket openSocket(String address) {
        Matcher m = ADDRESS_PATTERN.matcher(address);
        if (!m.matches()) {
            throw new IllegalArgumentException("Address: " + address);
        }
        String host = m.group(1);
        int port = Integer.parseInt(m.group(2));
        LOG.info("Connect to " + host + ":" + port);
        return new TSocket(host, port);
    }

    public static IndexBuilder createIndexer(File output) throws IOException {
        ParserOptions parserOptions = new ParserOptions();
        parserOptions.setFailFast(false);
        IndexBuilder indexBuilder = new IndexBuilder();
        indexBuilder.setIndexDirectory(output);
        indexBuilder.setParserOptions(parserOptions);
        return indexBuilder;
    }
}