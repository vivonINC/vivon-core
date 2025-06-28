package com.vivoninc.DAOs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.vivoninc.model.User;

@Repository
public class UserDAO {

    private final Neo4jClient neo4jClient;
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate nJdbcTemplate;

    public UserDAO(JdbcTemplate jdbcTemplate, Neo4jClient neo4jClient, NamedParameterJdbcTemplate nJdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
        this.neo4jClient = neo4jClient;
        this.nJdbcTemplate = nJdbcTemplate;
    }

    public void acceptFriendRequest(int userID, int incFriendID){
        neo4jClient.query("""
                MATCH (a:User {id: $fromID}), (b:User {id: $toID})
                MERGE (b)-[:FRIEND_REQ]->(a)
            """)
            .bind(userID).to("fromID")
            .bind(incFriendID).to("toID")
            .run();
    }

    public void sendFriendRequest(int fromID, int toID) {
        neo4jClient.query("""
                MATCH (a:User {id: $fromID}), (b:User {id: $toID})
                MERGE (a)-[:FRIEND_REQ]->(b)
            """)
            .bind(fromID).to("fromID")
            .bind(toID).to("toID")
            .run();
    }

    public Collection<User> getUsersIncommingFriendReq(int id) {
        String cypher = """
                MATCH (u:User {id: $id})<-[f:FRIEND_REQ]-(friend:User)
                RETURN friend
                """;

        return neo4jClient.query(cypher)
                .bind(id).to("id")   // bind the parameter "id"
                .fetchAs(User.class) // map result to User.class
                .mappedBy((typeSystem, record) -> {
                    var node = record.get("friend").asNode();
                    User user = new User();
                    user.setId(node.get("id").asInt());
                    user.setUserName(node.get("name").asString());
                    return user;
                })
                .all();  // get List<User>
    }

     public Collection<User> getUsersFriends(int id) {
        // Step 1: get friend IDs from Neo4j
        List<Integer> friendIds = new ArrayList<>(neo4jClient.query("""
        MATCH (u:User {id: $id})-[:FRIEND]-(friend:User)
        RETURN friend.id AS friendId
        """)
        .bind(id).to("id")
        .fetchAs(Integer.class)
        .mappedBy((ts, record) -> record.get("friendId").asInt())
        .all());


        // Step 2: query MySQL for full user info
        if (friendIds.isEmpty()) {
            return Collections.emptyList();
        }

        String sql = "SELECT id, username, avatar FROM users WHERE id IN (:ids)";
        Map<String, Object> params = Map.of("ids", friendIds);

        List<User> friends = nJdbcTemplate.query(sql, params, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setUserName(rs.getString("username"));
            user.setAvatar(rs.getString("avatar"));
            return user;
        });

        return friends;
    }
    
}
