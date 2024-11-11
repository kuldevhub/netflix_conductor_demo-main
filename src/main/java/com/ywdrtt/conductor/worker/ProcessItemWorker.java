package com.ywdrtt.conductor.worker;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;

@Component
public class ProcessItemWorker implements Worker {

	@Override
	public String getTaskDefName() {
		return "process_item_task"; // This should match the task name defined in the workflow
	}

	@Override
	public TaskResult execute(Task task) {
		// Retrieve input data from the task
		Map<String, Object> inputData = task.getInputData();
		Map<String, Object> itemData = (Map<String, Object>) inputData.get("item");

		// Assuming the Item class has fields like name, status, and count
		String name = (String) itemData.get("name");
		String status = (String) itemData.get("status");
		int count = (int) itemData.get("count");
		
		// Process the item (For example, print it or perform some operation)
		System.out.println("Processing item: " + name + " with status: " + status + " and count: " + count);

		count += 2;
		
		System.out.println("Processing item: Add 2 to count : " + count);
		
		
		itemData.put("count", count);
		
		// Create the result for this task
		TaskResult result = new TaskResult(task);
		result.setStatus(TaskResult.Status.COMPLETED);

		// You can set some output data if needed
		result.getOutputData().put("processedItem", itemData);

		return result;
	}
}
