package com.vivoninc.DAOs;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.Value;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.vivoninc.model.User;

import org.springframework.transaction.annotation.Transactional;

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

        // Create convo and add both users
        String sql = "INSERT INTO conversations (type) VALUES ('direct')";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            return ps;
        }, keyHolder);
        
        long conversationId = keyHolder.getKey().longValue();
        
        // Add both users
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

@Transactional(transactionManager = "neo4jTransactionManager")
public String sendFriendRequest(int fromUserId, int toUserId) {
    System.out.println("Sending friend request from user " + fromUserId + " to user " + toUserId);
    
    try {

        if (fromUserId == toUserId) {
            return "You cannot befriend yourself";
        }

    boolean isFriend = neo4jClient.query("""
        MATCH (from:User {id: $fromID})-[:FRIEND]-(to:User {id: $toID})
        RETURN count(*) > 0 AS isFriend
    """)
    .bind(fromUserId).to("fromID")
    .bind(toUserId).to("toID")
    .fetch()
    .one()
    .map(result -> (Boolean) result.get("isFriend"))
    .orElse(false);



    if (isFriend) {
        return("That user is already your friend");
    }

        // Create the friend request relationship directly
        String cypher2 = """
            MATCH (from:User {id: $fromId}), (to:User {id: $toId})
            MERGE (from)-[:FRIEND_REQ]->(to)
            """;

        neo4jClient.query(cypher2)
            .bind(fromUserId).to("fromId")
            .bind(toUserId).to("toId")
            .run();
            
        return("Friend request sent successfully");
        
    } catch (Exception e) {
        return("Error sending friend request: " + e.getMessage());
    }
}


@Transactional(readOnly = true)
public Collection<User> getUsersIncommingFriendReq(int id) {
    System.out.println("Getting incoming friend requests for user ID: " + id);
    
    String cypher = """
        MATCH (u:User {id: $id})<-[:FRIEND_REQ]-(friend:User)
        RETURN friend.id AS friendId
        """;

    try {
        List<Integer> friendIds = (List<Integer>) neo4jClient.query(cypher)
            .bind(id).to("id")
            .fetchAs(Integer.class)
            .mappedBy((typeSystem, record) -> {
                int friendId = record.get("friendId").asInt();
                System.out.println("Found incoming friend request from user ID: " + friendId);
                return friendId;
            })
            .all();

        System.out.println("Total incoming friend requests found: " + friendIds.size());

        if (friendIds.isEmpty()) {
            System.out.println("No incoming friend requests found for user " + id);
            return new ArrayList<>();
        }

        // Query MySQL for the full user data
        List<User> users = friendIds.stream()
            .map(friendId -> {
                try {
                    System.out.println("Fetching user data from MySQL for ID: " + friendId);
                    User user = jdbcTemplate.queryForObject(
                        "SELECT id, username, avatar FROM users WHERE id = ?",
                        new Object[]{friendId},
                        (rs, rowNum) -> {
                            User u = new User();
                            u.setId(rs.getInt("id"));
                            u.setUserName(rs.getString("username"));
                            u.setAvatar(rs.getString("avatar"));
                            return u;
                        }
                    );
                    System.out.println("Successfully fetched user: " + user.getUserName());
                    return user;
                } catch (Exception e) {
                    System.err.println("Error fetching user data for ID " + friendId + ": " + e.getMessage());
                    return null;
                }
            })
            .filter(user -> user != null)
            .toList();
            
        System.out.println("Returning " + users.size() + " users with friend requests");
        return users;
        
    } catch (Exception e) {
        System.err.println("Error executing friend request query: " + e.getMessage());
        e.printStackTrace();
        return new ArrayList<>();
    }
}

     public Collection<User> getUsersFriends(int id) {
        //get friend IDs from Neo4j
        List<Integer> friendIds = new ArrayList<>(neo4jClient.query("""
        MATCH (u:User {id: $id})-[:FRIEND]-(friend:User)
        RETURN friend.id AS friendId
        """)
        .bind(id).to("id")
        .fetchAs(Integer.class)
        .mappedBy((ts, record) -> record.get("friendId").asInt())
        .all());


        // query MySQL for full user info
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

    public void removeFriend(int userID, int userIDtoRemove){
        String cypher = """
        MATCH (remover:User {id: $userID})-[f:FRIEND]-(other:User {id: $userIDtoRemove})
        DELETE f
        """;

        neo4jClient.query(cypher)
        .bind(userID).to("userID")
        .bind(userIDtoRemove).to("userIDtoRemove")
        .run();
    }

    public void addFriendDescription(int fromID, int toID, String desc){
        String propertyName = "desc_" + fromID; // e.g., "desc_123"
        
        String cypher = """
        MATCH (from:User {id: $fromID})-[f:FRIEND]-(to:User {id: $toID})
        SET f[$propertyName] = $desc
        """;

        neo4jClient.query(cypher)
        .bind(fromID).to("fromID")
        .bind(toID).to("toID")
        .bind(desc).to("desc")
        .bind(propertyName).to("propertyName")
        .run();
    }

    public Integer getIDFromUsername(String username){
        return jdbcTemplate.queryForObject("SELECT id FROM users WHERE username = ?", Integer.class, username);
    }

    public String getFriendDescription(String fromID, String toID) {
        String propertyName = "desc_" + fromID;
        String cypher = """
            MATCH (from:User {id: $fromID})-[f:FRIEND]-(to:User {id: $toID})
            RETURN f[$propertyName] AS description
        """;

        return neo4jClient.query(cypher)
            .bind(Integer.parseInt(fromID)).to("fromID")
            .bind(Integer.parseInt(toID)).to("toID")
            .bind(propertyName).to("propertyName")
            .fetchAs(String.class)
            .mappedBy((typeSystem, record) -> {
                Value descValue = record.get("description");
                return descValue.isNull() ? null : descValue.asString();
            })
            .one()
            .orElse(null);
    }

}
