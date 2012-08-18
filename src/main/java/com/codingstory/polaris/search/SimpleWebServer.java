package com.codingstory.polaris.search;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.Map;

public class SimpleWebServer {

    public static class SearchServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            Preconditions.checkNotNull(req);
            Preconditions.checkNotNull(resp);
            String query = req.getParameter("q");

            if (Strings.isNullOrEmpty(query)) {
                showSearchForm(resp);
            } else {
                List<Result> results = search(query);
                showSearchResults(results, resp);
            }
        }

        private List<Result> search(String query) throws IOException, ServletException {
            try {
                SrcSearcher searcher = new SrcSearcher("index");
                List<Result> results = searcher.search(query, 100);
                return results;
            } catch (ParseException e) {
                throw new ServletException(e);
            } catch (InvalidTokenOffsetsException e) {
                throw new ServletException(e);
            }
        }

        private void showSearchForm(HttpServletResponse resp) throws IOException {
            InputStream in = SearchServlet.class.getResourceAsStream("/SearchForm.html");
            OutputStream out = resp.getOutputStream();
            try {
                IOUtils.copy(in, out);
            } finally {
                IOUtils.closeQuietly(in);
            }
            out.flush();
        }

        private void showSearchResults(List<Result> results, HttpServletResponse resp) throws ServletException, IOException {
            InputStream in = SearchServlet.class.getResourceAsStream("/SearchResult.ftl");
            try {
                Configuration conf = new Configuration();
                conf.setClassForTemplateLoading(SearchServlet.class, "/");
                Template template = conf.getTemplate("SearchResult.ftl");
                Map<String, Object> root = Maps.newHashMap();
                root.put("results", results);
                Writer out = resp.getWriter();
                template.process(root, out);
                out.flush();
            } catch (TemplateException e) {
                throw new ServletException(e);
            } finally {
                IOUtils.closeQuietly(in);
            }
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            doGet(req, resp);
        }
    }

    public static class HelloServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            Preconditions.checkNotNull(req);
            Preconditions.checkNotNull(resp);
            PrintWriter out = resp.getWriter();
            out.println("<html><body><h1>Hello!</h1></body></html>");
            out.flush();
        }
    }

    private int port = 0;

    public void setPort(int port) {
        this.port = port;
    }

    public void run() throws InterruptedException, IOException {
        Server server = new Server(port);
        ServletContextHandler contextHandler = new ServletContextHandler();
        contextHandler.addServlet(SearchServlet.class, "/");
        contextHandler.addServlet(HelloServlet.class, "/hello");
        server.setHandler(contextHandler);
        try {
            server.start();
        } catch (Exception e) {
            throw new IOException(e); // Just wrap it
        }
        server.join();
    }

}