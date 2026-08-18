package com.example.userservice.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.userservice.dto.UserResponse;
import com.example.userservice.model.User;

@Service
public class UserService {

    private List<User> users = new ArrayList<>();

    public UserService() {

        users.add(new User(1L, "Ranjani", "ranjani@gmail.com"));
        users.add(new User(2L, "Arun", "arun@gmail.com"));
        users.add(new User(3L, "Priya", "priya@gmail.com"));
    }

    public UserResponse getUserById(Long id) {

        for (User user : users) {

            if (user.getId().equals(id)) {

                return new UserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail()
                );
            }
        }

        return null;
    }
}