package agh.matury;

import agh.matury.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

  private final UserService userService;

  public HelloController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/")
  public String hello() {
    userService.createUser("jeden", "dwa", "trzy");
    return "Hello, world!";
  }
}
