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
package com.axelor.apps.organisation.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.SpentTime;
import com.axelor.apps.base.db.Unit;
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
import com.axelor.apps.organisation.exceptions.IExceptionMessage;
import com.axelor.apps.tool.date.DurationTool;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;


public class TaskService {

	@Inject
	private UnitConversionService unitConversionService;
	
	@Inject
	private CurrencyService currencyService;
	
	@Inject
	private PriceListService priceListService;
	
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
			throw new AxelorException(I18n.get(IExceptionMessage.TASK_1), IException.CONFIGURATION_ERROR);
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
		
		Query q = JPA.em().createQuery("select SUM(sol.companyExTaxTotal) FROM SaleOrderLine as sol WHERE sol.task = ?1 AND sol.saleOrder.statusSelect = 3");
		q.setParameter(1, task);
				
		BigDecimal saleOrderTurnover = (BigDecimal) q.getSingleResult();
		
		BigDecimal financialInformationUpdateTurnover = this.getFinancialInformationUpdateAmount(task, ITaskUpdateLine.TYPE_REVENUE, ITaskUpdateLine.APPLICATION_INITIAL_ESTIMATED);
		
		BigDecimal turnover = BigDecimal.ZERO;
		if(saleOrderTurnover != null)  {
			turnover = turnover.add(saleOrderTurnover);
		}
		if(financialInformationUpdateTurnover != null)  {
			turnover = turnover.add(financialInformationUpdateTurnover);
		}
		
		/**  COST  **/
		
		q = JPA.em().createQuery("select SUM(pol.companyExTaxTotal) FROM PurchaseOrderLine as pol WHERE pol.task = ?1 AND pol.purchaseOrder.statusSelect = 3");
		q.setParameter(1, task);
				
		BigDecimal purchaseOrderCost = (BigDecimal) q.getSingleResult();
		
		BigDecimal saleOrderCost = this.getSaleOrderInitialEstimatedCost(task);
		
		BigDecimal financialInformationUpdateCost = this.getFinancialInformationUpdateAmount(task, ITaskUpdateLine.TYPE_COST, ITaskUpdateLine.APPLICATION_INITIAL_ESTIMATED);
		
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


	public BigDecimal getSaleOrderInitialEstimatedCost(Task task) throws AxelorException  {
		BigDecimal saleOrderEstimatedCost = BigDecimal.ZERO;
		
		if(task.getPlanningLineList() != null && !task.getPlanningLineList().isEmpty())  {
			saleOrderEstimatedCost = this.getPlanningLinesAmount(task, null);
			
			Query q = JPA.em().createQuery("select SUM(sol.companyCostPrice * sol.qty) FROM SaleOrderLine as sol WHERE sol.task = ?1 AND sol.saleOrder.statusSelect = 3 AND sol.product.applicationTypeSelect = 1");
			q.setParameter(1, task);
					
			saleOrderEstimatedCost = (BigDecimal) q.getSingleResult();
		}
		else  {
			Query q = JPA.em().createQuery("select SUM(sol.companyCostPrice * sol.qty) FROM SaleOrderLine as sol WHERE sol.task = ?1 AND sol.saleOrder.statusSelect = 3");
			q.setParameter(1, task);
					
			saleOrderEstimatedCost = (BigDecimal) q.getSingleResult();
		}
		
		return saleOrderEstimatedCost;
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
		if(task.getPlanningLineList() == null)
			return planningLinesAmount;
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
													Unit.findByCode("JR"), 
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
	
	public Task importTask(Object bean, Map values) {
        try{
        	Task task = (Task) bean;
        	task.setSpentTime(getSpentTime(task));
        	task.setPlannedTime(getPlannedTime(task));
        	updateFinancialInformation(task);
			return task;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
	}
	
}
