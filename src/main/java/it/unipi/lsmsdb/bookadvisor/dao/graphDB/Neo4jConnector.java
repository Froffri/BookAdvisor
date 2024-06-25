package it.unipi.lsmsdb.bookadvisor.dao.graphDB;

import org.neo4j.driver.*;

public class Neo4jConnector {
    private static final String URI = "bolt://localhost:7687";
    private static final String USERNAME = "neo4j";
    private static final String PASSWORD = "password";

    private Driver driver;

    public Neo4jConnector() {
        this.driver = GraphDatabase.driver(URI, AuthTokens.basic(USERNAME, PASSWORD));
    }

    public Driver getDriver() {
        return driver;
    }

    public Session getSession() {
        return driver.session();
    }

    public void close() {
        driver.close();
    }
}
