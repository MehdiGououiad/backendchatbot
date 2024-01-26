package chatbot.service;

import chatbot.entity.User;
import org.springframework.stereotype.Service;
import chatbot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(String username, String password) {
        // Business logic goes here
        // For example, validating the user object
        // Or throwing an exception if the user already exists

        // Create a new User object with the provided username and password
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        // Save the user to the userRepository
        return userRepository.save(user);
    }
    
    public User loginUser(String username, String password) {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user; // Return the user object
            }
        }
        return null; // Return null if user not found or password is incorrect
    }
    

}