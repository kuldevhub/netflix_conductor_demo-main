//package com.ywdrtt.conductor.service;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.io.Resource;
//
//import com.fasterxml.jackson.core.exc.StreamReadException;
//import com.fasterxml.jackson.databind.DatabindException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.netflix.conductor.client.http.MetadataClient;
//import com.netflix.conductor.client.http.WorkflowClient;
//import com.netflix.conductor.common.metadata.tasks.TaskDef;
//import com.netflix.conductor.common.metadata.workflow.StartWorkflowRequest;
//import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
//import com.ywdrtt.conductor.model.Item;
//import com.ywdrtt.conductor.model.Wrapper;
//
////@Service
//public class FileSubmissionWorkflowService2 {
//
//	private final MetadataClient metadataClient;
//	private final WorkflowClient workflowClient;
////	private final WorkflowDef fileSubmissionWorkflowDef;
//
////	@Autowired
////	FileSubmissionWorkFlow fileSubmissionWorkFlow;
//
//	@Value("classpath:dynamic_fork_join_workflow.json")
//	private Resource workflowResource;
//	
//	private final ObjectMapper objectMapper;
//
//	@Autowired
//	public FileSubmissionWorkflowService(WorkflowClient workflowClient,
////			WorkflowDef fileSubmissionWorkflowDef,
//			MetadataClient metadataClient, ObjectMapper objectMapper) {
//		this.workflowClient = workflowClient;
////		this.fileSubmissionWorkflowDef = fileSubmissionWorkflowDef;
//		this.metadataClient = metadataClient;
//		this.objectMapper = objectMapper;
//	}
//
//	public void registerAndStartWorkflow(String workflowName, int version, Map<String, Object> inputDataa)
//			throws StreamReadException, DatabindException, IOException {
//
//		
//		  TaskDef processItemTaskDef = new TaskDef();
//          processItemTaskDef.setName("process_item_task");
//          processItemTaskDef.setDescription("Task to process each item dynamically");
//          processItemTaskDef.setTimeoutSeconds(120);          // Timeout for task execution
//          processItemTaskDef.setRetryCount(3);                // Number of retries
//          processItemTaskDef.setRetryDelaySeconds(10);        // Delay between retries
//          processItemTaskDef.setResponseTimeoutSeconds(60);   // Time within which response is expected
//
//          // Register task definition
//          metadataClient.registerTaskDefs(List.of(processItemTaskDef));
//
//          System.out.println("process_item_task has been registered successfully.");
//	        
//	        
//		// Set workflow output if needed
////		workflowDef.setOutputParameters(Map.of("finalWrapper", "${aggregateResults_ref.output.wrapper}"));
//
//		Wrapper wrapper = (Wrapper) inputDataa.get("wrapper");
//
//		// List to hold task definitions for each item
//		List<Map<String, Object>> dynamicTasks = new ArrayList<>();
//		Map<String, Map<String, Object>> dynamicTasksInput = new HashMap<>();
//
//		// Create WorkflowTask configurations for each item in wrapper.items
//		for (int i = 0; i < wrapper.getItems().size(); i++) {
//			Item item = wrapper.getItems().get(i);
//
//			// Define a unique task reference name for each item
//			String taskRefName = "process_item_task_" + i;
//
//			// Define the task configuration as a Map
//			Map<String, Object> taskConfig = new HashMap<>();
//			taskConfig.put("name", "process_item_task");
//			taskConfig.put("taskReferenceName", taskRefName);
//			taskConfig.put("type", "SIMPLE");
//
//			// Add the task configuration to the list of dynamicTasks
//			dynamicTasks.add(taskConfig);
//
//			// Input parameters for each task
//			Map<String, Object> inputParams = new HashMap<>();
//			inputParams.put("item", item);
//
//			// Add the input parameters for this task to dynamicTasksInput map
//			dynamicTasksInput.put(taskRefName, inputParams);
//		}
//
//		// Prepare input data for the workflow
//		Map<String, Object> inputData = new HashMap<>();
//		inputData.put("wrapper", wrapper);
//		inputData.put("dynamicTasks", dynamicTasks); // List of task configurations
//		inputData.put("dynamicTasksInput", dynamicTasksInput); // Map of input parameters per task
//
//		WorkflowDef workflowDef = objectMapper.readValue(workflowResource.getInputStream(), WorkflowDef.class);
////        registerWorkflow(workflowDef);
//
//		// Register the workflow definition with Conductor if not already registered
//		WorkflowDef existingDef = getWorkflowDefinition(workflowDef.getName(), workflowDef.getVersion());
//		if (existingDef == null) {
////			metadataClient.registerWorkflowDef(fileSubmissionWorkFlow.fileSubmissionWorkflowDef(workflowName, version, inputData));
//			metadataClient.registerWorkflowDef(workflowDef);
////			workflowDef
//			System.out.println("Workflow registered: " + workflowName);
//		} else {
//			System.out.println("Workflow already registered: " + existingDef.getName());
//		}
//
//		System.out.println("Workflow registered Name: " + workflowDef.getName());
//
//		System.out.println("Workflow registered Version: " + workflowDef.getVersion());
//
//		// Start the workflow with input data
//		startWorkflow(workflowDef.getName(), workflowDef.getVersion(), inputData);
//	}
//
//	private List<Item> getItemsToProcess(Map<String, Object> inputData) {
//		Wrapper wrapper = (Wrapper) inputData.get("wrapper");
//		return wrapper.getItems();
//	}
//
//	private void startWorkflow(String workFlowName, int version, Map<String, Object> inputData) {
//
//		// Start the workflow
//		StartWorkflowRequest startWorkflowRequest = new StartWorkflowRequest();
//		startWorkflowRequest.setName(workFlowName);
//		startWorkflowRequest.setVersion(version);
//		startWorkflowRequest.setInput(inputData);
//
//		// Start the workflow
//		String workflowId = workflowClient.startWorkflow(startWorkflowRequest);
//		System.out.println("Workflow started with ID: " + workflowId);
//	}
//
//	private WorkflowDef getWorkflowDefinition(String workflowName, int version) {
//		try {
//			WorkflowDef workflowDef = metadataClient.getWorkflowDef(workflowName, version);
//			if (workflowDef == null) {
//				System.out.println("Workflow definition not found: " + workflowName + " version: " + version);
//			}
//			return workflowDef;
//		} catch (Exception e) {
//			// Handle any errors (e.g., network issues, unexpected errors)
//			System.out.println("Error while fetching workflow definition: " + e.getMessage());
//			return null;
//		}
//	}
//
//}
