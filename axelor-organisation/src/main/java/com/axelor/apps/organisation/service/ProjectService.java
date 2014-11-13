/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.organisation.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

import javax.persistence.Query;

import org.joda.time.LocalDateTime;

import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.organisation.db.IProject;
import com.axelor.apps.organisation.db.ITask;
import com.axelor.apps.organisation.db.ITaskUpdateLine;
import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.organisation.db.Task;
import com.axelor.apps.organisation.db.repo.ProjectRepository;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.persist.Transactional;

public class ProjectService extends ProjectRepository{
	
	static final String DEFAULT_EXPORT_TYPE = "pdf";
	
	@Inject
	private Injector injector;

	
	private LocalDateTime todayTime;
	
	@Inject
	private TaskService taskService;
	
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
		defaultTask.setExportTypeSelect(DEFAULT_EXPORT_TYPE);
		defaultTask.setStatusSelect(ITask.STATUS_DRAFT);
		defaultTask.setStartDateT(todayTime);
		defaultTask.setAmountToInvoice(BigDecimal.ZERO);
		defaultTask.setEstimatedAmount(BigDecimal.ZERO);
		if(project.getTaskList() == null)  {
			project.setTaskList(new ArrayList<Task>());
		}
		project.addTaskListItem(defaultTask);
		project.setDefaultTask(defaultTask);
		taskService.save(defaultTask);
		
		return defaultTask;
		
	}

	public void updateFinancialInformation(Project project) throws AxelorException  {
		
		this.updateInitialEstimatedAmount(project);
		this.updateRealEstimatedAmount(project);
		this.updateRealInvoicedAmount(project);
		
	}
	
	
	public void updateInitialEstimatedAmount(Project project) throws AxelorException  {
		
		/**  REVENUE  **/
		
		Query q = JPA.em().createQuery("select SUM(sol.companyExTaxTotal) FROM SaleOrderLine as sol WHERE sol.task.project = ?1 AND sol.saleOrder.statusSelect = 3");
		q.setParameter(1, project);
				
		BigDecimal saleOrderTurnover = (BigDecimal) q.getSingleResult();
		
		BigDecimal financialInformationUpdateTurnover = this.getFinancialInformationUpdateAmount(project, ITaskUpdateLine.TYPE_REVENUE, ITaskUpdateLine.APPLICATION_INITIAL_ESTIMATED);
		
		BigDecimal turnover = BigDecimal.ZERO;
		if(saleOrderTurnover != null)  {
			turnover = turnover.add(saleOrderTurnover);
		}
		if(financialInformationUpdateTurnover != null)  {
			turnover = turnover.add(financialInformationUpdateTurnover);
		}
		
		/**  COST  **/
		
		q = JPA.em().createQuery("select SUM(pol.companyExTaxTotal) FROM PurchaseOrderLine as pol WHERE pol.task.project = ?1 AND pol.purchaseOrder.statusSelect = 3");
		q.setParameter(1, project);
				
		BigDecimal purchaseOrderCost = (BigDecimal) q.getSingleResult();
		
		BigDecimal saleOrderCost = this.getSaleOrderInitialEstimatedCost(project);
		
		BigDecimal financialInformationUpdateCost = this.getFinancialInformationUpdateAmount(project, ITaskUpdateLine.TYPE_COST, ITaskUpdateLine.APPLICATION_INITIAL_ESTIMATED);
		
		BigDecimal cost = BigDecimal.ZERO;
		if(purchaseOrderCost != null)  {
			cost = cost.add(purchaseOrderCost);
		}
		if(saleOrderCost != null)  {
			cost = cost.add(saleOrderCost);
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
		
		
		q = JPA.em().createQuery("select SUM(tl.timesheet.user.employee.dailySalaryCost * tl.duration) FROM TimesheetLine as tl WHERE tl.task.project = ?1 and tl.timesheet.statusSelect = 3");
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
		
		Query q = JPA.em().createQuery("select SUM(il.companyExTaxTotal) FROM InvoiceLine as il WHERE il.task.project = ?1 " +
				"AND (il.invoice.statusSelect = ?2 OR il.invoice.statusSelect = ?3) AND (il.invoice.operationTypeSelect = ?4 OR il.invoice.operationTypeSelect = ?5)");
		q.setParameter(1, project);
		q.setParameter(2, InvoiceRepository.STATUS_VALIDATED);
		q.setParameter(3, InvoiceRepository.STATUS_VENTILATED);
		q.setParameter(4, InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND);
		q.setParameter(5, InvoiceRepository.OPERATION_TYPE_CLIENT_SALE);
				
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
		
		q = JPA.em().createQuery("select SUM(il.companyExTaxTotal) FROM InvoiceLine as il WHERE il.task.project = ?1 " +
				"AND (il.invoice.statusSelect = ?2 OR il.invoice.statusSelect = ?3) AND (il.invoice.operationTypeSelect = ?4 OR il.invoice.operationTypeSelect = ?5)");
		q.setParameter(1, project);
		q.setParameter(2, InvoiceRepository.STATUS_VALIDATED);
		q.setParameter(3, InvoiceRepository.STATUS_VENTILATED);
		q.setParameter(4, InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE);
		q.setParameter(5, InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND);
				
		BigDecimal supplierInvoiceLineCost = (BigDecimal) q.getSingleResult();
		
//		q = JPA.em().createQuery("select SUM(il.companyCostPrice * il.qty) FROM InvoiceLine as il WHERE il.task.project = ?1 AND il.invoice.status.code = 'val' AND (il.invoice.operationTypeSelect = 3 OR il.invoice.operationTypeSelect = 4)");
//		q.setParameter(1, project);
//				
//		BigDecimal customerInvoiceLineCost = (BigDecimal) q.getSingleResult();
		
		
		q = JPA.em().createQuery("select SUM(tl.timesheet.user.employee.dailySalaryCost * tl.duration) FROM TimesheetLine as tl WHERE tl.task.project = ?1 and tl.timesheet.statusSelect = 3");
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
	
	
	public BigDecimal getSaleOrderInitialEstimatedCost(Project project) throws AxelorException  {
		
		BigDecimal saleOrderConfirmedCost = BigDecimal.ZERO;
		
		if(project.getTaskList() != null)  {
			for(Task task : project.getTaskList())   {
				
				BigDecimal saleOrderInitialEstimatedCost = injector.getInstance(TaskService.class).getSaleOrderInitialEstimatedCost(task);
				if(saleOrderInitialEstimatedCost != null)  {
					saleOrderConfirmedCost = saleOrderConfirmedCost.add(saleOrderInitialEstimatedCost);
				}
			}
		}
		
		return saleOrderConfirmedCost;
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
		project.setClientPartner(clientPartner);
		project.setCompany(company);
		project.setContactPartner(contactPartner);
		project.setExportTypeSelect(IProject.REPORT_TYPE_PDF);
		project.setIsBusiness(isBusiness);
		project.setIsProject(isProject);
		project.setUnit(GeneralService.getUnit());
		
		return project;
	}
	
	public Project importProject(Object bean, Map values) {
        try{
        	Project project = (Project) bean;
			createDefaultTask(project);
			updateFinancialInformation(project);
			updateTaskProgress(project);
			return project;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
	}
	
}
