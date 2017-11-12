package com.santas.cap.demo;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@SpringBootTest(classes = SignageApp.class)
@RunWith(SpringRunner.class)
public class DemoCapOneClientTest {

    @Autowired
    CapClient capClient;

    @Test
    @SneakyThrows
    public void shouldCallCapOne() {
        CapAuth capAuth = capClient.accessToken();

        Assertions.assertThat(capAuth).isNotNull();
        Assertions.assertThat(capAuth.getAccessToken()).isNotNull();
    }

}
