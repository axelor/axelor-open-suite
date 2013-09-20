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

import javax.persistence.Query;

import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.organisation.db.Task;
import com.axelor.db.JPA;
import com.google.inject.persist.Transactional;

public class ProjectService {

	@Transactional
	public void createDefaultTask(Project project) {

		if(project.getAffairName() != null && !project.getAffairName().isEmpty()) {
			Task findTask = Task.all().filter("name = ?", project.getAffairName()).fetchOne();
			if(findTask == null) {
				Task task = new Task();

				task.setName(project.getAffairName());
				task.save();
			}
		}
	}

	@Transactional
	public void createPreSalesTask(Project project) {

		if(project.getAffairName() != null && !project.getAffairName().isEmpty()) {
			Task findTask = Task.all().filter("name = ?", "Avant vente "+project.getAffairName()).fetchOne();
			if(findTask == null) {
				Task task = new Task();

				task.setName("Avant vente "+project.getAffairName());
				task.save();
			}
		}
	}
	
	
	public void updateFinancialInformation(Project project)  {
		
		this.updateEstimatedAmount(project);
		this.updateConfirmedAmount(project);
		this.updateRealizedAmount(project);
		
	}
	
	
	public void updateEstimatedAmount(Project project)  {
		
		Query q = JPA.em().createQuery("select SUM(sol.exTaxTotal) FROM SalesOrderLine as sol WHERE sol.salesOrder.affairProject = ?1 AND sol.salesOrder.statusSelect = 2");
		q.setParameter(1, project);
				
		BigDecimal estimatedTurnover = (BigDecimal) q.getSingleResult();
		
		q = JPA.em().createQuery("select SUM(pol.exTaxTotal) FROM PurchaseOrderLine as pol WHERE pol.purchaseOrder.affairProject = ?1 AND pol.purchaseOrder.statusSelect = 2");
		q.setParameter(1, project);
				
		BigDecimal purchaseOrderEstimatedCost = (BigDecimal) q.getSingleResult();
		
		q = JPA.em().createQuery("select SUM(sol.product.costPrice * sol.qty) FROM SalesOrderLine as sol WHERE sol.salesOrder.affairProject = ?1 AND sol.salesOrder.statusSelect = 2");
		q.setParameter(1, project);
				
		BigDecimal salesOrderEstimatedCost = (BigDecimal) q.getSingleResult();
		
		BigDecimal estimatedCost = BigDecimal.ZERO;
		if(purchaseOrderEstimatedCost != null)  {
			estimatedCost = estimatedCost.add(purchaseOrderEstimatedCost);
		}
		if(salesOrderEstimatedCost != null)  {
			estimatedCost = estimatedCost.add(salesOrderEstimatedCost);
		}
		
		BigDecimal estimatedMargin = BigDecimal.ZERO;
		if(estimatedTurnover != null)  {
			estimatedMargin = estimatedCost.add(estimatedTurnover);
		}
		if(estimatedCost != null)  {
			estimatedMargin = estimatedMargin.subtract(estimatedCost);
		}
		
		project.setEstimatedTurnover(estimatedTurnover);
		project.setEstimatedCost(estimatedCost);
		project.setEstimatedMargin(estimatedMargin);
	}
	
	
	public void updateConfirmedAmount(Project project)  {
		
		Query q = JPA.em().createQuery("select SUM(sol.exTaxTotal) FROM SalesOrderLine as sol WHERE sol.salesOrder.affairProject = ?1 AND sol.salesOrder.statusSelect = 3");
		q.setParameter(1, project);
				
		BigDecimal confirmedTurnover = (BigDecimal) q.getSingleResult();
		
		q = JPA.em().createQuery("select SUM(pol.exTaxTotal) FROM PurchaseOrderLine as pol WHERE pol.purchaseOrder.affairProject = ?1 AND pol.purchaseOrder.statusSelect = 3");
		q.setParameter(1, project);
				
		BigDecimal purchaseOrderConfirmedCost = (BigDecimal) q.getSingleResult();
		
		q = JPA.em().createQuery("select SUM(sol.product.costPrice * sol.qty) FROM SalesOrderLine as sol WHERE sol.salesOrder.affairProject = ?1 AND sol.salesOrder.statusSelect = 3");
		q.setParameter(1, project);
				
		BigDecimal salesOrderConfirmedCost = (BigDecimal) q.getSingleResult();
		
		BigDecimal confirmedCost = BigDecimal.ZERO;
		if(purchaseOrderConfirmedCost != null)  {
			confirmedCost = confirmedCost.add(purchaseOrderConfirmedCost);
		}
		if(salesOrderConfirmedCost != null)  {
			confirmedCost = confirmedCost.add(salesOrderConfirmedCost);
		}
		
		BigDecimal confirmedMargin = BigDecimal.ZERO;
		if(confirmedTurnover != null)  {
			confirmedMargin = confirmedMargin.add(confirmedTurnover);
		}
		if(confirmedCost != null)  {
			confirmedMargin = confirmedMargin.subtract(confirmedCost);
		}
		
		project.setConfirmedTurnover(confirmedTurnover);
		project.setConfirmedCost(confirmedCost);
		project.setConfirmedMargin(confirmedMargin);
	}
	
	
	public void updateRealizedAmount(Project project)  {
		
		Query q = JPA.em().createQuery("select SUM(il.exTaxTotal) FROM InvoiceLine as il WHERE il.invoice.project = ?1 AND il.invoice.status.code = 'dis' AND (il.invoice.operationTypeSelect = 3 OR il.invoice.operationTypeSelect = 4)");
		q.setParameter(1, project);
				
		BigDecimal realizedTurnover = (BigDecimal) q.getSingleResult();
		
		q = JPA.em().createQuery("select SUM(il.exTaxTotal) FROM InvoiceLine as il WHERE il.invoice.project = ?1 AND il.invoice.status.code = 'dis' AND (il.invoice.operationTypeSelect = 1 OR il.invoice.operationTypeSelect = 2)");
		q.setParameter(1, project);
				
		BigDecimal supplierInvoiceLineRealizedCost = (BigDecimal) q.getSingleResult();
		
		q = JPA.em().createQuery("select SUM(il.product.costPrice * il.qty) FROM InvoiceLine as il WHERE il.invoice.project = ?1 AND il.invoice.status.code = 'dis' AND (il.invoice.operationTypeSelect = 3 OR il.invoice.operationTypeSelect = 4)");
		q.setParameter(1, project);
				
		BigDecimal customerInvoiceLineRealizedCost = (BigDecimal) q.getSingleResult();
		
		
		q = JPA.em().createQuery("select SUM(tl.timesheet.userInfo.employee.dailySalaryCost * tl.duration) FROM TimesheetLine as tl WHERE tl.timesheet.project = ?1 and tl.timesheet.statusSelect = 3");
		q.setParameter(1, project);
				
		BigDecimal timesheetLineRealizedCost = (BigDecimal) q.getSingleResult();
			
		
		q = JPA.em().createQuery("select SUM(el.total) FROM ExpenseLine as el WHERE el.expense.project = ?1 and el.expense.statusSelect = 4");
		q.setParameter(1, project);
				
		BigDecimal expenseLineRealizedCost = (BigDecimal) q.getSingleResult();
		
		
		
		BigDecimal realizedCost = BigDecimal.ZERO;
		if(supplierInvoiceLineRealizedCost != null)  {
			realizedCost = realizedCost.add(supplierInvoiceLineRealizedCost);
		}
		if(customerInvoiceLineRealizedCost != null)  {
			realizedCost = realizedCost.add(customerInvoiceLineRealizedCost);
		}
		if(timesheetLineRealizedCost != null)  {
			realizedCost = realizedCost.add(timesheetLineRealizedCost);
		}
		if(expenseLineRealizedCost != null)  {
			realizedCost = realizedCost.add(expenseLineRealizedCost);
		}
		
		BigDecimal realizedMargin = BigDecimal.ZERO;
		if(realizedTurnover != null)  {
			realizedMargin = realizedMargin.add(realizedTurnover);
		}
		if(realizedCost != null)  {
			realizedMargin = realizedMargin.subtract(realizedCost);
		}
		
		project.setRealizedTurnover(realizedTurnover);
		project.setRealizedCost(realizedCost);
		project.setRealizedMargin(realizedMargin);
	}
}
