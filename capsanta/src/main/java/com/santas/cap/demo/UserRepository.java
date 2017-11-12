package com.santas.cap.demo;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>{
    User getUsersByName(String name);
    User getUsersByAccountId(String accountId);
}
