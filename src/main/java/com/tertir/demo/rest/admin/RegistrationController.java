//package com.tertir.demo.rest.admin;
//
//import com.tertir.demo.services.UserService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/tertir/public/register")
//@CrossOrigin("*")
//@Validated
//public class RegistrationController {
//
//    private final UserService userService;
//
//    public RegistrationController(UserService userService) {
//        this.userService = userService;
//    }
//
//    @PostMapping
//    public ResponseEntity<?> registerUser(@RequestParam("username")String username,
//                                          @RequestParam("password")String password){
//
//        userService.registerUser(username,password);
//        return ResponseEntity.ok("Success");
//    }
//
//}
