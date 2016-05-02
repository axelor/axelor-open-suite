package com.axelor.apps.production.service;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;

import com.axelor.apps.business.project.service.InvoicingProjectService;
import com.axelor.apps.businessproject.db.InvoicingProject;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.service.ProjectTaskService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.inject.Beans;

public class InvoicingProjectServiceBusinessImpl extends InvoicingProjectService{

	
	@Override
	public void setLines(InvoicingProject invoicingProject,ProjectTask projectTask, int counter){
		
		List<ManufOrder> manufOrderList = new ArrayList<ManufOrder>();
		LocalDate deadLine = invoicingProject.getDeadlineDate();
		
		if(counter > ProjectTaskService.MAX_LEVEL_OF_PROJECT)  {  return;  }
			counter++;

			if(projectTask.getProjTaskInvTypeSelect() == ProjectTaskRepository.INVOICING_TYPE_FLAT_RATE || projectTask.getProjTaskInvTypeSelect() == ProjectTaskRepository.INVOICING_TYPE_TIME_BASED)  {

				manufOrderList.addAll(Beans.get(ManufOrderRepository.class)
							.all().filter("self.productionOrder.projectTask = ?1 AND (self.realStartDateT < ?2 or ?2 is null)", projectTask, deadLine).fetch());
				for (ManufOrder manufOrder : manufOrderList)
					invoicingProject.addManufOrderSetItem(manufOrder);
				
				if(projectTask.getProjTaskInvTypeSelect() == ProjectTaskRepository.INVOICING_TYPE_FLAT_RATE && !projectTask.getInvoiced())
					invoicingProject.addProjectTaskSetItem(projectTask);
			}

			List<ProjectTask> projectTaskChildrenList = Beans.get(ProjectTaskRepository.class).all().filter("self.project = ?1", projectTask).fetch();

			for (ProjectTask projectTaskChild : projectTaskChildrenList) {
				this.setLines(invoicingProject, projectTaskChild, counter);
			}
	}
	
}
