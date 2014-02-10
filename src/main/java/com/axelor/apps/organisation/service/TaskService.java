/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
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
 * Software distributed under the License is distributed on an "AS IS"
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
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.organisation.service;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Query;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.SpentTime;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.organisation.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.organisation.db.Employee;
import com.axelor.apps.organisation.db.ITask;
import com.axelor.apps.organisation.db.ITaskUpdateLine;
import com.axelor.apps.organisation.db.PlanningLine;
import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.organisation.db.Task;
import com.axelor.apps.tool.date.DurationTool;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.meta.service.MetaTranslations;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;


public class TaskService {

	@Inject
	private UnitConversionService unitConversionService;
	
	@Inject
	private CurrencyService currencyService;
	
	@Inject
	private PriceListService priceListService;
	
	@Inject
	private MetaTranslations metaTranslations;
	
	private LocalDateTime todayTime;
	
	@Inject
	public TaskService() {
		
		todayTime = GeneralService.getTodayDateTime().toLocalDateTime();
		
	}
	
	public void updateFinancialInformation(Task task) throws AxelorException  {
		
		// Les montants sont figés dès le commencement de la tache
		if(task.getStatusSelect() < ITask.STATUS_STARTED && task.getRealEstimatedMethodSelect() != ITask.REAL_ESTIMATED_METHOD_NONE)  {
			this.updateInitialEstimatedAmount(task);
		}
		this.updateRealEstimedAmount(task);
		this.updateRealInvoicedAmount(task);
		
	}
	
	
	public void checkTaskProject(Task task) throws AxelorException  {
		if(task.getProject() == null)  {
			throw new AxelorException(metaTranslations.get(IExceptionMessage.TASK_1), IException.CONFIGURATION_ERROR);
		}
	}
	
	public void checkProjectUnit(Project project) throws AxelorException  {
		if(project.getUnit() == null)  {
			throw new AxelorException(IExceptionMessage.TASK_2, IException.CONFIGURATION_ERROR);
		}
	}
	
	
	public void checkPlanningLineUnit(PlanningLine planningLine) throws AxelorException  {
		if(planningLine.getUnit() == null)  {
			throw new AxelorException(IExceptionMessage.TASK_3, IException.CONFIGURATION_ERROR);
		}
	}
	
	public void checkSpentTimeUnit(SpentTime spentTime) throws AxelorException  {
		if(spentTime.getUnit() == null)  {
			throw new AxelorException(IExceptionMessage.TASK_4, IException.CONFIGURATION_ERROR);
		}
	}
	
	
	public BigDecimal getSpentTime(Task task) throws AxelorException  {
		
		if(task.getSpentTimeList() != null && !task.getSpentTimeList().isEmpty())  {
			
			this.checkTaskProject(task);
			this.checkProjectUnit(task.getProject());
			
			return this.getSpentTime(task.getSpentTimeList(), task.getProject().getUnit());
		}
		else  {
			return BigDecimal.ZERO;
		}
	}
	
	
	public BigDecimal getPlannedTime(Task task) throws AxelorException  {
		
		BigDecimal plannedTime = BigDecimal.ZERO;
		
		if(task.getPlanningLineList() != null)  {
			
			this.checkTaskProject(task);
			this.checkProjectUnit(task.getProject());
			
			return this.getPlannedTime(task.getPlanningLineList(), task.getProject().getUnit());
		}
		
		return plannedTime;
	}
	
	
	public BigDecimal getPlannedTime(List<PlanningLine> planningLineList, Unit unit) throws AxelorException  {
		
		BigDecimal plannedTime = BigDecimal.ZERO;
		
		for(PlanningLine planningLine : planningLineList)  {
			
			this.checkPlanningLineUnit(planningLine);
			plannedTime = plannedTime.add(unitConversionService.convert(planningLine.getUnit(), unit, planningLine.getDuration()));
		}
		
		return plannedTime;
	}
	
	
	public BigDecimal getSpentTime(List<SpentTime> spentTimeList, Unit unit) throws AxelorException  {
		
		BigDecimal spentTimesDuration = BigDecimal.ZERO;
		
		for(SpentTime spentTime : spentTimeList)  {
			
			this.checkSpentTimeUnit(spentTime);
			spentTimesDuration = spentTimesDuration.add(unitConversionService.convert(spentTime.getUnit(), unit, spentTime.getDuration()));
		}
		
		return spentTimesDuration;
	}
	
	public void updateInitialEstimatedAmount(Task task) throws AxelorException  {
		
		/**  REVENUE  **/
		
		Query q = JPA.em().createQuery("select SUM(sol.companyExTaxTotal) FROM SalesOrderLine as sol WHERE sol.task = ?1 AND sol.salesOrder.statusSelect = 3");
		q.setParameter(1, task);
				
		BigDecimal salesOrderTurnover = (BigDecimal) q.getSingleResult();
		
		BigDecimal financialInformationUpdateTurnover = this.getFinancialInformationUpdateAmount(task, ITaskUpdateLine.TYPE_REVENUE, ITaskUpdateLine.APPLICATION_INITIAL_ESTIMATED);
		
		BigDecimal turnover = BigDecimal.ZERO;
		if(salesOrderTurnover != null)  {
			turnover = turnover.add(salesOrderTurnover);
		}
		if(financialInformationUpdateTurnover != null)  {
			turnover = turnover.add(financialInformationUpdateTurnover);
		}
		
		/**  COST  **/
		
		q = JPA.em().createQuery("select SUM(pol.companyExTaxTotal) FROM PurchaseOrderLine as pol WHERE pol.task = ?1 AND pol.purchaseOrder.statusSelect = 3");
		q.setParameter(1, task);
				
		BigDecimal purchaseOrderCost = (BigDecimal) q.getSingleResult();
		
		BigDecimal salesOrderCost = this.getSalesOrderInitialEstimatedCost(task);
		
		BigDecimal financialInformationUpdateCost = this.getFinancialInformationUpdateAmount(task, ITaskUpdateLine.TYPE_COST, ITaskUpdateLine.APPLICATION_INITIAL_ESTIMATED);
		
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
		
		task.setInitialEstimatedTurnover(turnover);
		task.setInitialEstimatedCost(cost);
		task.setInitialEstimatedMargin(margin);
	}
	
	
	public void updateRealEstimedAmount(Task task) throws AxelorException  {
		
		/**  REVENUE  **/
		
		BigDecimal progressTurnover = this.getTaskProgressTurnover(task);
		
		BigDecimal financialInformationUpdateTurnover = this.getFinancialInformationUpdateAmount(task, ITaskUpdateLine.TYPE_REVENUE, ITaskUpdateLine.APPLICATION_REAL_ESTIMATED);
		
		BigDecimal turnover = BigDecimal.ZERO;
		if(progressTurnover != null)  {
			turnover = turnover.add(progressTurnover);
		}
		if(financialInformationUpdateTurnover != null)  {
			turnover = turnover.add(financialInformationUpdateTurnover);
		}
		
		/**  COST  **/
		
		Query q = JPA.em().createQuery("select SUM(pol.companyExTaxTotal) FROM PurchaseOrderLine as pol WHERE pol.task = ?1 AND pol.purchaseOrder.statusSelect = 3 AND pol.product.applicationTypeSelect = 1");
		q.setParameter(1, task);
				
		BigDecimal purchaseOrderCost = (BigDecimal) q.getSingleResult();
		
		
		q = JPA.em().createQuery("select SUM(tl.timesheet.userInfo.employee.dailySalaryCost * tl.duration) FROM TimesheetLine as tl WHERE tl.task = ?1 and tl.timesheet.statusSelect = 3");
		q.setParameter(1, task);
				
		BigDecimal timesheetLineCost = (BigDecimal) q.getSingleResult();
		
		q = JPA.em().createQuery("select SUM(el.companyTotal) FROM ExpenseLine as el WHERE el.task = ?1 and el.expense.statusSelect = 4");
		q.setParameter(1, task);
				
		BigDecimal expenseLineCost = (BigDecimal) q.getSingleResult();
		
		
		// plannification pas encore échue
		
		q = JPA.em().createQuery("select MAX(tl.date) FROM TimesheetLine as tl WHERE tl.task = ?1 and tl.timesheet.statusSelect = 3");
		q.setParameter(1, task);
				
		LocalDate timesheetLineMaxDate = (LocalDate) q.getSingleResult();
		
		BigDecimal planningLineCost = this.getPlanningLinesAmount(task, timesheetLineMaxDate);
		
		
		BigDecimal financialInformationUpdateCost = this.getFinancialInformationUpdateAmount(task, ITaskUpdateLine.TYPE_COST, ITaskUpdateLine.APPLICATION_REAL_ESTIMATED);
		
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
		
		task.setRealEstimatedTurnover(turnover);
		task.setRealEstimatedCost(cost);
		task.setRealEstimatedMargin(margin);
	}
	
	
	public void updateRealInvoicedAmount(Task task)  {
		
		/**  REVENUE  **/
		
		Query q = JPA.em().createQuery("select SUM(il.companyExTaxTotal) FROM InvoiceLine as il WHERE il.task = ?1 AND (il.invoice.status.code = 'val' OR il.invoice.status.code = 'dis') AND (il.invoice.operationTypeSelect = 3 OR il.invoice.operationTypeSelect = 4)");
		q.setParameter(1, task);
				
		BigDecimal invoiceLineTurnover = (BigDecimal) q.getSingleResult();
		
		BigDecimal financialInformationUpdateTurnover = this.getFinancialInformationUpdateAmount(task, ITaskUpdateLine.TYPE_REVENUE, ITaskUpdateLine.APPLICATION_REAL_INVOICED);
		
		BigDecimal turnover = BigDecimal.ZERO;
		if(invoiceLineTurnover != null)  {
			turnover = turnover.add(invoiceLineTurnover);
		}
		if(financialInformationUpdateTurnover != null)  {
			turnover = turnover.add(financialInformationUpdateTurnover);
		}

		
		/**  COST  **/
		
		q = JPA.em().createQuery("select SUM(il.companyExTaxTotal) FROM InvoiceLine as il WHERE il.task = ?1 AND (il.invoice.status.code = 'val' OR il.invoice.status.code = 'dis') AND (il.invoice.operationTypeSelect = 1 OR il.invoice.operationTypeSelect = 2)");
		q.setParameter(1, task);
				
		BigDecimal supplierInvoiceLineCost = (BigDecimal) q.getSingleResult();
		
//		q = JPA.em().createQuery("select SUM(il.companyCostPrice * il.qty) FROM InvoiceLine as il WHERE il.task = ?1 AND il.invoice.status.code = 'val' AND (il.invoice.operationTypeSelect = 3 OR il.invoice.operationTypeSelect = 4)");
//		q.setParameter(1, task);
//				
//		BigDecimal customerInvoiceLineCost = (BigDecimal) q.getSingleResult();
		
		
		q = JPA.em().createQuery("select SUM(tl.timesheet.userInfo.employee.dailySalaryCost * tl.duration) FROM TimesheetLine as tl WHERE tl.task = ?1 and tl.timesheet.statusSelect = 3");
		q.setParameter(1, task);
				
		BigDecimal timesheetLineCost = (BigDecimal) q.getSingleResult();
			
		
		q = JPA.em().createQuery("select SUM(el.companyTotal) FROM ExpenseLine as el WHERE el.task = ?1 and el.expense.statusSelect = 4");
		q.setParameter(1, task);
				
		BigDecimal expenseLineCost = (BigDecimal) q.getSingleResult();
		
		BigDecimal financialInformationUpdateCost = this.getFinancialInformationUpdateAmount(task, ITaskUpdateLine.TYPE_COST, ITaskUpdateLine.APPLICATION_REAL_INVOICED);
		
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
		
		task.setRealInvoicedTurnover(turnover);
		task.setRealInvoicedCost(cost);
		task.setRealInvoicedMargin(margin);
	}


	public BigDecimal getSalesOrderInitialEstimatedCost(Task task) throws AxelorException  {
		BigDecimal salesOrderEstimatedCost = BigDecimal.ZERO;
		
		if(task.getPlanningLineList() != null && !task.getPlanningLineList().isEmpty())  {
			salesOrderEstimatedCost = this.getPlanningLinesAmount(task, null);
			
			Query q = JPA.em().createQuery("select SUM(sol.companyCostPrice * sol.qty) FROM SalesOrderLine as sol WHERE sol.task = ?1 AND sol.salesOrder.statusSelect = 3 AND sol.product.applicationTypeSelect = 1");
			q.setParameter(1, task);
					
			salesOrderEstimatedCost = (BigDecimal) q.getSingleResult();
		}
		else  {
			Query q = JPA.em().createQuery("select SUM(sol.companyCostPrice * sol.qty) FROM SalesOrderLine as sol WHERE sol.task = ?1 AND sol.salesOrder.statusSelect = 3");
			q.setParameter(1, task);
					
			salesOrderEstimatedCost = (BigDecimal) q.getSingleResult();
		}
		
		return salesOrderEstimatedCost;
	}
	
	
	public BigDecimal getFinancialInformationUpdateAmount(Task task, int typeSelect, int applicationSelect )  {
		
		Query q = JPA.em().
				createQuery("select SUM(fiu.amount) FROM FinancialInformationUpdate as fiu WHERE fiu.task = ?1 AND fiu.typeSelect = ?2 AND fiu.applicationSelect = ?3");
		q.setParameter(1, task);
		q.setParameter(2, typeSelect);
		q.setParameter(3, applicationSelect);
				
		return (BigDecimal) q.getSingleResult();
		
	}
	
	
	public BigDecimal getPlanningLinesAmountNotAccounted(List<Task> taskList) throws AxelorException  {
		
		BigDecimal planningLineAmount = BigDecimal.ZERO;
		
		if(taskList != null)  {
			for(Task task : taskList)  {
				planningLineAmount = planningLineAmount.add(this.getPlanningLinesAmountNotAccounted(task));
			}
		}
		
		return planningLineAmount;
		
	}
	
	public BigDecimal getPlanningLinesAmountNotAccounted(Task task) throws AxelorException  {
		
		Query q = JPA.em().createQuery("select MAX(tl.date) FROM TimesheetLine as tl WHERE tl.task = ?1 and tl.timesheet.statusSelect = 3");
		q.setParameter(1, task);
				
		LocalDate timesheetLineMaxDate = (LocalDate) q.getSingleResult();
		
		return  this.getPlanningLinesAmount(task, timesheetLineMaxDate);
	}
	

	
	public BigDecimal getPlanningLinesAmount(Task task, LocalDate startDate) throws AxelorException  {
		BigDecimal planningLinesAmount = BigDecimal.ZERO;
		
		for(PlanningLine planningLine : task.getPlanningLineList())  {
			if(startDate == null || planningLine.getFromDateTime().isAfter(startDate))  {
				Employee employee = planningLine.getEmployee();
				Product profil = planningLine.getProduct();
				if(employee != null)  {
					planningLinesAmount = planningLinesAmount.
							add(employee.getDailySalaryCost().
									multiply(unitConversionService.
											convert(
													planningLine.getUnit(), 
													Unit.all().filter("self.code = 'JR'").fetchOne(), 
													planningLine.getDuration())));
				}
				else if(profil != null)  {
					planningLinesAmount = planningLinesAmount.
							add(profil.getCostPrice().
									multiply(unitConversionService.
											convert(
													planningLine.getUnit(), 
													profil.getUnit(), 
													planningLine.getDuration())));
				}
			}
		}
		
		return planningLinesAmount;
	}
	
	
	public BigDecimal getTaskProgressTurnover(List<Task> taskList)  {
		
		BigDecimal taskProgressTurnover = BigDecimal.ZERO;
		
		if(taskList != null)  {
			for(Task task : taskList)  {
				taskProgressTurnover = taskProgressTurnover.add(this.getTaskProgressTurnover(task));
			}
		}
		
		return taskProgressTurnover;
	}
	
	
	
	public BigDecimal getTaskProgressTurnover(Task task)  {
		
		return new BigDecimal(task.getTaskProgress()/100).multiply(task.getEstimatedAmount());
		
	}
	
	
	
	public void updateTaskProgress(List<Task> taskList)  {
		
		if(taskList != null)  {
			for(Task task : taskList)  {
				this.updateTaskProgress(task);
			}
		}
		
	}
	
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateTaskProgress(Task task)  {
		
		switch (task.getRealEstimatedMethodSelect()) {
			case ITask.REAL_ESTIMATED_METHOD_NONE:
				
				break;
				
			case ITask.REAL_ESTIMATED_METHOD_PROGRESS:
				
				task.setTaskProgress(
						this.computeTaskProgressMethodProgress(task));
				
				break;
				
			case ITask.REAL_ESTIMATED_METHOD_SUBSCRIPTION:
				
				task.setTaskProgress(
						this.computeTaskProgressMethodSubscription(task));
				break;
	
			default:
				break;
		}
		
		if(task.getTaskProgress() == 100)  {
			task.setStatusSelect(ITask.STATUS_COMPLETED);
		}
		task.save();
		
	}
	
	
	public int computeTaskProgressMethodProgress(Task task)  {
		
		BigDecimal taskProgress = BigDecimal.ZERO;
		
		BigDecimal totalTime = task.getTotalTime();
		if(totalTime.compareTo(BigDecimal.ZERO) == 1)  {
			taskProgress = task.getSpentTime().multiply(new BigDecimal(100)).divide(totalTime, 2, BigDecimal.ROUND_UP);
		}
		
		return taskProgress.intValue();
	}
	
	
	public int computeTaskProgressMethodSubscription(Task task)  {
		
		LocalDateTime startDateTime = task.getStartDateT();
		LocalDateTime endDateTime = task.getEndDateT();
		
		if(startDateTime != null && endDateTime != null && this.todayTime.isAfter(startDateTime))  {
			if(this.todayTime.isAfter(endDateTime))  {
				return 100;
			}
 			
			int total = DurationTool.getMinutesDuration(DurationTool.computeDuration(startDateTime, endDateTime));
			
			if(total == 0)  {
				return 0;
			}
			
			int realized = DurationTool.getMinutesDuration(DurationTool.computeDuration(startDateTime, this.todayTime));
			
			return 100 * realized / total;
			
		}
		
		return 0;
	}
	
	
	/**
	 * Méthode permettant de calculer la somme des durées de la liste de planning et 
	 * d'assigner la quantité, l'unité, et la date de fin à la tâche courante.
	 * @param planningLineList La liste des lignes de planning
	 * @param task La tâche courante
	 * @throws AxelorException Les unités demandés ne se trouvent pas dans la liste de conversion
	 */
	public LocalDateTime getTaskEndDate(Task task) throws AxelorException {

		LocalDateTime laterDate = task.getEndDateT();
		
		if(task.getPlanningLineList() != null)  {
			
			for(PlanningLine planningLine : task.getPlanningLineList())  {
				if(laterDate == null || laterDate.compareTo(planningLine.getToDateTime()) < 0) {
					laterDate = planningLine.getToDateTime();
				}
			}
		}
		
		return laterDate;
	}
	
}
