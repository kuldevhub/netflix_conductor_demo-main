package com.ywdrtt.conductor.worker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;

public class AggregateResultsWorker2  implements Worker {

	@Override
	public String getTaskDefName() {
		return "aggregate_results"; // This should match the task name defined in the workflow
	}

	@Override
	public TaskResult execute(Task task) {
//		List<Map<String, Object>> items = (List<Map<String, Object>>) task.getInputData().get("items");

		// Retrieve the entire Wrapper object from the task input
		Map<String, Object> wrapperData = (Map<String, Object>) task.getInputData().get("wrapper");
		Map<String, Object> taskInputItems = (Map<String, Object>) task.getInputData().get("items");
		
		List<Map<String, Object>> items = new ArrayList<Map<String,Object>>();
		
		 for (Entry<String, Object> entry : taskInputItems.entrySet()) {
			  System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
			    Map<String, Object> processedItems = (Map<String, Object>) taskInputItems.get(entry.getKey());
			    
			    for (Entry<String, Object> itemsEntrySet : processedItems.entrySet()) {
			    	System.out.println("Key : " + itemsEntrySet.getKey() + " Value : " + itemsEntrySet.getValue());
			    	
			    	Map<String, Object> item = new HashMap<>();
			    	item.put(itemsEntrySet.getKey(), itemsEntrySet.getValue());
			    	items.add(item);
			    }
		 }
		
		
//		 Map<String, Object> processedItem = (Map<String, Object>) entry.getValue();
         
//		List<Map<String, Object>> items = items.;
		
		
//		List<Map<String, Object>> items =  (List<Map<String, Object>>) wrapperData.get("items");

		// Perform aggregation or any processing you need on the list of items
		System.out.println("Aggregating results for " + items.size() + " items.");
		int totalItemCount = 0;
		for (Map<String, Object> item : items) {
			Object processedItem = item.get("processedItem");
			String name = (String) ((Map<String, Object>) processedItem).get("name");
			String status = (String) ((Map<String, Object>) processedItem).get("status");
			int count = (int) ((Map<String, Object>) processedItem).get("count");
			totalItemCount += count;
			// Example of aggregation logic (e.g., print each item's details)
			System.out.println("Item: " + name + ", Status: " + status + ", Count: " + count);
		}

		// Example of accessing data from the Wrapper object
		int totalItems = (int) wrapperData.get("totalItems");
//		System.out.println("Wrapper totalItems: " + totalItems);
		totalItems += totalItemCount;
		wrapperData.put("totalItems", totalItems);

		// Create the result for this task
		TaskResult result = new TaskResult(task);
		result.setStatus(TaskResult.Status.COMPLETED);

		
		System.out.println("summary :Aggregation complete for " + items.size() + " items. Total Count For all Items :" + totalItems);
		// Set output data if needed (e.g., aggregated summary)
		result.getOutputData().put("summary",
				"Aggregation complete for " + items.size() + " items. Total Count For all Items :" + totalItems);

		result.getOutputData().put("wrapperData", wrapperData);

		return result;
	}

}
