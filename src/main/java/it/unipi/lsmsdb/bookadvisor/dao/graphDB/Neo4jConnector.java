package it.unipi.lsmsdb.bookadvisor.dao.graphDB;

import org.neo4j.driver.*;

public class Neo4jConnector {
    private static final String URI = "bolt://10.1.1.23:7687";
    // private static final String URI = "bolt://localhost:7687";
    private static final String USERNAME = "neo4j";
    private static final String PASSWORD = "studenti";

    private Driver driver;

    public Neo4jConnector() {
        try {
            driver = GraphDatabase.driver(URI, AuthTokens.basic(USERNAME, PASSWORD));
        } catch (Exception e) {
            throw new RuntimeException("Error initializing Neo4j connection: " + e.getMessage(), e);
        }
    }

    public static Neo4jConnector getInstance() {
        return new Neo4jConnector();
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
