package agh.matury.service;

import agh.matury.model.User;
import agh.matury.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User createUser(String username, String email, String passwordHash) {
    User user = new User();
    user.setUsername(username);
    user.setEmail(email);
    user.setPasswordHash(passwordHash);
    return userRepository.save(user);
  }
}
