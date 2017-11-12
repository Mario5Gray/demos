package com.santas.cap.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.config.annotation.builders.ClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@EnableResourceServer
@SpringBootApplication
public class SignageApp {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(SignageApp.class, args);
    }
}

@Configuration
@EnableAuthorizationServer
class SignageAuthorizationConfiguration {
    AuthenticationManager authMan;

    public SignageAuthorizationConfiguration(AuthenticationManager am) {
        this.authMan = am;
    }

    public ClientDetailsServiceBuilder.ClientBuilder
    configure(ClientDetailsServiceConfigurer clientDetail) throws Exception {
        return clientDetail.inMemory()
                .withClient("client1234")
                .scopes("signage")
                .secret("password")
                .authorizedGrantTypes("password");

    }

    public AuthorizationServerEndpointsConfigurer configure(AuthorizationServerEndpointsConfigurer authConfig) {
        return authConfig.authenticationManager(this.authMan);
    }

}

@Component
class SignageUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        User u = userRepository.getUsersByName(s);

        return org.springframework.security.core.userdetails.User
                .withUsername(u.getName())
                .roles("USER")
                .password("simplepassword")
                .build();
    };
}


