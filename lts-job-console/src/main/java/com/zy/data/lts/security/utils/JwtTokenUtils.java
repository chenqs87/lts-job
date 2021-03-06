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
package com.zy.data.lts.security.utils;

import com.zy.data.lts.core.dao.UserDao;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Jwt token tool
 *
 * @author chenqingsong
 */
@Component
public class JwtTokenUtils {

    private final Logger log = LoggerFactory.getLogger(JwtTokenUtils.class);

    private static final String AUTHORITIES_KEY = "auth";

    @Autowired
    private UserDao userDao;

    private String secretKey;

    /**
     * Token 过期时间
     */
    private long tokenValidityInMilliseconds;

    @PostConstruct
    public void init() {
        this.secretKey = "SecretKey012345678901234567890123456789012345678901234567890123456789";
        this.tokenValidityInMilliseconds = 1000 * 60 * 60 * 2L;
    }

    /**
     * Create token
     *
     * @param authentication auth info
     * @return token
     */
    public String createToken(Authentication authentication) {

        long now = (new Date()).getTime();

        Date validity = new Date(now + this.tokenValidityInMilliseconds);

        List<String> authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    /**
     * Get auth Info
     *
     * @param token token
     * @return auth info
     */
    public Authentication getAuthentication(String token) {
        try {

            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();

            //List<String> authorities = (List<String>) claims.get(AUTHORITIES_KEY);
            String username = claims.getSubject();
            com.zy.data.lts.core.entity.User user = userDao.findByName(username);

            User principal = new User(username, "", AuthorityUtils.createAuthorityList(user.getRole()));
            return new UsernamePasswordAuthenticationToken(principal, "", AuthorityUtils.createAuthorityList(user.getRole()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * validate token
     *
     * @param token token
     * @return whether valid
     */
    @SuppressWarnings("deprecation")
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            log.info("Invalid JWT signature.");
            log.trace("Invalid JWT signature trace: {}", e);
        } catch (MalformedJwtException e) {
            log.info("Invalid JWT token.");
            log.trace("Invalid JWT token trace: {}", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token.");
            log.trace("Expired JWT token trace: {}", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token.");
            log.trace("Unsupported JWT token trace: {}", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT token compact of handler are invalid.");
            log.trace("JWT token compact of handler are invalid trace: {}", e);
        }
        return false;
    }
}
