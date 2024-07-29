package com.example.mychat;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;
import  com.example.mychat.UserDto;

import java.util.List;

@Controller
public class AuthController {
    private UserDto currentUser;
    private List<UserDto> onlineUsers;

    @PostMapping("/")
    public String receiveUser(@RequestBody UserDto user, Model model) {
        this.currentUser = user;
        model.addAttribute("userDto", currentUser);
        return "index"; // 返回 index 视图
    }
    @GetMapping("/")
    public String chatroom(Model model) {
        if (currentUser != null) {
            model.addAttribute("userDto", currentUser);
        }
        return "index"; // 返回 index 视图
    }
}