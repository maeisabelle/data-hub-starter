package com.marklogic.hubstarter.ds;

// IMPORTANT: Do not edit. This file is generated.

import com.marklogic.client.io.Format;
import com.marklogic.client.io.marker.AbstractWriteHandle;


import com.marklogic.client.DatabaseClient;

import com.marklogic.client.impl.BaseProxy;

/**
 * Provides a set of operations on the database server
 */
public interface Movies {
    /**
     * Creates a Movies object for executing operations on the database server.
     *
     * The DatabaseClientFactory class can create the DatabaseClient parameter. A single
     * client object can be used for any number of requests and in multiple threads.
     *
     * @param db	provides a client for communicating with the database server
     * @return	an object for session state
     */
    static Movies on(DatabaseClient db) {
        final class MoviesImpl implements Movies {
            private BaseProxy baseProxy;

            private MoviesImpl(DatabaseClient dbClient) {
                baseProxy = new BaseProxy(dbClient, "/ds/movies/");
            }

            @Override
            public com.fasterxml.jackson.databind.node.ObjectNode searchMovies(String searchString) {
              return BaseProxy.ObjectType.toObjectNode(
                baseProxy
                .request("movieSearch.sjs", BaseProxy.ParameterValuesKind.SINGLE_ATOMIC)
                .withSession()
                .withParams(
                    BaseProxy.atomicParam("searchString", false, BaseProxy.StringType.fromString(searchString)))
                .withMethod("POST")
                .responseSingle(false, Format.JSON)
                );
            }

        }

        return new MoviesImpl(db);
    }

  /**
   * Searches for movie documents matching the given query
   *
   * @param searchString	The string to search for
   * @return	A JSON object containing the search results.
   */
    com.fasterxml.jackson.databind.node.ObjectNode searchMovies(String searchString);

}
