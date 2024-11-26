package com.ywdrtt.conductor.config;

import java.util.List;

import javax.annotation.PreDestroy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.netflix.conductor.client.automator.TaskRunnerConfigurer;
import com.netflix.conductor.client.http.MetadataClient;
import com.netflix.conductor.client.http.TaskClient;
import com.netflix.conductor.client.http.WorkflowClient;
import com.netflix.conductor.client.worker.Worker;

@Configuration
public class ConductorWorkerConfig {

	private TaskRunnerConfigurer taskRunnerConfigurer;

	private static final String CONDUCTOR_SERVER_URL = "http://localhost:8080/api/"; // Set this to your Conductor
																						// server URL

	@Bean
	public TaskClient taskClient() {
		TaskClient taskClient = new TaskClient();
		taskClient.setRootURI(CONDUCTOR_SERVER_URL); // Set your Conductor server URL here
		return taskClient;
	}

	@Bean
	public WorkflowClient workflowClient() {
		WorkflowClient workflowClient = new WorkflowClient();
		workflowClient.setRootURI(CONDUCTOR_SERVER_URL); // Set to your Conductor server's URI
		return workflowClient;
	}

	@Bean
	public MetadataClient metadataClient() {
		MetadataClient metadataClient = new MetadataClient();
		metadataClient.setRootURI(CONDUCTOR_SERVER_URL); // Set to your Conductor server's URI
		return metadataClient;
	}

	@Bean
	public TaskRunnerConfigurer taskRunnerConfigurer(TaskClient taskClient, List<Worker> workers) {
        this.taskRunnerConfigurer = new TaskRunnerConfigurer.Builder(taskClient, workers)
        		.withThreadCount(1)
        		.build();
        this.taskRunnerConfigurer.init(); // Starts polling for tasks
        return this.taskRunnerConfigurer;
	}

	@PreDestroy
	public void shutdownTaskRunner() {
		if (this.taskRunnerConfigurer != null) {
			this.taskRunnerConfigurer.shutdown(); // Gracefully shuts down polling when the app closes
		}
	}
}
