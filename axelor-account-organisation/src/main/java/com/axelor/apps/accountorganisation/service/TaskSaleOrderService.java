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
package com.axelor.apps.accountorganisation.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.accountorganisation.db.ISaleOrder;
import com.axelor.apps.accountorganisation.exceptions.IExceptionMessage;
import com.axelor.apps.base.db.IProduct;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.UnitRepository;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.organisation.db.IProject;
import com.axelor.apps.organisation.db.ITask;
import com.axelor.apps.organisation.db.PlanningLine;
import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.organisation.db.Task;
import com.axelor.apps.organisation.db.repo.TaskRepository;
import com.axelor.apps.organisation.service.ProjectService;
import com.axelor.apps.organisation.service.TaskService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.SaleOrderSubLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;


public class TaskSaleOrderService {
	
	private static final Logger LOG = LoggerFactory.getLogger(TaskSaleOrderService.class);

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
	
	@Inject
	private SaleOrderRepository saleOrderRepo;
	
	@Inject
	private TaskRepository taskRepo;
	
	@Inject
	private UnitRepository unitRepo;
	
	private LocalDateTime todayTime;
	
	@Inject
	public TaskSaleOrderService() {
		
		todayTime = GeneralService.getTodayDateTime().toLocalDateTime();
		
	}
	
	
	/**
	 * Méthode permettant de générer ou mettre à jour le(s) tache(s) liées au devis.
	 * @param saleOrder
	 * 			Un devis
	 * @throws AxelorException Les unités demandés ne se trouvent pas dans la liste de conversion
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void createTasks(SaleOrder saleOrder) throws AxelorException  {

		// Vérification de l'ensemble des lignes de devis pour le type 'Facturation par tâche'
		this.checkProductType(saleOrder);
			
		// Si il y a au moins une tache a créer
		if(this.hasSaleOrderLineTaskProduct(saleOrder))  {
			
			boolean hasGlobalTask = saleOrder.getHasGlobalTask();
			
			Project project = saleOrder.getProject();
			
			if(project == null)  {
				project = projectService.createProject(
						saleOrder.getClientPartner().getFullName()+" "+saleOrder.getSaleOrderSeq(), 
						IProject.STATUS_DRAFT, 
						saleOrder.getClientPartner(), 
						saleOrder.getCompany(), 
						saleOrder.getContactPartner(), 
						true, 
						true);
				
				saleOrder.setProject(project);
			}
			
			if(hasGlobalTask)  {
				
				projectService.updateDefaultTask(project);
				
				this.assignTaskInSaleOrderLines(saleOrder.getSaleOrderLineList(), project.getDefaultTask());
				
			}
			
			this.createTasks(saleOrder.getSaleOrderLineList());
			
			saleOrderRepo.save(saleOrder);
		}
	}
	
	
	public void assignTaskInSaleOrderLines(List<SaleOrderLine> saleOrderLineList, Task defaultTask)  {
		
		if(saleOrderLineList != null)  {
			for(SaleOrderLine saleOrderLine : saleOrderLineList)  {
				
				saleOrderLine.setTask(defaultTask);	
			
			}
		}
		
	}
	
	
	
	public void createTasks(List<SaleOrderLine> saleOrderLineList) throws AxelorException  {

		if(saleOrderLineList != null)  {
			for(SaleOrderLine saleOrderLine : saleOrderLineList)  {
				
				if(this.isTaskProduct(saleOrderLine)) {
					
					Task task = this.getTask(saleOrderLine);
					
					saleOrderLine.setTask(task);
					
					this.createPlanningLines(saleOrderLine, task);
					
					this.updateTask(saleOrderLine);
					
					taskRepo.save(task);
				}
			}
		}		
	}
	
	
	public Task getTask(SaleOrderLine saleOrderLine) throws AxelorException  {
		
		Task task = saleOrderLine.getTask();
		
		if(task == null)  {
			task = this.createTask(saleOrderLine);
		}
		
		return task;
		
	}
	
	
	
	public boolean isTaskProduct(SaleOrderLine saleOrderLine)  {
		
		if(saleOrderLine.getProduct() != null
				&& saleOrderLine.getProduct().getProductTypeSelect().equals(IProduct.PRODUCT_TYPE_SERVICE) 
				&& saleOrderLine.getSaleSupplySelect() == IProduct.SALE_SUPPLY_PRODUCE)  {
			
			return true;
		}
		
		return false;
	}
	
	
	public Task createTask(SaleOrderLine saleOrderLine) throws AxelorException  {
		Task task = new Task();
		
		SaleOrder saleOrder = saleOrderLine.getSaleOrder();
		
		Project project = saleOrder.getProject();
		
		if(project.getTaskList() == null)  {
			project.setTaskList(new ArrayList<Task>());
		}
		project.addTaskListItem(task);
		
		task.setProject(project);
		task.setSaleOrderLine(saleOrderLine);
		task.setProduct(saleOrderLine.getProduct());
		
		boolean isToInvoice = this.isToInvoice(saleOrderLine.getSaleOrder());
		
		task.setIsToInvoice(isToInvoice);
		
		if(isToInvoice)  {
			
			task.setQty(saleOrderLine.getQty());
			task.setPrice(this.computeDiscount(saleOrderLine));
			task.setInvoicingDate(saleOrderLine.getInvoicingDate());
		}
		
		task.setName(saleOrder.getSaleOrderSeq()+" - "+saleOrderLine.getSequence()+" : "+saleOrderLine.getProductName());
		task.setDescription(saleOrderLine.getDescription());
		task.setStartDateT(todayTime);
		task.setEndDateT(todayTime);
		task.setIsTimesheetAffected(true);
		
		task.setEstimatedAmount(BigDecimal.ZERO);
		task.setTotalTime(BigDecimal.ZERO);
		task.setStatusSelect(ITask.STATUS_DRAFT);
		task.setExportTypeSelect("pdf");
		
		return task;
	}
	
	
	public void updateTask(SaleOrderLine saleOrderLine) throws AxelorException  {
		
		Task task = saleOrderLine.getTask();
		
		boolean isToInvoice = this.isToInvoice(saleOrderLine.getSaleOrder());
		
		task.setIsToInvoice(isToInvoice);
		
		if(isToInvoice)  {
			
			task.setAmountToInvoice(task.getAmountToInvoice().add(saleOrderLine.getCompanyExTaxTotal()));
			
		}
		
		task.setEstimatedAmount(task.getEstimatedAmount().add(saleOrderLine.getCompanyExTaxTotal()));
			
		task.setTotalTime(task.getTotalTime().add(unitConversionService.convert(saleOrderLine.getUnit(), task.getProject().getUnit(), saleOrderLine.getQty())));

		task.setEndDateT(taskService.getTaskEndDate(task));

		task.setPlannedTime(taskService.getPlannedTime(task));
		
		task.setSpentTime(taskService.getSpentTime(task));
		
		taskService.updateFinancialInformation(task);
		
		taskService.updateTaskProgress(task);
	}
	
	
	public BigDecimal computeDiscount(SaleOrderLine saleOrderLine)  {
		
		return priceListService.computeDiscount(saleOrderLine.getPrice(), saleOrderLine.getDiscountTypeSelect(), saleOrderLine.getDiscountAmount());
		
	}
	
	
	public void createPlanningLines(SaleOrderLine saleOrderLine, Task task) throws AxelorException  {
	
		if(saleOrderLine.getSaleOrderSubLineList() != null) {
			if(task.getPlanningLineList() == null)  {
				task.setPlanningLineList(new ArrayList<PlanningLine>());
			}
			
			for(SaleOrderSubLine saleOrderSubLine : saleOrderLine.getSaleOrderSubLineList())  {
				task.getPlanningLineList().add(
						this.createPlanningLine(saleOrderSubLine, task));
			}
		}
	}
	
	
	public PlanningLine createPlanningLine(SaleOrderSubLine saleOrderSubLine, Task task) throws AxelorException  {
		
		PlanningLine planningLine = new PlanningLine();
		planningLine.setTask(task);
		planningLine.setEmployee(saleOrderSubLine.getEmployee());
		planningLine.setProduct(saleOrderSubLine.getProduct());
		planningLine.setFromDateTime(todayTime);
		
		planningLine.setDuration(saleOrderSubLine.getQty());
		planningLine.setUnit(saleOrderSubLine.getUnit());
		planningLine.setToDateTime(
				planningLine.getFromDateTime().plusMinutes(
						unitConversionService.convert(
								saleOrderSubLine.getUnit(), 
								unitRepo.findByCode("MIN"),
								saleOrderSubLine.getQty()).intValue()));
		return planningLine;
		
	}
	
	
	public boolean hasSaleOrderLineTaskProduct(SaleOrder saleOrder) throws AxelorException  {

		if(saleOrder.getSaleOrderLineList() != null && this.isTaskInvoicingMethod(saleOrder))  {
			for(SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList())  {
				
				if(this.hasSaleOrderLineTaskProduct(saleOrderLine))  {
					return true;
				}
				
			}
		}
		return false;
	}
	
	
	public boolean hasSaleOrderLineTaskProduct(SaleOrderLine saleOrderLine) throws AxelorException  {
		
		if(this.isTaskProduct(saleOrderLine))  {
			return true;
		}
		
		return false;
		
	}
	
	
	
	public void checkProductType(SaleOrder saleOrder) throws AxelorException  {

		if(saleOrder.getSaleOrderLineList() != null && this.isTaskInvoicingMethod(saleOrder))  {
			for(SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList())  {
				
				this.checkProductType(saleOrderLine);
				
			}
		}
	}
	
	public void checkProductType(SaleOrderLine saleOrderLine) throws AxelorException  {
		
		if(!this.isTaskProduct(saleOrderLine))  {
			throw new AxelorException(I18n.get(IExceptionMessage.TASK_SALES_ORDER_1), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	
	public boolean isToInvoice(SaleOrder saleOrder)  {
		
		return saleOrder.getInvoicingTypeSelect() == ISaleOrder.INVOICING_TYPE_PER_TASK || saleOrder.getInvoicingTypeSelect() == ISaleOrder.INVOICING_TYPE_FREE ;
		
	}
	
	public boolean isTaskInvoicingMethod(SaleOrder saleOrder)  {
		
		return saleOrder.getInvoicingTypeSelect() == ISaleOrder.INVOICING_TYPE_PER_TASK;
		
	}
}
