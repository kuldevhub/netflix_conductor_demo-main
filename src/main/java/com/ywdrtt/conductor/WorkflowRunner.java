package com.ywdrtt.conductor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.ywdrtt.conductor.service.WorkflowService;

@Component
public class WorkflowRunner implements CommandLineRunner {

    private final WorkflowService workflowService;

    public WorkflowRunner(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Register the workflow and start it
    	System.out.println("Registering a flow");
        workflowService.registerAndStartWorkflow("demo_fork_join8", 1);
    }
}

