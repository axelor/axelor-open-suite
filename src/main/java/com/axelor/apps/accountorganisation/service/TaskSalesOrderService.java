/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.accountorganisation.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.accountorganisation.db.ISalesOrder;
import com.axelor.apps.accountorganisation.exceptions.IExceptionMessage;
import com.axelor.apps.base.db.IProduct;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.organisation.db.IProject;
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
import com.axelor.i18n.I18n;
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

		// Vérification de l'ensemble des lignes de devis pour le type 'Facturation par tâche'
		this.checkProductType(salesOrder);
			
		// Si il y a au moins une tache a créer
		if(this.hasSalesOrderLineTaskProduct(salesOrder))  {
			
			boolean hasGlobalTask = salesOrder.getHasGlobalTask();
			
			Project project = salesOrder.getProject();
			
			if(project == null)  {
				project = projectService.createProject(
						salesOrder.getClientPartner().getFullName()+" "+salesOrder.getSalesOrderSeq(), 
						IProject.STATUS_DRAFT, 
						salesOrder.getClientPartner(), 
						salesOrder.getCompany(), 
						salesOrder.getContactPartner(), 
						true, 
						true);
				
				salesOrder.setProject(project);
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
		
		if(salesOrderLine.getProduct() != null
				&& salesOrderLine.getProduct().getProductTypeSelect().equals(IProduct.PRODUCT_TYPE_SERVICE) 
				&& salesOrderLine.getSaleSupplySelect() == IProduct.SALE_SUPPLY_PRODUCE)  {
			
			return true;
		}
		
		return false;
	}
	
	
	public Task createTask(SalesOrderLine salesOrderLine) throws AxelorException  {
		Task task = new Task();
		
		SalesOrder salesOrder = salesOrderLine.getSalesOrder();
		
		Project project = salesOrder.getProject();
		
		if(project.getTaskList() == null)  {
			project.setTaskList(new ArrayList<Task>());
		}
		project.addTaskListItem(task);
		
		task.setProject(project);
		task.setSalesOrderLine(salesOrderLine);
		task.setProduct(salesOrderLine.getProduct());
		
		boolean isToInvoice = this.isToInvoice(salesOrderLine.getSalesOrder());
		
		task.setIsToInvoice(isToInvoice);
		
		if(isToInvoice)  {
			
			task.setQty(salesOrderLine.getQty());
			task.setPrice(this.computeDiscount(salesOrderLine));
			task.setInvoicingDate(salesOrderLine.getInvoicingDate());
		}
		
		task.setName(salesOrder.getSalesOrderSeq()+" - "+salesOrderLine.getSequence()+" : "+salesOrderLine.getProductName());
		task.setDescription(salesOrderLine.getDescription());
		task.setStartDateT(todayTime);
		task.setEndDateT(todayTime);
		task.setIsTimesheetAffected(true);
		
		task.setEstimatedAmount(BigDecimal.ZERO);
		task.setTotalTime(BigDecimal.ZERO);
		task.setStatusSelect(ITask.STATUS_DRAFT);
		task.setExportTypeSelect("pdf");
		
		return task;
	}
	
	
	public void updateTask(SalesOrderLine salesOrderLine) throws AxelorException  {
		
		Task task = salesOrderLine.getTask();
		
		boolean isToInvoice = this.isToInvoice(salesOrderLine.getSalesOrder());
		
		task.setIsToInvoice(isToInvoice);
		
		if(isToInvoice)  {
			
			task.setAmountToInvoice(task.getAmountToInvoice().add(salesOrderLine.getCompanyExTaxTotal()));
			
		}
		
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
								Unit.findByCode("MIN"),
								salesOrderSubLine.getQty()).intValue()));
		return planningLine;
		
	}
	
	
	public boolean hasSalesOrderLineTaskProduct(SalesOrder salesOrder) throws AxelorException  {

		if(salesOrder.getSalesOrderLineList() != null && this.isTaskInvoicingMethod(salesOrder))  {
			for(SalesOrderLine salesOrderLine : salesOrder.getSalesOrderLineList())  {
				
				if(this.hasSalesOrderLineTaskProduct(salesOrderLine))  {
					return true;
				}
				
			}
		}
		return false;
	}
	
	
	public boolean hasSalesOrderLineTaskProduct(SalesOrderLine salesOrderLine) throws AxelorException  {
		
		if(this.isTaskProduct(salesOrderLine))  {
			return true;
		}
		
		return false;
		
	}
	
	
	
	public void checkProductType(SalesOrder salesOrder) throws AxelorException  {

		if(salesOrder.getSalesOrderLineList() != null && this.isTaskInvoicingMethod(salesOrder))  {
			for(SalesOrderLine salesOrderLine : salesOrder.getSalesOrderLineList())  {
				
				this.checkProductType(salesOrderLine);
				
			}
		}
	}
	
	public void checkProductType(SalesOrderLine salesOrderLine) throws AxelorException  {
		
		if(!this.isTaskProduct(salesOrderLine))  {
			throw new AxelorException(I18n.get(IExceptionMessage.TASK_SALES_ORDER_1), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	
	public boolean isToInvoice(SalesOrder salesOrder)  {
		
		return salesOrder.getInvoicingTypeSelect() == ISalesOrder.INVOICING_TYPE_PER_TASK || salesOrder.getInvoicingTypeSelect() == ISalesOrder.INVOICING_TYPE_FREE ;
		
	}
	
	public boolean isTaskInvoicingMethod(SalesOrder salesOrder)  {
		
		return salesOrder.getInvoicingTypeSelect() == ISalesOrder.INVOICING_TYPE_PER_TASK;
		
	}
}
