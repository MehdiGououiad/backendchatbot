package chatbot.controllers;


import chatbot.entity.User;
import chatbot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping("/create")
    public ResponseEntity<User> createUser(String username,String password) {
        User createdUser = userService.createUser(username,password);
        return ResponseEntity.ok(createdUser);
    }
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestParam String username, @RequestParam String password) {
        User user = userService.loginUser(username, password);
    
        if (user != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("userId", user.getId()); // Assuming the User object has an getId() method
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> response = Collections.singletonMap("message", "Login failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
//    @GetMapping
//    public List<User> getAllUsers() {
//        return userService.getAllUsers();
//    }


    //TODO: Implement the methods for the UserController
    // Create User
    //



    // Existing endpoints
}
