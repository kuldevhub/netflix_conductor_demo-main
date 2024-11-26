package com.ywdrtt.conductor.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import com.netflix.conductor.client.http.WorkflowClient;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.ywdrtt.conductor.model.Item;
import com.ywdrtt.conductor.model.Wrapper;

@SpringBootTest
public class SubmissionWorkflowServiceTest {

	@Mock
	private WorkflowClient workflowClient;

	@Mock
	private WorkflowDef fileSubmissionWorkflowDef;;

	@InjectMocks
	private FileSubmissionWorkflowService fileSubmissionWorkflowService;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);

		// Mock WorkflowDef properties
		when(fileSubmissionWorkflowDef.getName()).thenReturn("demo_fork_join2");
		when(fileSubmissionWorkflowDef.getVersion()).thenReturn(1);
	}

	@Test
	public void testRegisterAndStartWorkflow() throws IOException {
		// Define the input parameters you will pass to the workflow
		// Load the file from resources
		ClassPathResource resource = new ClassPathResource("sample.pdf");
		byte[] fileBytes = Files.readAllBytes(resource.getFile().toPath());

		List<Item> itemList = Arrays.asList(new Item("book", 5, "processing", 50, fileBytes),
				new Item("computer", 2, "processing", 500, fileBytes), new Item("phone", 4, "processing", 700, fileBytes));

		Wrapper wrapper = new Wrapper(itemList); // Wrap the list inside the Wrapper object

		Map<String, Object> inputData = Map.of("wrapper", wrapper // Passing the Wrapper object
		);

		Map<String, Object> inputParameters = new HashMap<>();
		inputParameters.put("wrapper", wrapper); // Example input, replace with actual wrapper data

		// Call the method to be tested
//        fileSubmissionWorkflowService.registerAndStartWorkflow("demo_fork_join", 1);

		// Verify that the registerWorkflowDef method was called with the correct
		// WorkflowDef object
//        verify(fileSubmissionWorkflowService, times(1)).registerWorkflowDef(fileSubmissionWorkflowDef);

		// Verify that the startWorkflow method was called with the correct arguments
		verify(workflowClient, times(1)).startWorkflow(null);
	}
}
