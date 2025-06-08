package com.vivoninc.Controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vivoninc.model.Friend;

@RestController
@RequestMapping("/friends")
public class FriendController {

    @GetMapping
    public List<Friend> getFriends() {
        return null;
        // logic to get current user's friends
    }


}

