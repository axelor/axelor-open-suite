package com.axelor.csv.script;

import java.util.Map;

import javax.inject.Inject;

import com.axelor.wkf.db.Workflow;
import com.axelor.wkf.workflow.WorkflowImporter;
import com.google.common.base.Strings;

public class ImportWorklow {
	
	@Inject
	WorkflowImporter workflowImporter;

	public Object generateMonthlyPayment(Object bean, Map<?, ?> values) {

		if ( bean == null || Strings.isNullOrEmpty( ( (Workflow) bean ).getBpmn() ) ) { return null; }
		workflowImporter.run( ( (Workflow) bean ).getBpmn() );
		
		return null;
	}
}
