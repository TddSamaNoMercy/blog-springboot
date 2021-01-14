package com.github.xym.service;

import com.github.xym.entity.User;
import com.github.xym.mapper.MyMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private BCryptPasswordEncoder mockEncoder;
    @Mock
    private MyMapper mockMapper;
    @InjectMocks
    private UserService userService;

    @Test
    public void testSave() {
        when(mockEncoder.encode("myPassword")).thenReturn("myEncodedPassword");
        userService.save("myUser", "myPassword");
        verify(mockMapper).save("myUser", "myEncodedPassword");
    }

    @Test
    public void testGetUserByUsername() {
        userService.getUserByUsername("myUser");
        verify(mockMapper).findUserByUsername("myUser");
    }

    @Test
    public void testThrowExceptionWhenUserNotFound() {
        Assertions.assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("myUser");
        });
    }

    @Test
    public void testThrowExceptionWhenUserFound() {
        when(mockMapper.findUserByUsername("myUser")).
                thenReturn(new User(123, "myUser", "myEncodedPassword"));
        UserDetails userDetails = userService.loadUserByUsername("myUser");
        Assertions.assertEquals("myUser", userDetails.getUsername());
        Assertions.assertEquals("myEncodedPassword", userDetails.getPassword());
    }
}