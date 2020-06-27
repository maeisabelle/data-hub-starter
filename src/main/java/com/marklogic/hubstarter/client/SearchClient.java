package com.marklogic.hubstarter.client;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.io.SearchHandle;
import com.marklogic.client.io.SearchHandle.Report;
import com.marklogic.client.query.MatchDocumentSummary;
import com.marklogic.client.query.MatchLocation;
import com.marklogic.client.query.MatchSnippet;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StructuredQueryBuilder;
import com.marklogic.client.query.StructuredQueryDefinition;
import com.marklogic.hubstarter.auth.SimpleX509TrustManager;

/**
 * Runs a search using the MarkLogic search API using a given search options
 * name. The search options must already be installed into the modules database
 * before using this. If no search options are specified, the "default" options
 * will be used.
 */
public class SearchClient {

    protected String host;
    protected int port;
    protected String user;
    protected String password;
    protected boolean dhs;
    protected String searchOptions;

    public SearchClient(String host, int port, String user, String password, boolean dhs, String searchOptions) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.dhs = dhs;
        this.searchOptions = searchOptions;
    }

    public void search(String searchString) throws Exception {
        DatabaseClient client = createDbClient();

        try {
            QueryManager queryMgr = client.newQueryManager();
            StructuredQueryBuilder qb = new StructuredQueryBuilder(searchOptions);

            // we can expand the capability here to query based on different parameters
            // for now, just query for the given search string as a word/phrase
            StructuredQueryDefinition querydef = qb.and(qb.term(searchString));

            long t1 = System.currentTimeMillis();
            SearchHandle results = queryMgr.search(querydef, new SearchHandle());
            long t2 = System.currentTimeMillis();

            printResults(results, querydef);

            System.out.println("\nElapsed millis: " + (t2 - t1));
        } finally {
            if (client != null) {
                client.release();
            }
        }
    }

    protected void printResults(SearchHandle resultsHandle, StructuredQueryDefinition querydef) {
        System.out.println("Matched " + resultsHandle.getTotalResults() + " documents\n");

        // iterate over the result documents
        MatchDocumentSummary[] docSummaries = resultsHandle.getMatchResults();
        System.out.println("Listing " + docSummaries.length + " documents:\n");
        for (MatchDocumentSummary docSummary : docSummaries) {
            String uri = docSummary.getUri();
            int score = docSummary.getScore();

            // iterate over the match locations within a result document
            MatchLocation[] locations = docSummary.getMatchLocations();
            System.out.println("Matched " + locations.length + " locations in " + uri + " with " + score + " score:");
            for (MatchLocation location : locations) {

                // iterate over the snippets at a match location
                for (MatchSnippet snippet : location.getSnippets()) {
                    boolean isHighlighted = snippet.isHighlighted();

                    if (isHighlighted)
                        System.out.print("[");
                    System.out.print(snippet.getText());
                    if (isHighlighted)
                        System.out.print("]");
                }
                System.out.println();
            }
        }

        for (Report report : resultsHandle.getReports()) {
            System.out.println("report: " + report);
        }
    }

    DatabaseClient createDbClient() throws NoSuchAlgorithmException, KeyManagementException {
        if (dhs) {
            X509TrustManager trustManager = new SimpleX509TrustManager();
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, new TrustManager[] { trustManager }, null);

            return DatabaseClientFactory.newClient(
                host, port,
                new DatabaseClientFactory.BasicAuthContext(user, password).withSSLContext(sslContext, trustManager),
                DatabaseClient.ConnectionType.GATEWAY
            );
        } else {
            return DatabaseClientFactory.newClient(host, port,
                    new DatabaseClientFactory.DigestAuthContext(user, password));
        }
    }

    public static void main(final String[] args) throws Exception {
        Properties props = new Properties();
        List<String> remainingArgs = new ArrayList<String>();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--")) {
                props.setProperty(arg.substring(2), args[++i]);
            } else {
                remainingArgs.add(arg);
            }
        }

        String searchString = remainingArgs.get(0);

        SearchClient searchClient = new SearchClient(props.getProperty("host", "localhost"),
                Integer.parseInt(props.getProperty("port", "8011")), props.getProperty("username"),
                props.getProperty("password"), Boolean.parseBoolean(props.getProperty("dhs", "false")),
                props.getProperty("searchOption", "default"));

        System.out.println("Running using the following configuration:");
        System.out.format("ML host: %s\nport: %d\nUsername: %s\n", searchClient.host, searchClient.port,
                searchClient.user);

        searchClient.search(searchString);

        System.exit(0);
    }
}
