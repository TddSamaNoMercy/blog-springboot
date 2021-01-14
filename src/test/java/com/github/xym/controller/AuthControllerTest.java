package com.github.xym.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xym.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.HttpSession;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
class AuthControllerTest {
    private MockMvc mockMvc;
    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @Mock
    private UserService mockUserService;
    @Mock
    private AuthenticationManager mockAuthenticationManager;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new AuthController(mockUserService, mockAuthenticationManager)).build();
    }

    @Test
    void login() throws Exception {
        mockMvc.perform(get("/auth"))
                .andExpect(status().isOk())
                .andExpect(mvcResult -> Assertions
                        .assertTrue(mvcResult.getResponse().getContentAsString(Charset.defaultCharset()).contains("用户没有登录")));
        Map<String, String> loginInfo = new HashMap<>();
        loginInfo.put("username", "myUser");
        loginInfo.put("password", "myPassword");

        Mockito.when(mockUserService.loadUserByUsername("myUser")).thenReturn(
                new User("myUser", bCryptPasswordEncoder
                        .encode("myEncodedPassword"), Collections.emptyList()));
        Mockito.when(mockUserService.getUserByUsername("myUser")).thenReturn(
                new com.github.xym.entity.User(123, "myUser", bCryptPasswordEncoder
                        .encode("myEncodedPassword")));
        //login
        final MvcResult response = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(loginInfo)))
                .andExpect(status().isOk())
                .andExpect(result ->
                        Assertions.assertTrue(result.getResponse().getContentAsString(Charset.defaultCharset()).contains("登录成功")))
                .andReturn();
        final HttpSession session = response.getRequest().getSession();

        mockMvc.perform(get("/auth").session((MockHttpSession) session))
                .andExpect(status().isOk())
                .andExpect(mvcResult -> Assertions
                        .assertTrue(mvcResult.getResponse().getContentAsString(Charset.defaultCharset()).contains("myUser")));
        //logout when logged in
        mockMvc.perform(get("/auth/logout").session((MockHttpSession) session)).andExpect(status().isOk()).andExpect(result ->
                Assertions.assertTrue(result.getResponse().getContentAsString(Charset.defaultCharset()).contains("注销成功")));
    }
}