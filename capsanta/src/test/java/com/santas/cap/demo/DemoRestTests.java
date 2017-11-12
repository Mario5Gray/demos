package com.santas.cap.demo;

import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@SpringBootTest(classes = SignageApp.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@RunWith(SpringRunner.class)
public class DemoRestTests {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    SignageRepository signageRepository;

    @Before
    public void setupData() {
        Supplier<List<Signage>> sigSupply = () -> Arrays.asList(
                new Signage(1L, "DOC1", "PERSON1", 123456L),
                new Signage(2L, "DOC1", "PERSON2", 123456L)
        );

        Mockito.when(signageRepository.findAll()).thenReturn(sigSupply.get());
        Mockito.when(signageRepository.getByDocId(Mockito.anyString())).thenReturn(sigSupply.get());
    }

    @Test
    @SneakyThrows
    public void testShouldRetrieveJsondata() {
        mockMvc.perform(get("/byDocId/DOC1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("@.[0].id").value(1L))
                .andExpect(jsonPath("@.[1].id").value(2L));
    }
}
