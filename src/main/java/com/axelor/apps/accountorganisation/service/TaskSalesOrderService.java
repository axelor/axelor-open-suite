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
package com.axelor.apps.accountorganisation.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.accountorganisation.exceptions.IExceptionMessage;
import com.axelor.apps.base.db.IProduct;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.organisation.db.IProject;
import com.axelor.apps.organisation.db.ISalesOrder;
import com.axelor.apps.organisation.db.ITask;
import com.axelor.apps.organisation.db.PlanningLine;
import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.organisation.db.Task;
import com.axelor.apps.organisation.service.ProjectService;
import com.axelor.apps.organisation.service.TaskService;
import com.axelor.apps.supplychain.db.SalesOrder;
import com.axelor.apps.supplychain.db.SalesOrderLine;
import com.axelor.apps.supplychain.db.SalesOrderSubLine;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.meta.service.MetaTranslations;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;


public class TaskSalesOrderService {
	
	private static final Logger LOG = LoggerFactory.getLogger(TaskSalesOrderService.class);

	@Inject
	private UnitConversionService unitConversionService;
	
	@Inject
	private CurrencyService currencyService;
	
	@Inject
	private PriceListService priceListService;
	
	@Inject
	private TaskService taskService;
	
	@Inject
	private ProjectService projectService;
	
//	@Inject
//	private MetaTranslations metaTranslations;
	
	
	
	private LocalDateTime todayTime;
	
	@Inject
	public TaskSalesOrderService() {
		
		todayTime = GeneralService.getTodayDateTime().toLocalDateTime();
		
	}
	
	
	/**
	 * Méthode permettant de générer ou mettre à jour le(s) tache(s) liées au devis.
	 * @param salesOrder
	 * 			Un devis
	 * @throws AxelorException Les unités demandés ne se trouvent pas dans la liste de conversion
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void createTasks(SalesOrder salesOrder) throws AxelorException  {

		if(salesOrder.getInvoicingTypeSelect() == ISalesOrder.INVOICING_TYPE_PER_TASK  ||  
				salesOrder.getInvoicingTypeSelect() == ISalesOrder.INVOICING_TYPE_FREE)  {
			
			this.checkProductType(salesOrder);
			
			boolean hasGlobalTask = salesOrder.getHasGlobalTask();
			
			Project project = salesOrder.getAffairProject();
			
			if(project == null)  {
				project = projectService.createProject(
						salesOrder.getClientPartner().getFullName()+" "+salesOrder.getSalesOrderSeq(), 
						IProject.STATUS_DRAFT, 
						salesOrder.getClientPartner(), 
						salesOrder.getCompany(), 
						salesOrder.getContactPartner(), 
						true, 
						true);
				
				salesOrder.setAffairProject(project);
			}
			
			if(hasGlobalTask)  {
				
				projectService.updateDefaultTask(project);
				
				this.assignTaskInSalesOrderLines(salesOrder.getSalesOrderLineList(), project.getDefaultTask());
				
			}
			
			this.createTasks(salesOrder.getSalesOrderLineList());
			
			salesOrder.save();
		}
	}
	
	
	public void assignTaskInSalesOrderLines(List<SalesOrderLine> salesOrderLineList, Task defaultTask)  {
		
		if(salesOrderLineList != null)  {
			for(SalesOrderLine salesOrderLine : salesOrderLineList)  {
				
				salesOrderLine.setTask(defaultTask);	
			
			}
		}
		
	}
	
	
	
	public void createTasks(List<SalesOrderLine> salesOrderLineList) throws AxelorException  {

		if(salesOrderLineList != null)  {
			for(SalesOrderLine salesOrderLine : salesOrderLineList)  {
				
				if(this.isTaskProduct(salesOrderLine)) {
					
					Task task = this.getTask(salesOrderLine);
					
					salesOrderLine.setTask(task);
					
					this.createPlanningLines(salesOrderLine, task);
					
					this.updateTask(salesOrderLine);
					
					task.save();
				}
			}
		}		
	}
	
	
	public Task getTask(SalesOrderLine salesOrderLine) throws AxelorException  {
		
		Task task = salesOrderLine.getTask();
		
		if(task == null)  {
			task = this.createTask(salesOrderLine);
		}
		
		return task;
		
	}
	
	
	
	public boolean isTaskProduct(SalesOrderLine salesOrderLine)  {
		
		if(salesOrderLine != null 
				&& salesOrderLine.getProduct() != null
				&& salesOrderLine.getProduct().getProductTypeSelect().equals(IProduct.PRODUCT_TYPE_SERVICE) 
				&& salesOrderLine.getSaleSupplySelect() == IProduct.SALE_SUPPLY_PRODUCE)  {
			
			return true;
		}
		
		return false;
	}
	
	
	public Task createTask(SalesOrderLine salesOrderLine) throws AxelorException  {
		Task task = new Task();
		
		SalesOrder salesOrder = salesOrderLine.getSalesOrder();
		
		Project project = salesOrder.getAffairProject();
		
		if(project.getTaskList() == null)  {
			project.setTaskList(new ArrayList<Task>());
		}
		project.addTaskListItem(task);
		
		task.setProject(project);
		task.setSalesOrderLine(salesOrderLine);
		task.setProduct(salesOrderLine.getProduct());
		task.setQty(salesOrderLine.getQty());
		task.setPrice(this.computeDiscount(salesOrderLine));
		task.setName(salesOrder.getSalesOrderSeq()+" - "+salesOrderLine.getSequence()+" : "+salesOrderLine.getProductName());
		task.setDescription(salesOrderLine.getDescription());
		task.setStartDateT(todayTime);
		task.setEndDateT(todayTime);
		task.setIsTimesheetAffected(true);
		task.setIsToInvoice(false);
		task.setInvoicingDate(salesOrderLine.getInvoicingDate());
		task.setEstimatedAmount(BigDecimal.ZERO);
		task.setTotalTime(BigDecimal.ZERO);
		task.setStatusSelect(ITask.STATUS_DRAFT);
		task.setExportTypeSelect("pdf");
		
		return task;
	}
	
	
	public void updateTask(SalesOrderLine salesOrderLine) throws AxelorException  {
		
		Task task = salesOrderLine.getTask();
		
		task.setIsToInvoice(true);
		
		task.setAmountToInvoice(task.getAmountToInvoice().add(salesOrderLine.getCompanyExTaxTotal()));
		
		task.setEstimatedAmount(task.getEstimatedAmount().add(salesOrderLine.getCompanyExTaxTotal()));
			
		task.setTotalTime(task.getTotalTime().add(unitConversionService.convert(salesOrderLine.getUnit(), task.getProject().getUnit(), salesOrderLine.getQty())));

		task.setEndDateT(taskService.getTaskEndDate(task));

		task.setPlannedTime(taskService.getPlannedTime(task));
		
		task.setSpentTime(taskService.getSpentTime(task));
		
		taskService.updateFinancialInformation(task);
		
		taskService.updateTaskProgress(task);
	}
	
	
	public BigDecimal computeDiscount(SalesOrderLine salesOrderLine)  {
		
		return priceListService.computeDiscount(salesOrderLine.getPrice(), salesOrderLine.getDiscountTypeSelect(), salesOrderLine.getDiscountAmount());
		
	}
	
	
	public void createPlanningLines(SalesOrderLine salesOrderLine, Task task) throws AxelorException  {
	
		if(salesOrderLine.getSalesOrderSubLineList() != null) {
			if(task.getPlanningLineList() == null)  {
				task.setPlanningLineList(new ArrayList<PlanningLine>());
			}
			
			for(SalesOrderSubLine salesOrderSubLine : salesOrderLine.getSalesOrderSubLineList())  {
				task.getPlanningLineList().add(
						this.createPlanningLine(salesOrderSubLine, task));
			}
		}
	}
	
	
	public PlanningLine createPlanningLine(SalesOrderSubLine salesOrderSubLine, Task task) throws AxelorException  {
		
		PlanningLine planningLine = new PlanningLine();
		planningLine.setTask(task);
		planningLine.setEmployee(salesOrderSubLine.getEmployee());
		planningLine.setProduct(salesOrderSubLine.getProduct());
		planningLine.setFromDateTime(todayTime);
		
		planningLine.setDuration(salesOrderSubLine.getQty());
		planningLine.setUnit(salesOrderSubLine.getUnit());
		planningLine.setToDateTime(
				planningLine.getFromDateTime().plusMinutes(
						unitConversionService.convert(
								salesOrderSubLine.getUnit(), 
								Unit.all().filter("self.code = 'MIN'").fetchOne(), 
								salesOrderSubLine.getQty()).intValue()));
		return planningLine;
		
	}
	
	
	public void checkProductType(SalesOrder salesOrder) throws AxelorException  {

		if(salesOrder.getSalesOrderLineList() != null && salesOrder.getInvoicingTypeSelect() == ISalesOrder.INVOICING_TYPE_PER_TASK)  {
			for(SalesOrderLine salesOrderLine : salesOrder.getSalesOrderLineList())  {
				
				this.checkProductType(salesOrderLine);
				
			}
		}
		
		
	}
	
	public void checkProductType(SalesOrderLine salesOrderLine) throws AxelorException  {
		
		if(!this.isTaskProduct(salesOrderLine))  {
			throw new AxelorException(new MetaTranslations().get(IExceptionMessage.TASK_SALES_ORDER_1), IException.CONFIGURATION_ERROR);
		}
		
	}
}
