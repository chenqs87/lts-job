/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zy.data.lts.console;


import com.zy.data.lts.security.config.WebSecurityConfig;
import com.zy.data.lts.security.utils.JwtTokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.util.Iterator;

/**
 * 登录验证
 *
 * @author chenqingsong
 */
@RestController
@RequestMapping("/console/auth")
public class AuthController {

    @Autowired
    private JwtTokenUtils jwtTokenUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @ResponseBody
    @RequestMapping(value = "login", method = RequestMethod.POST)
    public ResponseEntity login(@RequestBody User user, HttpServletResponse response) {

        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());

        try {
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtTokenUtils.createToken(authentication);
            token = "Bearer " + token;
            response.addHeader(WebSecurityConfig.AUTHORIZATION_HEADER, token);

            String role = "";
            Iterator<? extends GrantedAuthority> iter = authentication.getAuthorities().iterator();

            if(iter.hasNext()) {
                role = iter.next().getAuthority();
            }

            return ResponseEntity.ok(new LoginSuccess(token, role));
        } catch (BadCredentialsException authentication) {
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    public static class User {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class LoginSuccess {
        private String token;
        private String role;

        public LoginSuccess (){}

        public LoginSuccess(String token, String role) {
            this.token = token;
            this.role = role;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}
