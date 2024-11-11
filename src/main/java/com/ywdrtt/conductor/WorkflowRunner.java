package com.ywdrtt.conductor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.ywdrtt.conductor.model.Item;
import com.ywdrtt.conductor.model.Wrapper;
import com.ywdrtt.conductor.service.FileSubmissionWorkflowService;

@Component
public class WorkflowRunner implements CommandLineRunner {

	@Autowired
    private final FileSubmissionWorkflowService fileSubmissionWorkflowService;

    public WorkflowRunner(FileSubmissionWorkflowService fileSubmissionWorkflowService) {
        this.fileSubmissionWorkflowService = fileSubmissionWorkflowService;
    }

    @Override
    public void run(String... args) throws Exception {
    	
    	
		List<Item> itemList = Arrays.asList(new Item("book", 5, "processing", 50),
				new Item("computer", 2, "processing", 500), new Item("phone", 4, "processing", 700));

		Wrapper wrapper = new Wrapper(itemList); // Wrap the list inside the Wrapper object

		Map<String, Object> inputData = Map.of("wrapper", wrapper // Passing the Wrapper object
		);
		
        // Register the workflow and start it
    	System.out.println("Registering a flow");
    	fileSubmissionWorkflowService.registerAndStartWorkflow("demo_fork_join", 1, inputData);
    }
}

