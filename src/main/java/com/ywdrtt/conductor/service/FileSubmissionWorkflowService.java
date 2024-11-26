package com.ywdrtt.conductor.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.netflix.conductor.client.http.MetadataClient;
import com.netflix.conductor.client.http.WorkflowClient;
import com.netflix.conductor.common.metadata.workflow.StartWorkflowRequest;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.ywdrtt.conductor.config.workflow.FileSubmissionWorkFlow;

@Service
public class FileSubmissionWorkflowService {

	private final MetadataClient metadataClient;
	private final WorkflowClient workflowClient;
//	private final WorkflowDef fileSubmissionWorkflowDef;
	
	@Autowired
	FileSubmissionWorkFlow fileSubmissionWorkFlow;

	@Autowired
	public FileSubmissionWorkflowService(WorkflowClient workflowClient, 
//			WorkflowDef fileSubmissionWorkflowDef,
			MetadataClient metadataClient) {
		this.workflowClient = workflowClient;
//		this.fileSubmissionWorkflowDef = fileSubmissionWorkflowDef;
		this.metadataClient = metadataClient;
	}

	public void registerAndStartWorkflow(String workflowName, int version, Map<String, Object> inputData) {

		// Set workflow output if needed
//		workflowDef.setOutputParameters(Map.of("finalWrapper", "${aggregateResults_ref.output.wrapper}"));

		// Register the workflow definition with Conductor if not already registered
		WorkflowDef existingDef = getWorkflowDefinition(workflowName, version);
		if (existingDef == null) {
			metadataClient.registerWorkflowDef(fileSubmissionWorkFlow.fileSubmissionWorkflowDef(workflowName, version, inputData));
			System.out.println("Workflow registered: " + workflowName);
		} else {
			System.out.println("Workflow already registered: " + existingDef.getName());
		}

		// Start the workflow with input data
		startWorkflow(workflowName, version, inputData);
	}

	private void startWorkflow(String workFlowName, int version, Map<String, Object> inputData) {

		// Start the workflow
		StartWorkflowRequest startWorkflowRequest = new StartWorkflowRequest();
		startWorkflowRequest.setName(workFlowName);
		startWorkflowRequest.setVersion(version);
		startWorkflowRequest.setInput(inputData);

		// Start the workflow
		String workflowId = workflowClient.startWorkflow(startWorkflowRequest);
		System.out.println("Workflow started with ID: " + workflowId);
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

}
