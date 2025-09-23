package com.example.springproject.service;

import com.example.springproject.model.User;
import com.example.springproject.repository.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.sql.SQLException;

@Service
public class UserDetailsServiceImpl implements UserDetailsService{
    @Autowired
    private UserDao userDao;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String id) {
        try {
            User user = userDao.findById(id);
            if (user == null) {
                System.out.println("[DEBUG] User not found: " + id);
                throw new UsernameNotFoundException(id);
            }

            System.out.println("[DEBUG] User found: " + user.getId());
            System.out.println("[DEBUG] is_banned: " + user.isBanned());


            boolean enabled = !user.isBanned();
            System.out.println("[DEBUG] Account enabled: " + enabled);

            boolean accountNonExpired = true;
            boolean credentialsNonExpired = true;
            boolean accountNonLocked = true;

            Set<GrantedAuthority> grantedAuthorities = new HashSet<>();

            org.springframework.security.core.userdetails.User springUser =
                    new org.springframework.security.core.userdetails.User(
                            user.getId(),
                            user.getPassword(),
                            enabled,
                            accountNonExpired,
                            credentialsNonExpired,
                            accountNonLocked,
                            grantedAuthorities);

            System.out.println("[DEBUG] Created UserDetails: " + springUser);

            return springUser;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}