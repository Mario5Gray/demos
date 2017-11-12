package com.santas.cap.demo;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class DemoEntityTests {

    @Test
    public void testShouldCreateEntity() {
        Signage signage = new Signage(1L, "DOCHASH", "MYHASH", 123456L);

        Assertions.assertThat(signage).isNotNull();
        Assertions.assertThat(signage.getDocId()).isNotNull();
        Assertions.assertThat(signage.getSignerId()).isEqualTo("MYHASH");
        Assertions.assertThat(signage.getTimeInMillis()).isNotNull();
    }

    @Test
    public void testShouldCreateUser() {
        User user = new User(1L, "ACCOUNTID", "MARIO", "818-555-1234");

        Assertions.assertThat(user).isNotNull();
        Assertions.assertThat(user.getId()).isEqualTo(1L);
        Assertions.assertThat(user.getAccountId()).isEqualTo("ACCOUNTID");
        Assertions.assertThat(user.getName()).isEqualTo("MARIO");
        Assertions.assertThat(user.getTele()).isEqualTo("818-555-1234");
    }
}
