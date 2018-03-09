package com.mypieceofcode.evfinder.config;

import com.mypieceofcode.evfinder.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfig{


    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http){
       return http
               .csrf().disable()
                .authorizeExchange()
                .pathMatchers("/events", "/users").authenticated()
                .and().authorizeExchange().anyExchange().permitAll()
               .and()
               .httpBasic()
               .securityContextRepository(new WebSessionServerSecurityContextRepository())
               .and().formLogin().and().logout().logoutSuccessHandler(this::logoutHandler).and()
               .build();
    }


    // TODO: 08.03.2018 remove just for tests

    private Mono<AuthorizationDecision> currentUserMatchesPath(Mono<Authentication> authentication, AuthorizationContext context) {
        return authentication
                .map(a -> context.getVariables().get("user").equals(a.getName()))
                .map(AuthorizationDecision::new);
    }
    @Bean
    PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    private Mono<Void> logoutHandler(WebFilterExchange webFilterExchange, Authentication authentication) {
        ServerHttpResponse response = webFilterExchange.getExchange().getResponse();
        response.setStatusCode(HttpStatus.SEE_OTHER);
        response.getHeaders().add(HttpHeaders.LOCATION, "/login");
        return response.setComplete();
    }



}
