package com.example.book;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;


@RunWith(SpringRunner.class)
@SpringBootTest( classes = BookApplication.class,
 		 webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class BookApplicationTests {

	@Autowired
	MockMvc mockMvc;
	
	@Test
	public void testAccessRestService() throws Exception {
		this.mockMvc.perform(get("/available"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.TEXT_PLAIN))
				.andExpect(content().string("Spring is available"));
	}

}
