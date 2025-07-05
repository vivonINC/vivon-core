package com.vivoninc.DAOs;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.vivoninc.DAOs.UserDAO;

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

}
