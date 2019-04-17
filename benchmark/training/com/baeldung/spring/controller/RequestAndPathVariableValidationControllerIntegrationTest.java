package com.baeldung.spring.controller;


import com.baeldung.spring.ClientWebConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ClientWebConfig.class)
@WebAppConfiguration
public class RequestAndPathVariableValidationControllerIntegrationTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Test
    public void getNameOfDayByNumberRequestParam_whenGetWithProperRequestParam_thenReturn200() throws Exception {
        mockMvc.perform(get("/public/api/1/name-for-day").param("dayOfWeek", Integer.toString(5))).andExpect(status().isOk());
    }

    @Test
    public void getNameOfDayByNumberRequestParam_whenGetWithRequestParamOutOfRange_thenReturn400() throws Exception {
        mockMvc.perform(get("/public/api/1/name-for-day").param("dayOfWeek", Integer.toString(15))).andExpect(status().isBadRequest());
    }

    @Test
    public void getNameOfDayByPathVariable_whenGetWithProperRequestParam_thenReturn200() throws Exception {
        mockMvc.perform(get("/public/api/1/name-for-day/{dayOfWeek}", Integer.toString(5))).andExpect(status().isOk());
    }

    @Test
    public void getNameOfDayByPathVariable_whenGetWithRequestParamOutOfRange_thenReturn400() throws Exception {
        mockMvc.perform(get("/public/api/1/name-for-day/{dayOfWeek}", Integer.toString(15))).andExpect(status().isBadRequest());
    }

    @Test
    public void validStringRequestParam_whenGetWithProperRequestParam_thenReturn200() throws Exception {
        mockMvc.perform(get("/public/api/1/valid-name").param("name", "John")).andExpect(status().isOk());
    }

    @Test
    public void validStringRequestParam_whenGetWithTooLongRequestParam_thenReturn400() throws Exception {
        mockMvc.perform(get("/public/api/1/valid-name").param("name", "asdfghjklqw")).andExpect(status().isBadRequest());
    }

    @Test
    public void validStringRequestParam_whenGetWithLowerCaseRequestParam_thenReturn400() throws Exception {
        mockMvc.perform(get("/public/api/1/valid-name").param("name", "john")).andExpect(status().isBadRequest());
    }
}
