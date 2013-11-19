/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.organisation.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;

import javax.persistence.Query;

import org.joda.time.LocalDateTime;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.organisation.db.Candidate;
import com.axelor.apps.organisation.db.Employee;
import com.axelor.apps.organisation.db.IProject;
import com.axelor.apps.organisation.db.ITask;
import com.axelor.apps.organisation.db.ITaskUpdateLine;
import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.organisation.db.Task;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.persist.Transactional;

public class ProjectService {
	
	@Inject
	private Injector injector;

	
	private LocalDateTime todayTime;
	
	@Inject
	public ProjectService() {
		
		todayTime = GeneralService.getTodayDateTime().toLocalDateTime();
		
	}
	
	public void updateDefaultTask(Project project) {

		if(project.getDefaultTask() == null)  {
			
			this.createDefaultTask(project);
			
		}
		
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Task createDefaultTask(Project project) {

		Task defaultTask = new Task();

		defaultTask.setName(project.getName());
		defaultTask.setProject(project);
		defaultTask.setRealEstimatedMethodSelect(project.getRealEstimatedMethodSelect());
		project.setDefaultTask(defaultTask);
		defaultTask.setExportTypeSelect("pdf");
		defaultTask.setStatusSelect(ITask.STATUS_DRAFT);
		defaultTask.setStartDateT(todayTime);
		defaultTask.setAmountToInvoice(BigDecimal.ZERO);
		defaultTask.setEstimatedAmount(BigDecimal.ZERO);
		if(project.getTaskList() == null)  {
			project.setTaskList(new ArrayList<Task>());
		}
		project.addTaskListItem(defaultTask);
		project.setDefaultTask(defaultTask);
		defaultTask.save();
		
		return defaultTask;
		
	}
	

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Task createPreSalesTask(Project project) {

		if(project.getName() != null && !project.getName().isEmpty()) {
			Task findTask = Task.all().filter("self.project = ?1 AND self.name = ?2", project, "Avant vente "+project.getName()).fetchOne();
			if(findTask == null) {
				Task preSalestask = new Task();

				preSalestask.setProject(project);
				preSalestask.setName("Avant vente "+project.getName());
				preSalestask.setRealEstimatedMethodSelect(project.getRealEstimatedMethodSelect());
				project.getTaskList().add(preSalestask);
				preSalestask.save();
				
				return preSalestask;
			}
		}
		
		return null;
	}
	
	
	public void updateFinancialInformation(Project project) throws AxelorException  {
		
		this.updateInitialEstimatedAmount(project);
		this.updateRealEstimatedAmount(project);
		this.updateRealInvoicedAmount(project);
		
	}
	
	
	public void updateInitialEstimatedAmount(Project project) throws AxelorException  {
		
		/**  REVENUE  **/
		
		Query q = JPA.em().createQuery("select SUM(sol.companyExTaxTotal) FROM SalesOrderLine as sol WHERE sol.task.project = ?1 AND sol.salesOrder.statusSelect = 3");
		q.setParameter(1, project);
				
		BigDecimal salesOrderTurnover = (BigDecimal) q.getSingleResult();
		
		BigDecimal financialInformationUpdateTurnover = this.getFinancialInformationUpdateAmount(project, ITaskUpdateLine.TYPE_REVENUE, ITaskUpdateLine.APPLICATION_INITIAL_ESTIMATED);
		
		BigDecimal turnover = BigDecimal.ZERO;
		if(salesOrderTurnover != null)  {
			turnover = turnover.add(salesOrderTurnover);
		}
		if(financialInformationUpdateTurnover != null)  {
			turnover = turnover.add(financialInformationUpdateTurnover);
		}
		
		/**  COST  **/
		
		q = JPA.em().createQuery("select SUM(pol.companyExTaxTotal) FROM PurchaseOrderLine as pol WHERE pol.task.project = ?1 AND pol.purchaseOrder.statusSelect = 3");
		q.setParameter(1, project);
				
		BigDecimal purchaseOrderCost = (BigDecimal) q.getSingleResult();
		
		BigDecimal salesOrderCost = this.getSalesOrderInitialEstimatedCost(project);
		
		BigDecimal financialInformationUpdateCost = this.getFinancialInformationUpdateAmount(project, ITaskUpdateLine.TYPE_COST, ITaskUpdateLine.APPLICATION_INITIAL_ESTIMATED);
		
		BigDecimal cost = BigDecimal.ZERO;
		if(purchaseOrderCost != null)  {
			cost = cost.add(purchaseOrderCost);
		}
		if(salesOrderCost != null)  {
			cost = cost.add(salesOrderCost);
		}
		if(financialInformationUpdateCost != null)  {
			cost = cost.add(financialInformationUpdateCost);
		}
		
		
		/**  MARGIN  **/
		
		BigDecimal margin = BigDecimal.ZERO;
		if(turnover != null)  {
			margin = margin.add(turnover);
		}
		if(cost != null)  {
			margin = margin.subtract(cost);
		}
		
		project.setInitialEstimatedTurnover(turnover);
		project.setInitialEstimatedCost(cost);
		project.setInitialEstimatedMargin(margin);
	}
	
	
	public void updateRealEstimatedAmount(Project project) throws AxelorException  {
		
		/**  REVENUE  **/
		
		BigDecimal progressTurnover = this.getProjectProgress(project);
		
		BigDecimal financialInformationUpdateTurnover = this.getFinancialInformationUpdateAmount(project, ITaskUpdateLine.TYPE_REVENUE, ITaskUpdateLine.APPLICATION_REAL_ESTIMATED);
		
		BigDecimal turnover = BigDecimal.ZERO;
		if(progressTurnover != null)  {
			turnover = turnover.add(progressTurnover);
		}
		if(financialInformationUpdateTurnover != null)  {
			turnover = turnover.add(financialInformationUpdateTurnover);
		}
		
		/**  COST  **/
		
		Query q = JPA.em().createQuery("select SUM(pol.companyExTaxTotal) FROM PurchaseOrderLine as pol WHERE pol.task.project = ?1 AND pol.purchaseOrder.statusSelect = 3 AND pol.product.applicationTypeSelect = 1");
		q.setParameter(1, project);
				
		BigDecimal purchaseOrderCost = (BigDecimal) q.getSingleResult();
		
		
		q = JPA.em().createQuery("select SUM(tl.timesheet.userInfo.employee.dailySalaryCost * tl.duration) FROM TimesheetLine as tl WHERE tl.task.project = ?1 and tl.timesheet.statusSelect = 3");
		q.setParameter(1, project);
				
		BigDecimal timesheetLineCost = (BigDecimal) q.getSingleResult();
		
		q = JPA.em().createQuery("select SUM(el.companyTotal) FROM ExpenseLine as el WHERE el.task.project = ?1 and el.expense.statusSelect = 4");
		q.setParameter(1, project);
				
		BigDecimal expenseLineCost = (BigDecimal) q.getSingleResult();
		
		
		// plannification pas encore échue
		
		BigDecimal planningLineCost = this.getPlanningLinesAmount(project);
		
		BigDecimal financialInformationUpdateCost = this.getFinancialInformationUpdateAmount(project, ITaskUpdateLine.TYPE_COST, ITaskUpdateLine.APPLICATION_REAL_ESTIMATED);
		
		BigDecimal cost = BigDecimal.ZERO;
		if(purchaseOrderCost != null)  {
			cost = cost.add(purchaseOrderCost);
		}
		if(timesheetLineCost != null)  {
			cost = cost.add(timesheetLineCost);
		}
		if(expenseLineCost != null)  {
			cost = cost.add(expenseLineCost);
		}
		if(planningLineCost != null)  {
			cost = cost.add(planningLineCost);
		}
		if(financialInformationUpdateCost != null)  {
			cost = cost.add(financialInformationUpdateCost);
		}
		
		
		/**  MARGIN  **/
		
		BigDecimal margin = BigDecimal.ZERO;
		if(turnover != null)  {
			margin = margin.add(turnover);
		}
		if(cost != null)  {
			margin = margin.subtract(cost);
		}
		
		project.setRealEstimatedTurnover(turnover);
		project.setRealEstimatedCost(cost);
		project.setRealEstimatedMargin(margin);
	}
	
	
	public void updateRealInvoicedAmount(Project project)  {
		
		/**  REVENUE  **/
		
		Query q = JPA.em().createQuery("select SUM(il.companyExTaxTotal) FROM InvoiceLine as il WHERE il.task.project = ?1 AND (il.invoice.status.code = 'val' OR il.invoice.status.code = 'dis') AND (il.invoice.operationTypeSelect = 3 OR il.invoice.operationTypeSelect = 4)");
		q.setParameter(1, project);
				
		BigDecimal invoiceLineTurnover = (BigDecimal) q.getSingleResult();
		
		BigDecimal financialInformationUpdateTurnover = this.getFinancialInformationUpdateAmount(project, ITaskUpdateLine.TYPE_REVENUE, ITaskUpdateLine.APPLICATION_REAL_INVOICED);
		
		BigDecimal turnover = BigDecimal.ZERO;
		if(invoiceLineTurnover != null)  {
			turnover = turnover.add(invoiceLineTurnover);
		}
		if(financialInformationUpdateTurnover != null)  {
			turnover = turnover.add(financialInformationUpdateTurnover);
		}

		
		/**  COST  **/
		
		q = JPA.em().createQuery("select SUM(il.companyExTaxTotal) FROM InvoiceLine as il WHERE il.task.project = ?1 AND (il.invoice.status.code = 'val' OR il.invoice.status.code = 'dis') AND (il.invoice.operationTypeSelect = 1 OR il.invoice.operationTypeSelect = 2)");
		q.setParameter(1, project);
				
		BigDecimal supplierInvoiceLineCost = (BigDecimal) q.getSingleResult();
		
//		q = JPA.em().createQuery("select SUM(il.companyCostPrice * il.qty) FROM InvoiceLine as il WHERE il.task.project = ?1 AND il.invoice.status.code = 'val' AND (il.invoice.operationTypeSelect = 3 OR il.invoice.operationTypeSelect = 4)");
//		q.setParameter(1, project);
//				
//		BigDecimal customerInvoiceLineCost = (BigDecimal) q.getSingleResult();
		
		
		q = JPA.em().createQuery("select SUM(tl.timesheet.userInfo.employee.dailySalaryCost * tl.duration) FROM TimesheetLine as tl WHERE tl.task.project = ?1 and tl.timesheet.statusSelect = 3");
		q.setParameter(1, project);
				
		BigDecimal timesheetLineCost = (BigDecimal) q.getSingleResult();
			
		
		q = JPA.em().createQuery("select SUM(el.companyTotal) FROM ExpenseLine as el WHERE el.task.project = ?1 and el.expense.statusSelect = 4");
		q.setParameter(1, project);
				
		BigDecimal expenseLineCost = (BigDecimal) q.getSingleResult();
		
		BigDecimal financialInformationUpdateCost = this.getFinancialInformationUpdateAmount(project, ITaskUpdateLine.TYPE_COST, ITaskUpdateLine.APPLICATION_REAL_INVOICED);
		
		BigDecimal cost = BigDecimal.ZERO;
		if(supplierInvoiceLineCost != null)  {
			cost = cost.add(supplierInvoiceLineCost);
		}
//		if(customerInvoiceLineCost != null)  {
//			cost = cost.add(customerInvoiceLineCost);
//		}
		if(timesheetLineCost != null)  {
			cost = cost.add(timesheetLineCost);
		}
		if(expenseLineCost != null)  {
			cost = cost.add(expenseLineCost);
		}
		if(financialInformationUpdateCost != null)  {
			cost = cost.add(financialInformationUpdateCost);
		}
		
		
		/**  MARGIN  **/
		
		BigDecimal margin = BigDecimal.ZERO;
		if(turnover != null)  {
			margin = margin.add(turnover);
		}
		if(cost != null)  {
			margin = margin.subtract(cost);
		}
		
		project.setRealInvoicedTurnover(turnover);
		project.setRealInvoicedCost(cost);
		project.setRealInvoicedMargin(margin);
	}
	
	
	public BigDecimal getSalesOrderInitialEstimatedCost(Project project) throws AxelorException  {
		
		BigDecimal salesOrderConfirmedCost = BigDecimal.ZERO;
		
		if(project.getTaskList() != null)  {
			for(Task task : project.getTaskList())   {
				
				BigDecimal salesOrderInitialEstimatedCost = injector.getInstance(TaskService.class).getSalesOrderInitialEstimatedCost(task);
				if(salesOrderInitialEstimatedCost != null)  {
					salesOrderConfirmedCost = salesOrderConfirmedCost.add(salesOrderInitialEstimatedCost);
				}
			}
		}
		
		return salesOrderConfirmedCost;
	}

	
	public BigDecimal getFinancialInformationUpdateAmount(Project project, int typeSelect, int applicationSelect )  {
		
		Query q = JPA.em().
				createQuery("select SUM(fiu.amount) FROM FinancialInformationUpdate as fiu WHERE fiu.task.project = ?1 AND fiu.typeSelect = ?2 AND fiu.applicationSelect = ?3");
		q.setParameter(1, project);
		q.setParameter(2, typeSelect);
		q.setParameter(3, applicationSelect);
				
		return (BigDecimal) q.getSingleResult();
		
	}
	
	
	public BigDecimal getPlanningLinesAmount(Project project) throws AxelorException  {
		
		return injector.getInstance(TaskService.class).getPlanningLinesAmountNotAccounted(project.getTaskList());
		
	}
	
	
	public BigDecimal getProjectProgress(Project project)  {
		
		return injector.getInstance(TaskService.class).getTaskProgressTurnover(project.getTaskList());
		
	}
	
	
	public void updateTaskProgress(Project project)  {
		
		injector.getInstance(TaskService.class).updateTaskProgress(project.getTaskList());
		
	}
	
	public Project createProject(String name, int businessStatusSelect, Partner clientPartner, Company company, Partner contactPartner, boolean isBusiness, boolean isProject)  {
		
		Project project = new Project();
		project.setName(name);
		project.setBusinessStatusSelect(businessStatusSelect);
		project.setCandidateSet(new HashSet<Candidate>());
		project.setClientPartner(clientPartner);
		project.setCompany(company);
		project.setContactPartner(contactPartner);
		project.setEmployeeSet(new HashSet<Employee>());
		project.setExportTypeSelect(IProject.REPORT_TYPE_PDF);
		project.setIsBusiness(isBusiness);
		project.setIsProject(isProject);
		project.setUnit(GeneralService.getUnit());
		
		return project;
	}
	
}
