package com.vivoninc.DAOs;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.vivoninc.model.Message;
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

    public void acceptFriendRequest(int userID, int requesterID){
        // Remove the friend request and create mutual friendship
        neo4jClient.query("""
                MATCH (requester:User {id: $requesterID})-[req:FRIEND_REQ]->(user:User {id: $userID})
                DELETE req
                MERGE (requester)-[:FRIEND]-(user)
            """)
            .bind(userID).to("userID")
            .bind(requesterID).to("requesterID")
            .run();

        // Create conversation and add both users
        String sql = "INSERT INTO conversations (type) VALUES ('direct')";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            return ps;
        }, keyHolder);
        
        long conversationId = keyHolder.getKey().longValue();
        
        // Add both users to the conversation
        String memberSql = "INSERT INTO conversation_members (conversation_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(memberSql, conversationId, userID);
        jdbcTemplate.update(memberSql, conversationId, requesterID);
    }

    public void declineFriendRequest(int userID, int incFriendID){
    neo4jClient.query("""
            MATCH (a:User {id: $fromID})-[req:FRIEND_REQ]-(b:User {id: $toID})
            DELETE req
        """)
        .bind(userID).to("fromID")
        .bind(incFriendID).to("toID")
        .run();
}

public void sendFriendRequest(int fromID, int toID) {
    neo4jClient.query("""
            MATCH (a:User {id: $fromID})
            WITH a
            MATCH (b:User {id: $toID})
            MERGE (a)-[:FRIEND_REQ]->(b)
        """)
        .bind(fromID).to("fromID")
        .bind(toID).to("toID")
        .run();
}


 public Collection<User> getUsersIncommingFriendReq(int id) {
    String cypher = """
        MATCH (u:User {id: $id})<-[f:FRIEND_REQ]-(friend:User)
        RETURN friend.id AS friendId
        """;

    return neo4jClient.query(cypher)
        .bind(id).to("id")
        .fetchAs(Integer.class)
        .mappedBy((typeSystem, record) -> record.get("friendId").asInt())
        .all()
        .stream()
        .map(friendId -> {
            // Query MySQL for the full user data
            return jdbcTemplate.queryForObject(
                "SELECT id, username, avatar FROM users WHERE id = ?",
                new Object[]{friendId},
                (rs, rowNum) -> {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUserName(rs.getString("username"));
                    user.setAvatar(rs.getString("avatar"));
                    return user;
                }
            );
        })
        .toList();
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

public Collection<Map<String, Object>> getUserNameAndAvatar(List<String> userIDs){
    if (userIDs.isEmpty()) {
        return new ArrayList<>();
    }
    
    String placeholders = String.join(",", Collections.nCopies(userIDs.size(), "?"));
    String sql = "SELECT username, avatar FROM users WHERE id IN (" + placeholders + ")";
    
    // Convert List<String> to Object[] for the query
    Object[] params = userIDs.toArray();
    return jdbcTemplate.queryForList(sql, params);
}

    public void setUserIsOnline(int userID){
        String sql = "UPDATE users SET is_online 1 WHERE id = ?";
        jdbcTemplate.update(sql, userID);
    }

    public void setUserIsOffline(int userID){
        String sql = "UPDATE users SET is_online 0 WHERE id = ?";
        jdbcTemplate.update(sql, userID);
    }

    public Integer getIDFromUsername(String username){
        return jdbcTemplate.queryForObject("SELECT id FROM users WHERE username = ?", Integer.class, username);
    }


}
