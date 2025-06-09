package com.vivoninc.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class Neo4jInitializer {

    @Autowired
    private Neo4jClient neo4jClient;

    @PostConstruct
    public void init() {
        //Mock data
        //Only include id or metadata in connection attributes
        String cypher = """
            MERGE (a:User {name: 'John', password: '123'})
            MERGE (b:User {name: 'Jane', password: 'abcde'})
            MERGE (c:User {name: 'Mås', password: '4321'})
            MERGE (d:User {name: 'Nös', password: 'lol'})
            MERGE (gr1:Group)
            MERGE (c)-[:IN_GROUP]->(gr1)
            MERGE (d)-[:IN_GROUP]->(gr1)
            MERGE (b)-[:FRIEND]-(a)
            MERGE (b)-[:FRIEND]-(c)
            MERGE (a)-[:FRIEND_REQ]-(d)
        """;
        neo4jClient.query(cypher).run();
    }
}

