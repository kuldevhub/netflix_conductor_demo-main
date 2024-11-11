package com.ywdrtt.conductor.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.netflix.conductor.client.http.MetadataClient;
import com.netflix.conductor.client.http.WorkflowClient;
import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.StartWorkflowRequest;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.ywdrtt.conductor.model.Item;
import com.ywdrtt.conductor.model.Wrapper;

@Service
public class WorkflowService {

	private final MetadataClient metadataClient;
	private final WorkflowClient workflowClient;

	public WorkflowService(MetadataClient metadataClient, WorkflowClient workflowClient) {
		this.metadataClient = metadataClient;
		this.workflowClient = workflowClient;
	}

	public void registerAndStartWorkflow(String workflowName, int version) {
		// Create workflow definition
		WorkflowDef workflowDef = new WorkflowDef();
		workflowDef.setName(workflowName);
		workflowDef.setVersion(version);
		workflowDef.setDescription("A simple workflow that lists the items in wrapper java object");

		List<WorkflowTask> workflowTasks = new ArrayList<>();

		WorkflowTask forkTask = new WorkflowTask();

		forkTask.setName("dynamic_fork");

		forkTask.setTaskReferenceName("fork_task");

		forkTask.setType(TaskType.FORK_JOIN.name());

		// List to hold tasks that run in parallel
		List<List<WorkflowTask>> forkedTasks = new ArrayList<>();

		List<Item> items = getItemsToProcess();

		for (int i = 0; i < items.size(); i++) {
			WorkflowTask processTask = new WorkflowTask();

			processTask.setName("process_item_task");

			processTask.setTaskReferenceName("process_item_task" + i);

			processTask.setType(TaskType.SIMPLE.name());

			processTask
					.setInputParameters(Collections.singletonMap("item", "${workflow.input.wrapper.items[" + i + "]}"));

			// Each task list here represents a parallel task
			forkedTasks.add(Collections.singletonList(processTask));
		}

		forkTask.setForkTasks(forkedTasks);

		workflowTasks.add(forkTask);

		// Define the list of tasks to join on (e.g., all process_item_task instances)
		List<String> joinOnTasks = forkedTasks.stream()
		    .map(tasks -> tasks.get(0).getTaskReferenceName())  // Collect the reference names
		    .collect(Collectors.toList());
		
		// JOIN Task Setup - waits for all forked tasks to complete
		WorkflowTask joinTask = new WorkflowTask();
		joinTask.setName("join_task");
		joinTask.setTaskReferenceName("join_task");
		joinTask.setType("JOIN");
//        joinTask.setJoinOn(forkedTasks.stream().map(tasks -> tasks.get(0).getTaskReferenceName()).toList());

		joinTask.setJoinOn(
				forkedTasks.stream().map(tasks -> tasks.get(0).getTaskReferenceName()).collect(Collectors.toList()));

		joinTask.setJoinOn(joinOnTasks);
		
		workflowTasks.add(joinTask);

		// Aggregate results task after joining
		WorkflowTask aggregateResultsTask = new WorkflowTask();
		aggregateResultsTask.setName("aggregate_results");
		aggregateResultsTask.setTaskReferenceName("aggregate_results");
		aggregateResultsTask.setType("SIMPLE");
		
		// Pass the `processedItem` outputs from `process_item_task` instances as well as the `Wrapper` object
		aggregateResultsTask.setInputParameters(Map.of(
		    "items", joinOnTasks.stream()
		               .map(taskRef -> "${" + taskRef + ".output.processedItem}")
		               .collect(Collectors.toList()),
		    "wrapper", "${workflow.input.wrapper}"  // Pass the entire Wrapper object
		));

		
//		aggregateResultsTask.setInputParameters(Collections.singletonMap("items", "${workflow.input.wrapper.items}"));
		
		

		workflowTasks.add(aggregateResultsTask);

		// Add tasks to workflow definition
		workflowDef.setTasks(workflowTasks);

		// Set workflow output if needed
//		workflowDef.setOutputParameters(Map.of("finalWrapper", "${aggregateResults_ref.output.wrapper}"));

		// Register the workflow definition with Conductor if not already registered
		WorkflowDef existingDef = getWorkflowDefinition(workflowName, version);
		if (existingDef == null) {
			metadataClient.registerWorkflowDef(workflowDef);
			System.out.println("Workflow registered: " + workflowDef.getName());
		} else {
			System.out.println("Workflow already registered: " + existingDef.getName());
		}

		// Start the workflow with input data
		startWorkflow(workflowName, version);
	}

	private List<Item> getItemsToProcess() {
		List<Item> itemList = Arrays.asList(new Item("book", 5, "processing", 50),
				new Item("computer", 2, "processing", 500), new Item("phone", 4, "processing", 700));

		return itemList;
	}

	private WorkflowDef getWorkflowDefinition(String workflowName, int version) {
		try {
			WorkflowDef workflowDef = metadataClient.getWorkflowDef(workflowName, version);
			if (workflowDef == null) {
				System.out.println("Workflow definition not found: " + workflowName + " version: " + version);
			}
			return workflowDef;
		} catch (Exception e) {
			// Handle any errors (e.g., network issues, unexpected errors)
			System.out.println("Error while fetching workflow definition: " + e.getMessage());
			return null;
		}
	}

	private void startWorkflow(String workFlowName, int version) {
		// Create the input data
//		Map<String, Object> inputData = Map.of("num1", 5, "num2", 10);

		List<Item> itemList = Arrays.asList(new Item("book", 5, "processing", 50),
				new Item("computer", 2, "processing", 500), new Item("phone", 4, "processing", 700));

		Wrapper wrapper = new Wrapper(itemList); // Wrap the list inside the Wrapper object

		Map<String, Object> inputData = Map.of("wrapper", wrapper // Passing the Wrapper object
		);

		// Start the workflow
		StartWorkflowRequest startWorkflowRequest = new StartWorkflowRequest();
		startWorkflowRequest.setName(workFlowName);
		startWorkflowRequest.setVersion(version);
		startWorkflowRequest.setInput(inputData);

		// Start the workflow
		String workflowId = workflowClient.startWorkflow(startWorkflowRequest);
		System.out.println("Workflow started with ID: " + workflowId);
	}

}
