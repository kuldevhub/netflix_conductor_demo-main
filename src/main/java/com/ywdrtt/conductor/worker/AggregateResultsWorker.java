package com.ywdrtt.conductor.worker;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;

@Component
public class AggregateResultsWorker implements Worker {

	@Override
	public String getTaskDefName() {
		return "aggregate_results"; // This should match the task name defined in the workflow
	}

	@Override
	public TaskResult execute(Task task) {
		// Retrieve the list of items from the task input
//        Map<String, Object> inputData = task.getInputData();
		// Retrieve the list of updated items from the task input
		List<Map<String, Object>> items = (List<Map<String, Object>>) task.getInputData().get("items");

		// Retrieve the entire Wrapper object from the task input
		Map<String, Object> wrapperData = (Map<String, Object>) task.getInputData().get("wrapper");

		// Perform aggregation or any processing you need on the list of items
		System.out.println("Aggregating results for " + items.size() + " items.");
		int totalItemCount = 0;
		for (Map<String, Object> item : items) {
			String name = (String) item.get("name");
			String status = (String) item.get("status");
			int count = (int) item.get("count");
			totalItemCount += count;
			// Example of aggregation logic (e.g., print each item's details)
			System.out.println("Item: " + name + ", Status: " + status + ", Count: " + count);
		}

		// Example of accessing data from the Wrapper object
		int totalItems = (int) wrapperData.get("totalItems");
		
		totalItems += totalItemCount;
		wrapperData.put("totalItems", totalItems);

		// Create the result for this task
		TaskResult result = new TaskResult(task);
		result.setStatus(TaskResult.Status.COMPLETED);

		// Set output data if needed (e.g., aggregated summary)
		result.getOutputData().put("summary",
				"Aggregation complete for " + items.size() + " items. Total Count For all Items :" + totalItems);
		System.out.println("Wrapper totalItems: " + totalItems);
		result.getOutputData().put("wrapperData", wrapperData);

		return result;
	}
}
