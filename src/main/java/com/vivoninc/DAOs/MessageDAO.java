package com.vivoninc.DAOs;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.vivoninc.model.Message;

import java.sql.PreparedStatement;
import java.sql.Statement;

@Repository
public class MessageDAO {
    
    private final JdbcTemplate jdbcTemplate;
    private final UserDAO userDAO;

    public MessageDAO(JdbcTemplate jdbcTemplate, UserDAO userDAO){
        this.jdbcTemplate = jdbcTemplate;
        this.userDAO = userDAO;
    }

    public Collection<Map<String, Object>> getLast25Messages(int conversationID) {
        String sql = """
            SELECT 
                m.id,
                m.sender_id,
                m.content,
                m.created_at,
                u.username,
                u.avatar
            FROM messages m
            JOIN users u ON m.sender_id = u.id
            WHERE m.conversation_id = ? 
            AND m.deleted_at IS NULL
            ORDER BY m.created_at DESC
            LIMIT 25
            """;
        List<Map<String, Object>> temp = jdbcTemplate.queryForList(sql, conversationID);
        Collections.reverse(temp);
        return temp;
    }

    public Collection<Map<String, Object>> getMessagesBefore(int conversationID, String beforeTimestamp, int limit) {
    String sql = """
        SELECT 
            m.id,
            m.sender_id,
            m.content,
            m.created_at,
            u.username,
            u.avatar
        FROM messages m
        JOIN users u ON m.sender_id = u.id
        WHERE m.conversation_id = ? 
        AND m.created_at < ?
        AND m.deleted_at IS NULL
        ORDER BY m.created_at DESC 
        LIMIT ?
        """;
    List<Map<String, Object>> temp = jdbcTemplate.queryForList(sql, conversationID, beforeTimestamp, limit);
    
    Collections.reverse(temp);
    return temp;
}

    public Collection<Map<String, Object>> getConversationMembers(int conversation_id){
        String sql = "SELECT user_id FROM conversation_members WHERE conversation_id = ?";
        return jdbcTemplate.queryForList(sql, conversation_id);
    }

    public Collection<Map<String, Object>> getConversationGroups(int userID) {
        String sql = "SELECT conversation_id FROM conversation_members WHERE user_id = ?";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, userID);

        List<Long> conversationIds = result.stream()
            .map(row -> ((Number) row.get("conversation_id")).longValue())
            .collect(Collectors.toList());

        if (conversationIds.isEmpty()) {
            return Collections.emptyList();
        }

        // build placeholders (?, ?, ?, ...)
        String placeholders = String.join(",", Collections.nCopies(conversationIds.size(), "?"));

        String finalSql = """
            SELECT 
                c.id AS conversation_id,
                c.name AS conversation_name,
                c.type AS conversation_type,
                m.created_at AS last_message_time
            FROM conversations c
            LEFT JOIN messages m ON c.last_message_id = m.id
            WHERE c.id IN (""" + placeholders + ") ORDER BY m.created_at DESC";

        return jdbcTemplate.queryForList(finalSql, conversationIds.toArray());
    }

    public Optional<Map<String, Object>> findDirectConversation(int userId1, int userId2) {
        String sql = """
            SELECT DISTINCT c.id, c.name, c.type, c.last_message_id
            FROM conversations c
            JOIN conversation_members cm1 ON c.id = cm1.conversation_id
            JOIN conversation_members cm2 ON c.id = cm2.conversation_id
            WHERE c.type = 'direct' 
              AND cm1.user_id = ? 
              AND cm2.user_id = ?
            """;
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, userId1, userId2);
        
        if (results.isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.of(results.get(0));
    }

    @Transactional
    public Long createConversation(String conversationType, String conversationName, List<Integer> participantIds) {
        if (participantIds == null || participantIds.isEmpty()) {
            throw new IllegalArgumentException("Participant list cannot be empty");
        }

        //check conversation type
        if (!"direct".equals(conversationType) && !"group".equals(conversationType)) {
            throw new IllegalArgumentException("Conversation type must be 'direct' or 'group'");
        }

        // For direct conversations ensure 2 participants
        if ("direct".equals(conversationType) && participantIds.size() != 2) {
            throw new IllegalArgumentException("Direct conversations must have exactly 2 participants");
        }

        // Check if direct conversation exists
        if ("direct".equals(conversationType)) {
            Optional<Map<String, Object>> existing = findDirectConversation(
                participantIds.get(0), participantIds.get(1)
            );
            if (existing.isPresent()) {
                return ((Number) existing.get().get("id")).longValue();
            }
        }

        // Create conversation
        String insertConversationSql = """
            INSERT INTO conversations (name, type) VALUES (?, ?)
            """;
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertConversationSql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, conversationName);
            ps.setString(2, conversationType);
            return ps;
        }, keyHolder);
        
        Long conversationId = keyHolder.getKey().longValue();
        
        // Add users to convo
        String insertMemberSql = """
            INSERT INTO conversation_members (conversation_id, user_id, role) VALUES (?, ?, ?)
            """;
        
        for (int i = 0; i < participantIds.size(); i++) {
            Integer userId = participantIds.get(i);
            String role = "member"; // Default role
            
            // For group conversations make the first participant owner
            if ("group".equals(conversationType) && i == 0) {
                role = "owner";
            }
            
            jdbcTemplate.update(insertMemberSql, conversationId, userId, role);
        }
        
        return conversationId;
    }


    public Collection<Map<String, Object>> getUserConversationsWithMembers(int userId) {
        String sql = """
            SELECT 
                c.id AS conversation_id,
                c.name AS conversation_name,
                c.type AS conversation_type,
                c.last_message_id,
                m.created_at AS last_message_time,
                GROUP_CONCAT(
                    JSON_OBJECT(
                        'user_id', cm.user_id,
                        'username', u.username,
                        'avatar', u.avatar,
                        'is_online', u.is_online,
                        'role', cm.role
                    )
                ) AS members
            FROM conversations c
            JOIN conversation_members cm_user ON c.id = cm_user.conversation_id
            JOIN conversation_members cm ON c.id = cm.conversation_id
            JOIN users u ON cm.user_id = u.id
            LEFT JOIN messages m ON c.last_message_id = m.id
            WHERE cm_user.user_id = ?
            GROUP BY c.id, c.name, c.type, c.last_message_id, m.created_at
            ORDER BY m.created_at DESC
            """;
        
        return jdbcTemplate.queryForList(sql, userId);
    }

    public boolean isUserMemberOfConversation(int userId, long conversationId) {
        String sql = """
            SELECT COUNT(*) FROM conversation_members 
            WHERE user_id = ? AND conversation_id = ?
            """;
        
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, conversationId);
        return count != null && count > 0;
    }

    /**
     * Add a user to an existing conversation
     * @param conversationId The conversation ID
     * @param userId The user ID to add
     * @param role The role to assign (member, admin, owner)
     * @return true if user was added successfully
     */
    public boolean addUserToConversation(int userId, long conversationId) {
        // Check if user is already in the conversation
        if (isUserMemberOfConversation(userId, conversationId)) {
            return false;
        }
        
        String sql = """
            INSERT INTO conversation_members (conversation_id, user_id, role) 
            VALUES (?, ?, ?)
            """;
        
        int rowsAffected = jdbcTemplate.update(sql, conversationId, userId, "member");
        return rowsAffected > 0;
    }

    /**
     * Remove a user from a conversation
     * @param conversationId The conversation ID
     * @param userId The user ID to remove
     * @return true if user was removed successfully
     */
    public boolean removeUserFromConversation(long conversationId, int userId) {
        String sql = """
            DELETE FROM conversation_members 
            WHERE conversation_id = ? AND user_id = ?
            """;
        
        int rowsAffected = jdbcTemplate.update(sql, conversationId, userId);
        return rowsAffected > 0;
    }

    public Message send(Message message) {
        String sql = "INSERT INTO messages (conversation_id, sender_id, content, message_type, created_at) VALUES (?,?,?,?,?)";
        
        // Use KeyHolder to get the generated ID
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, message.getConversationID());
            ps.setString(2, message.getSenderID());
            ps.setString(3, message.getContent());
            ps.setString(4, message.getType().toString());
            ps.setTimestamp(5, message.getDateSent());
            return ps;
        }, keyHolder);
        
        // Set the generated ID on the message object
        Number generatedId = keyHolder.getKey();
        if (generatedId != null) {
            message.setID(generatedId.intValue());
        }
        
        return message; // Return the message with the ID set
    }
}