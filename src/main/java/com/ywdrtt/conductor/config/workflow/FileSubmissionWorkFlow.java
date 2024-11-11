package com.ywdrtt.conductor.config.workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.netflix.conductor.client.http.MetadataClient;
import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.ywdrtt.conductor.model.Item;

@Configuration
public class FileSubmissionWorkFlow {

	private final MetadataClient metadataClient;
	public FileSubmissionWorkFlow(MetadataClient metadataClient) {
		this.metadataClient = metadataClient;
	}
	
//	@Bean
//	public String workflowName() {
//	    return "demo_fork_join";
//	}
	
	@Bean
	public WorkflowDef fileSubmissionWorkflowDef(@Value("${workflow.file-submission.name}") String workflowName, @Value("${version}") int version) {

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

	private List<Item> getItemsToProcess() {
		List<Item> itemList = Arrays.asList(new Item("book", 5, "processing", 50),
				new Item("computer", 2, "processing", 500), new Item("phone", 4, "processing", 700));

		return itemList;
	}


}