package com.github.xym.controller;

import com.github.xym.entity.Result;
import com.github.xym.entity.User;
import com.github.xym.service.UserService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Map;


@Controller
public class AuthController {

    private UserService userService;
    private AuthenticationManager authenticationManager;

    @Inject
    public AuthController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/auth")
    @ResponseBody
    public Result auth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (authentication == null) ? null : authentication.getName();
        User loggedInUser = userService.getUserByUsername(username);
        if (loggedInUser == null) {
            return new Result("ok", "用户没有登录", false, null);
        } else {
            return new Result("ok", null, true, loggedInUser);
        }
    }

    @PostMapping("/auth/login")
    @ResponseBody
    public Result login(@RequestBody Map<String, String> map) {
        String username = map.get("username");
        String password = map.get("password");
        UserDetails userDetails;
        try {
            userDetails = userService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            return Result.failure("用户不存在");
        }
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(userDetails,
                        password, userDetails.getAuthorities());

        try {
            authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(token);
            return new Result("ok", "登录成功", true, userService.getUserByUsername(username));

        } catch (BadCredentialsException e) {
            return Result.failure("密码不正确");
        }
    }

    @PostMapping("/auth/register")
    @ResponseBody
    public Result register(@RequestBody Map<String, String> map) {
        String username = map.get("username");
        String password = map.get("password");
        if (username.length() < 1 || username.length() > 15) {
            return Result.failure("invalid username");
        }
        if (password.length() < 6 || password.length() > 15) {
            return Result.failure("invalid password");
        }

        try {
            userService.save(username, password);
        } catch (DuplicateKeyException e) {
            return Result.failure("user already exists");
        }
        return new Result("ok", "注册成功", false, userService.getUserByUsername(username));

    }

    @GetMapping("/auth/logout")
    @ResponseBody
    public Object logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (authentication == null) ? null : authentication.getName();
        User loggedInUser = userService.getUserByUsername(username);
        if (loggedInUser == null) {
            return Result.failure("用户尚未登录");
        } else {
            SecurityContextHolder.clearContext();
            return new Result("ok", "注销成功", false, null);
        }
    }


}
