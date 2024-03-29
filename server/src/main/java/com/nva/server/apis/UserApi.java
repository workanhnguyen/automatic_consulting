package com.nva.server.apis;

import com.nva.server.dtos.ChangePasswordRequest;
import com.nva.server.dtos.EditUserRequest;
import com.nva.server.dtos.UserResponse;
import com.nva.server.entities.User;
import com.nva.server.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin
public class UserApi {
    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok().body(userService.getUsers(null));
    }
    @PostMapping("/save")
    public ResponseEntity<User> saveUser(@RequestBody User user) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/users/save").toUriString());
        return ResponseEntity.created(uri).body(userService.saveUser(user));
    }

    @PostMapping("/changeAvatar")
    public ResponseEntity<?> changeAvatar(@RequestBody Map<String, String> avatarObj) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok().body(Collections.singletonMap("avatarLink", userService.updateAvatar(avatarObj.get("avatarBase64"), user)));
    }

    @PostMapping("/updateInfo")
    public ResponseEntity<UserResponse> updateInfo(@RequestBody EditUserRequest request) {
        return ResponseEntity.ok().body(userService.editUserInfo(request));
    }

    @PostMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        userService.changePassword(changePasswordRequest);
        return ResponseEntity.ok(Collections.singletonMap("message", "Mật khẩu đã được thay đổi thành công!"));
    }

    @PostMapping("/toggleLockAccount")
    public ResponseEntity<?> toggleLockAccount() {
        return ResponseEntity.ok(userService.toggleLockUser());
    }

    @DeleteMapping("/deleteAccount")
    public ResponseEntity<?> deleteAccount() {
        userService.deleteUser();
        return ResponseEntity.ok(Collections.singletonMap("message", "Account has been deleted successfully!"));
    }

    @GetMapping("/sayHello")
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok(String.format("Hello %s", SecurityContextHolder.getContext().getAuthentication().getName()));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile() {
        return ResponseEntity.ok(userService.getProfile());
    }
}
