package com.vivoninc.DAOs;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.vivoninc.model.User;

@Repository
public class UserDAO {

    private final Neo4jClient neo4jClient;
    private final JdbcTemplate jdbcTemplate;

    public UserDAO(JdbcTemplate jdbcTemplate, Neo4jClient neo4jClient){
        this.jdbcTemplate = jdbcTemplate;
        this.neo4jClient = neo4jClient;
    }

    public Collection<User> getUsersFriends(int id) {
        String cypher = """
                MATCH (u:User {id: $id})-[f]-(friend:User)
                RETURN friend
                """;

        return neo4jClient.query(cypher)
                .bind(id).to("id")   // bind the parameter "id"
                .fetchAs(User.class) // map result to User.class
                .mappedBy((typeSystem, record) -> {
                    // Here you manually map the record to a User
                    // 'friend' is the name of the returned node
                    var node = record.get("friend").asNode();
                    User user = new User();
                    user.setId(node.get("id").asInt());
                    user.setUserName(node.get("name").asString());
                    // set other properties as needed
                    return user;
                })
                .all();  // get List<User>
    }
    
}
