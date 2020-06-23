package com.marklogic.hubstarter.client;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.net.ssl.SSLContext;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.hubstarter.auth.SimpleX509TrustManager;
import com.marklogic.hubstarter.ds.Movies;

/**
 * Runs a search against the movieSearch data service. Specify the search string
 * as the first and only argument.
 */
public class MovieServiceClient {

    protected String host;
    protected int port;
    protected String user;
    protected String password;
    protected boolean dhs;

    public MovieServiceClient(String host, int port, String user, String password, boolean dhs) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.dhs = dhs;
    }

    public void search(String searchString) throws Exception {
        DatabaseClient client = createDbClient();

        try {
            Movies moviesService = Movies.on(client);

            long t1 = System.currentTimeMillis();
            ObjectNode results = moviesService.searchMovies(searchString);
            long t2 = System.currentTimeMillis();

            System.out.println(results.toString());
            System.out.println("\nElapsed millis: " + (t2- t1));
        } finally {
            if (client != null) {
                client.release();
            }
        }
    }

    DatabaseClient createDbClient() throws NoSuchAlgorithmException, KeyManagementException {
        if (dhs) {
            SSLContext sslContext = SimpleX509TrustManager.newSSLContext();

            return DatabaseClientFactory.newClient(host, port,
                    new DatabaseClientFactory.BasicAuthContext(user, password).withSSLContext(sslContext,
                            new SimpleX509TrustManager()),
                    DatabaseClient.ConnectionType.GATEWAY);
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

        MovieServiceClient searcher = new MovieServiceClient(
            props.getProperty("host", "localhost"),
            Integer.parseInt(props.getProperty("port", "8011")), 
            props.getProperty("username"),
            props.getProperty("password"), 
            Boolean.parseBoolean(props.getProperty("dhs", "false"))
        );

        System.out.println("Running using the following configuration:");
        System.out.format("ML host: %s\nport: %d\nUsername: %s\n", searcher.host, searcher.port, searcher.user);

        searcher.search(searchString);

        System.exit(0);
    }
}
