package com.codingstory.polaris.parser;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SourceAnnotator {
    private static final Comparator<Usage> USAGE_COMPARATOR = new Comparator<Usage>() {
        @Override
        public int compare(Usage left, Usage right) {
            return left.getJumpTarget().getSpan().compareTo(right.getJumpTarget().getSpan());
        }
    };

    public static String annotate(InputStream in, List<Usage> usages) throws IOException {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        List<Usage> sortedUsages = Lists.newArrayList(usages);
        Collections.sort(sortedUsages, USAGE_COMPARATOR);
        pw.print("<source>");
        sortedUsages= ImmutableList.copyOf(Iterables.filter(sortedUsages, new Predicate<Usage>() {
            @Override
            public boolean apply(Usage usage) {
                return accepts(usage);
            }
        }));

        PositionAwareInputStream in2 = new PositionAwareInputStream(in);
        int ch = in2.read();
        int j = 0;
        while (ch != -1 && j < sortedUsages.size()) {
            Usage currentUsage = sortedUsages.get(j);
            Position currentUsageFrom = currentUsage.getJumpTarget().getSpan().getFrom();
            Position currentPosition = in2.getPosition();
            int cmp = currentPosition.compareTo(currentUsageFrom);
            if (cmp < 0) {
                pw.write(escape((char) ch));
                ch = in2.read();
            } else if (cmp > 0) {
                j++;
            } else {
                Position currentUsageTo = currentUsage.getJumpTarget().getSpan().getTo();
                StringBuilder text = new StringBuilder();
                while (in2.getPosition().compareTo(currentUsageTo) < 0 && ch != 1) {
                    text.append(ch);
                    ch = in2.read();
                }
                emit(pw, new String(text), currentUsage);
            }
        }
        while (ch != -1) {
            pw.write(escape((char) ch));
            ch = in2.read();
        }
        pw.print("</source>");
        pw.close();
        return sw.toString();
    }

    private static boolean accepts(Usage usage) {
        if (usage instanceof TypeUsage) {
            TypeUsage typeUsage = (TypeUsage) usage;
            // Type declaration's span is buggy. Don't generate tags for them until we move to antlr parser.
            return typeUsage.getKind() != TypeUsage.Kind.TYPE_DECLARATION;
        } else {
            return false;
        }
    }

    private static void emit(PrintWriter out, String text, Usage usage) {
        if (usage instanceof TypeUsage) {
            TypeUsage typeUsage = (TypeUsage) usage;
            TypeHandle type = typeUsage.getType();
            if (!TypeUtils.isPrimitiveTypeHandle(type)) {
                String typeNameStr = type.getName().toString();
                out.printf("<type-usage type=\"%s\" type-id=\"%d\" resolved=\"%s\">%s</type-usage>",
                        escape(typeNameStr), type.getId(), Boolean.toString(type.isResolved()), escape(text));
                return;
            }
            // Don't show links for primitive or unresolved types.
            out.print(escape(text));
        }
    }

    private static String escape(String s) {
        StringBuffer t = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            t.append(escape(ch));
        }
        return t.toString();
    }

    private static String escape(char ch) {
        if (ch == '&') {
            return "&amp;";
        } else if (ch == '<') {
            return "&lt;";
        } else if (ch == '>') {
            return "&gt;";
        } else if (ch == '\"') {
            return "&quot;";
        } else if (ch == '\'') {
            return "&apos;";
        } else {
            return String.valueOf(ch);
        }
    }

    private static class PositionAwareInputStream extends InputStream {
        private InputStream in;
        private int line;
        private int column;

        private PositionAwareInputStream(InputStream in) {
            this.in = Preconditions.checkNotNull(in);
        }

        @Override
        public int read() throws IOException {
            int ch = in.read();
            if (ch < 0) {
                return ch;
            }
            if (ch == '\n') {
                line++;
                line = 0;
            } else {
                column++;
            }
            return ch;
        }

        public Position getPosition() {
            return new Position(line, column);
        }
    }
}
