package com.axelor.apps.production.service;

import java.util.List;

import com.axelor.apps.business.project.service.InvoicingProjectService;
import com.axelor.apps.businessproject.db.InvoicingProject;
import com.axelor.apps.businessproject.db.repo.ElementsToInvoiceRepository;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.service.ProjectTaskService;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.inject.Beans;

public class InvoicingProjectServiceBusinessProdImpl extends InvoicingProjectService{

	
	@Override
	public void setLines(InvoicingProject invoicingProject,ProjectTask projectTask, int counter){
		
		if(counter > ProjectTaskService.MAX_LEVEL_OF_PROJECT)  {  return;  }
		counter++;
		
		if(projectTask.getProjTaskInvTypeSelect() == ProjectTaskRepository.INVOICING_TYPE_FLAT_RATE || projectTask.getProjTaskInvTypeSelect() == ProjectTaskRepository.INVOICING_TYPE_TIME_BASED)  {

			invoicingProject.getSaleOrderLineSet().addAll(Beans.get(SaleOrderLineRepository.class)
					.all().filter("self.saleOrder.project = ?1 AND self.toInvoice = true AND self.invoiced = false AND (self.saleOrder.creationDate < ?2 or ?2 is null)", projectTask, invoicingProject.getDeadlineDate()).fetch());
			
			invoicingProject.getPurchaseOrderLineSet().addAll(Beans.get(PurchaseOrderLineRepository.class)
					.all().filter("self.projectTask = ?1 AND self.toInvoice = true AND self.invoiced = false AND (self.purchaseOrder.orderDate < ?2 or ?2 is null)", projectTask, invoicingProject.getDeadlineDate()).fetch());
			
			invoicingProject.getLogTimesSet().addAll(Beans.get(TimesheetLineRepository.class)
					.all().filter("self.affectedToTimeSheet.statusSelect = 3 AND self.projectTask = ?1 AND self.toInvoice = true AND (self.invoiced = false AND self.date < ?2 or ?2 is null)", projectTask, invoicingProject.getDeadlineDate()).fetch());
			
			invoicingProject.getExpenseLineSet().addAll(Beans.get(ExpenseLineRepository.class)
					.all().filter("self.projectTask = ?1 AND self.toInvoice = true AND self.invoiced = false AND (self.expenseDate < ?2 or ?2 is null)", projectTask, invoicingProject.getDeadlineDate()).fetch());
			
			invoicingProject.getElementsToInvoiceSet().addAll(Beans.get(ElementsToInvoiceRepository.class)
					.all().filter("self.project = ?1 AND self.toInvoice = true AND self.invoiced = false AND (self.date < ?2 or ?2 is null)", projectTask, invoicingProject.getDeadlineDate()).fetch());
			
			invoicingProject.getManufOrderSet().addAll(Beans.get(ManufOrderRepository.class)
					.all().filter("self.productionOrder.projectTask = ?1 AND (self.realStartDateT < ?2 or ?2 is null)", projectTask,  invoicingProject.getDeadlineDate()).fetch());
			
			if(projectTask.getProjTaskInvTypeSelect() == ProjectTaskRepository.INVOICING_TYPE_FLAT_RATE && !projectTask.getInvoiced())
				invoicingProject.addProjectTaskSetItem(projectTask);
		}

		List<ProjectTask> projectTaskChildrenList = Beans.get(ProjectTaskRepository.class).all().filter("self.project = ?1", projectTask).fetch();

		for (ProjectTask projectTaskChild : projectTaskChildrenList) {
			this.setLines(invoicingProject, projectTaskChild, counter);
		}

		return;
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
