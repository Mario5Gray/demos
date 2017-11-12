package com.santas.cap.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Collection;

@RestController
public class SignageRestController {

    @Autowired
    SignageRepository signageRepository;

    @GetMapping("/byDocId/{docId}")
    public Collection<Signage> getByDocId(@PathVariable("docId") String docId) {
        return signageRepository.getByDocId(docId);
    }
}

@RestController
class SignageUserRestController {

    @Autowired
    UserRepository userRepository;

    @GetMapping("/user/{username}")
    public User getByUsername(@PathVariable("userName") String username, Principal principal) {
        return userRepository.getUsersByName(username);
    }
}