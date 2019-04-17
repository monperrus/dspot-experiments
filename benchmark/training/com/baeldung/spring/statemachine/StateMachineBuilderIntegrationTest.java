package com.baeldung.spring.statemachine;


import StateMachineBuilder.Builder;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;


public class StateMachineBuilderIntegrationTest {
    @Test
    public void whenUseStateMachineBuilder_thenBuildSuccessAndMachineWorks() throws Exception {
        Builder<String, String> builder = StateMachineBuilder.builder();
        builder.configureStates().withStates().initial("SI").state("S1").end("SF");
        builder.configureTransitions().withExternal().source("SI").target("S1").event("E1").and().withExternal().source("S1").target("SF").event("E2");
        StateMachine machine = builder.build();
        machine.start();
        machine.sendEvent("E1");
        Assert.assertEquals("S1", machine.getState().getId());
        machine.sendEvent("E2");
        Assert.assertEquals("SF", machine.getState().getId());
    }
}
