package com.ywdrtt.conductor.config.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.netflix.conductor.client.http.MetadataClient;
import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.ywdrtt.conductor.model.Item;
import com.ywdrtt.conductor.model.Wrapper;

@Component
public class FileSubmissionWorkFlow {
	@Autowired
	private MetadataClient metadataClient;

//	public FileSubmissionWorkFlow(MetadataClient metadataClient) {
//		this.metadataClient = metadataClient;
//	}

	public WorkflowDef fileSubmissionWorkflowDef(String workflowName, int version, Map<String, Object> inputData) {
		// Create workflow definition
		WorkflowDef workflowDef = new WorkflowDef();
		workflowDef.setName(workflowName);
		workflowDef.setVersion(version);
		workflowDef.setDescription("A simple workflow that lists the items in wrapper java object");
//		workflowDef.setTimeoutSeconds(120);
		workflowDef.setOwnerEmail("test@test.com");
		// Initialize Task Definition with required properties
//		TaskDef processItemTaskDef = new TaskDef();
//		processItemTaskDef.setName("process_item_task");
//		processItemTaskDef.setTimeoutSeconds(120);        // Task timeout
//		processItemTaskDef.setRetryDelaySeconds(10);      // Delay between retries
//		processItemTaskDef.setRetryCount(3);              // Number of retries

		// Register TaskDef with Conductor
//		WorkflowClient workflowClient = new WorkflowClient();
//		workflowClient.registerTaskDefs(List.of(processItemTaskDef));
		/*
		 * Once the task definition is registered with process_item_task name, can
		 * reference it in your workflow definition:
		 */
		
		TaskDef processItemTaskDef = new TaskDef();
		processItemTaskDef.setName("process_item_task");
		processItemTaskDef.setDescription("Task to process each item dynamically");
		processItemTaskDef.setTimeoutSeconds(120);          // Timeout for task execution
		processItemTaskDef.setRetryCount(3);                // Number of retries
		processItemTaskDef.setRetryDelaySeconds(10);        // Delay between retries
		processItemTaskDef.setResponseTimeoutSeconds(60);   // Time within which response is expected
		processItemTaskDef.setOwnerEmail("Test@test.com");
      // Register task definition
      metadataClient.registerTaskDefs(List.of(processItemTaskDef));

      System.out.println("process_item_task has been registered successfully.");
		
		
		

		List<WorkflowTask> workflowTasks = new ArrayList<>();

		WorkflowTask forkTask = new WorkflowTask();

		forkTask.setName("dynamic_fork");

		forkTask.setTaskReferenceName("fork_task");

		forkTask.setType(TaskType.FORK_JOIN.name());

		// List to hold tasks that run in parallel
		List<List<WorkflowTask>> forkedTasks = new ArrayList<>();

		List<Item> items = getItemsToProcess(inputData);

		for (int i = 0; i < items.size(); i++) {
			WorkflowTask processTask = new WorkflowTask();

			processTask.setName("process_item_task");

			processTask.setTaskReferenceName("process_item_task" + i);

			processTask.setType(TaskType.SIMPLE.name());

			processTask
					.setInputParameters(Collections.singletonMap("item", "${workflow.input.wrapper.items[" + i + "]}"));

			processTask.setRetryCount(3);

			// Each task list here represents a parallel task
			forkedTasks.add(Collections.singletonList(processTask));
		}

		forkTask.setForkTasks(forkedTasks);

		workflowTasks.add(forkTask);

		// Define the list of tasks to join on (e.g., all process_item_task instances)
		List<String> joinOnTasks = forkedTasks.stream().map(tasks -> tasks.get(0).getTaskReferenceName()) // Collect the
																											// reference
																											// names
				.collect(Collectors.toList());

		// JOIN Task Setup - waits for all forked tasks to complete
		WorkflowTask joinTask = new WorkflowTask();
		joinTask.setName("join_task");
		joinTask.setTaskReferenceName("join_task");
		joinTask.setType("JOIN");
//		        joinTask.setJoinOn(forkedTasks.stream().map(tasks -> tasks.get(0).getTaskReferenceName()).toList());

		joinTask.setJoinOn(
				forkedTasks.stream().map(tasks -> tasks.get(0).getTaskReferenceName()).collect(Collectors.toList()));

		joinTask.setJoinOn(joinOnTasks);

		workflowTasks.add(joinTask);

		// Aggregate results task after joining
		WorkflowTask aggregateResultsTask = new WorkflowTask();
		aggregateResultsTask.setName("aggregate_results");
		aggregateResultsTask.setTaskReferenceName("aggregate_results");
		aggregateResultsTask.setType("SIMPLE");

		// Pass the `processedItem` outputs from `process_item_task` instances as well
		// as the `Wrapper` object
		aggregateResultsTask.setInputParameters(
				Map.of("items", joinOnTasks.stream().map(taskRef -> "${" + taskRef + ".output.processedItem}")
						.collect(Collectors.toList()), "wrapper", "${workflow.input.wrapper}" // Pass the entire Wrapper
																								// object
				));

//				aggregateResultsTask.setInputParameters(Collections.singletonMap("items", "${workflow.input.wrapper.items}"));

		workflowTasks.add(aggregateResultsTask);

		// Add tasks to workflow definition
		workflowDef.setTasks(workflowTasks);

		return workflowDef;

	}

	private List<Item> getItemsToProcess(Map<String, Object> inputData) {
		Wrapper wrapper = (Wrapper) inputData.get("wrapper");
		return wrapper.getItems();
	}

}
