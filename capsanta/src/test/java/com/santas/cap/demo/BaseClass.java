package com.santas.cap.demo;

import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

@AutoConfigureMockMvc
@SpringBootTest(classes = SignageApp.class)
@RunWith(SpringRunner.class)
public class BaseClass {

    @MockBean
    SignageRepository signageRepository;

    @Autowired
    SignageRestController signageRestController;

    @Before
    public void setupMockReturnData() {
        Supplier<List<Signage>> sigSupply = () -> Arrays.asList(
                new Signage(1L, "DOC1", "PERSON1", 123456L),
                new Signage(2L, "DOC1", "PERSON2", 123456L)
        );

        Mockito.when(signageRepository.getByDocId(Mockito.anyString()))
                .thenReturn(sigSupply.get());

        RestAssuredMockMvc.standaloneSetup(signageRestController);
    }

}
