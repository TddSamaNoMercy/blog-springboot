package com.github.xym.service;

import com.github.xym.entity.User;
import com.github.xym.mapper.MyMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Collections;

@Service
public class UserService implements UserDetailsService {
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private MyMapper myMapper;

    @Inject
    public UserService(BCryptPasswordEncoder bCryptPasswordEncoder, MyMapper myMapper) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.myMapper = myMapper;
    }

    public void save(String username, String password) {
        myMapper.save(username, bCryptPasswordEncoder.encode(password));
    }

    public User getUserByUsername(String username) {
        return myMapper.findUserByUsername(username);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username + "不存在");
        }
        return new org.springframework.security.core.userdetails.
                User(username, user.getEncryptedPassword(), Collections.emptyList());
    }
}
