package com.alten.shop.util;

import com.alten.shop.config.UserPrincipal;
import com.alten.shop.entity.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class TestSecurityUtils {

    public static void setAuthUser(String email, String username) {
        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setUsername(username);
        user.setFirstname("Test");
        user.setPassword("password");

        UserPrincipal userPrincipal = new UserPrincipal(user);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public static void clearAuth() {
        SecurityContextHolder.clearContext();
    }
}