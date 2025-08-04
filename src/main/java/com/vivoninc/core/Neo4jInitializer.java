package com.vivoninc.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

@Component
public class Neo4jInitializer {

    @Autowired
    private Neo4jClient neo4jClient;

    @PostConstruct
    @Transactional // Add transaction annotation
    public void init() {
        try {
            System.out.println("Starting Neo4j initialization...");
            
            // Test connection first
            testConnection();
            
            // Clear existing data (optional)
            clearDatabase();
            
            // Insert mock data
            insertMockData();
            
            // Verify data was inserted
            verifyData();
            
            System.out.println("Neo4j initialization completed successfully");
        } catch (Exception e) {
            System.err.println("Error during Neo4j initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void testConnection() {
        try {
            String result = neo4jClient.query("RETURN 'Connection successful' as message")
                .fetchAs(String.class)
                .mappedBy((typeSystem, record) -> record.get("message").asString())
                .one()
                .orElse("No result");
            System.out.println("Neo4j connection test: " + result);
        } catch (Exception e) {
            System.err.println("Neo4j connection failed: " + e.getMessage());
            throw e;
        }
    }
    
    private void clearDatabase() {
        try {
            System.out.println("Clearing existing data...");
            neo4jClient.query("MATCH (n) DETACH DELETE n").run();
            System.out.println("Database cleared");
        } catch (Exception e) {
            System.err.println("Error clearing database: " + e.getMessage());
        }
    }
    
    @Transactional
    private void insertMockData() {
        System.out.println("Inserting mock data...");
        
        // Insert users first
        String createUsers = """
            MERGE (a:User {id: 1})
            MERGE (b:User {id: 2})
            MERGE (c:User {id: 3})
            MERGE (d:User {id: 4})
            """;
        neo4jClient.query(createUsers).run();
        System.out.println("Users created");
        
        // Create group
        String createGroup = """
            MERGE (gr1:Group {id: 1})
            """;
        neo4jClient.query(createGroup).run();
        System.out.println("Group created");
        
        // Create relationships
        String createRelationships = """
            MATCH (c:User {id: 3}), (d:User {id: 4}), (gr1:Group {id: 1})
            MERGE (c)-[:IN_GROUP]->(gr1)
            MERGE (d)-[:IN_GROUP]->(gr1)
            """;
        neo4jClient.query(createRelationships).run();
        System.out.println("Group relationships created");
        
        String createFriendships = """
            MATCH (a:User {id: 1}), (b:User {id: 2}), (c:User {id: 3}), (d:User {id: 4})
            MERGE (a)-[:FRIEND]-(b)
            MERGE (b)-[:FRIEND]-(c)
            MERGE (d)-[:FRIEND_REQ]->(a)
            """;
        neo4jClient.query(createFriendships).run();
        System.out.println("Friend relationships created");
    }
    
    private void verifyData() {
        try {
            // Count nodes
            Integer nodeCount = neo4jClient.query("MATCH (n) RETURN count(n) as count")
                .fetchAs(Integer.class)
                .mappedBy((typeSystem, record) -> record.get("count").asInt())
                .one()
                .orElse(0);
            System.out.println("Total nodes in database: " + nodeCount);
            
            // Count relationships
            Integer relCount = neo4jClient.query("MATCH ()-[r]->() RETURN count(r) as count")
                .fetchAs(Integer.class)
                .mappedBy((typeSystem, record) -> record.get("count").asInt())
                .one()
                .orElse(0);
            System.out.println("Total relationships in database: " + relCount);
            
            // Check specific friend requests
            Integer friendReqCount = neo4jClient.query("MATCH ()-[:FRIEND_REQ]->() RETURN count(*) as count")
                .fetchAs(Integer.class)
                .mappedBy((typeSystem, record) -> record.get("count").asInt())
                .one()
                .orElse(0);
            System.out.println("Friend requests in database: " + friendReqCount);
            
        } catch (Exception e) {
            System.err.println("Error verifying data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}