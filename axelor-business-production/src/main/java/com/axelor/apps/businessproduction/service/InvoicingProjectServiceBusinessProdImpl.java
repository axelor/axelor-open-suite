package com.axelor.apps.businessproduction.service;

import java.util.List;

import org.joda.time.LocalDateTime;

import com.axelor.apps.businessproject.service.InvoicingProjectService;
import com.axelor.apps.businessproject.db.InvoicingProject;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.service.ProjectTaskService;
import com.axelor.inject.Beans;

public class InvoicingProjectServiceBusinessProdImpl extends InvoicingProjectService{
	
	
	@Override
	public void setLines(InvoicingProject invoicingProject,ProjectTask projectTask, int counter){
		
		if(counter > ProjectTaskService.MAX_LEVEL_OF_PROJECT)  {  return;  }
		counter++;
		
		this.fillLines(invoicingProject, projectTask);
		List<ProjectTask> projectTaskChildrenList = Beans.get(ProjectTaskRepository.class).all().filter("self.project = ?1", projectTask).fetch();

		for (ProjectTask projectTaskChild : projectTaskChildrenList) {
			this.setLines(invoicingProject, projectTaskChild, counter);
		}
		return;
	}
	
	@Override
	public void fillLines(InvoicingProject invoicingProject,ProjectTask projectTask){
		super.fillLines(invoicingProject, projectTask);
		if(projectTask.getProjTaskInvTypeSelect() == ProjectTaskRepository.INVOICING_TYPE_FLAT_RATE || projectTask.getProjTaskInvTypeSelect() == ProjectTaskRepository.INVOICING_TYPE_TIME_BASED)  { 
			LocalDateTime deadlineDateToDateTime = null;
			
			if (invoicingProject.getDeadlineDate() != null){
				deadlineDateToDateTime = LocalDateTime.fromDateFields(invoicingProject.getDeadlineDate().toDate());
			}
			invoicingProject.getManufOrderSet().addAll(Beans.get(ManufOrderRepository.class)
					.all().filter("self.productionOrder.projectTask = ?1 AND (self.realStartDateT < ?2 or ?3 is null)", projectTask, deadlineDateToDateTime, invoicingProject.getDeadlineDate()).fetch());
		}
	}
	
	@Override
	public void clearLines(InvoicingProject invoicingProject){
		
		invoicingProject.clearSaleOrderLineSet();
		invoicingProject.clearPurchaseOrderLineSet();
		invoicingProject.clearLogTimesSet();
		invoicingProject.clearExpenseLineSet();
		invoicingProject.clearElementsToInvoiceSet();
		invoicingProject.clearProjectTaskSet();
		invoicingProject.clearManufOrderSet();
	}
	
}
