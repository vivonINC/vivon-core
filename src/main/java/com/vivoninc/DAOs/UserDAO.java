package com.vivoninc.DAOs;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserDAO {

        private final JdbcTemplate jdbcTemplate;

        public UserDAO(JdbcTemplate jdbcTemplate){
            this.jdbcTemplate = jdbcTemplate;
        }

    
    
}
