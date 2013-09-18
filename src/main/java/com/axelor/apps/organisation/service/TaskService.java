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

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.UserInfo;
import com.axelor.apps.organisation.db.Employee;
import com.axelor.apps.organisation.db.Task;
import com.axelor.db.JPA;



public class TaskService {

	
//	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateFinancialInformation(Task task)  {
		
		
		this.updateEstimatedAmount(task);
		this.updateConfirmedAmount(task);
//		this.updateRealizedAmount(task);
		
		
	}
	
	
	public void updateEstimatedAmount(Task task)  {
		
		Query q = JPA.em().createQuery("select SUM(sol.exTaxTotal) FROM SalesOrderLine as sol WHERE sol.task = ?1 AND sol.salesOrder.statusSelect = 2");
		q.setParameter(1, task);
				
		BigDecimal estimatedTurnover = (BigDecimal) q.getSingleResult();
		
		q = JPA.em().createQuery("select SUM(pol.exTaxTotal) FROM PurchaseOrderLine as pol WHERE pol.task = ?1 AND pol.purchaseOrder.statusSelect = 2");
		q.setParameter(1, task);
				
		BigDecimal purchaseOrderEstimatedCost = (BigDecimal) q.getSingleResult();
		
		q = JPA.em().createQuery("select SUM(sol.product.costPrice * sol.qty) FROM SalesOrderLine as sol WHERE sol.task = ?1 AND sol.salesOrder.statusSelect = 2");
		q.setParameter(1, task);
				
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
		
		task.setEstimatedTurnover(estimatedTurnover);
		task.setEstimatedCost(estimatedCost);
		task.setEstimatedMargin(estimatedMargin);
	}
	
	
	public void updateConfirmedAmount(Task task)  {
		
		Query q = JPA.em().createQuery("select SUM(sol.exTaxTotal) FROM SalesOrderLine as sol WHERE sol.task = ?1 AND sol.salesOrder.statusSelect = 3");
		q.setParameter(1, task);
				
		BigDecimal confirmedTurnover = (BigDecimal) q.getSingleResult();
		
		q = JPA.em().createQuery("select SUM(pol.exTaxTotal) FROM PurchaseOrderLine as pol WHERE pol.task = ?1 AND pol.purchaseOrder.statusSelect = 3");
		q.setParameter(1, task);
				
		BigDecimal purchaseOrderConfirmedCost = (BigDecimal) q.getSingleResult();
		
		q = JPA.em().createQuery("select SUM(sol.product.costPrice * sol.qty) FROM SalesOrderLine as sol WHERE sol.task = ?1 AND sol.salesOrder.statusSelect = 3");
		q.setParameter(1, task);
				
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
		
		task.setConfirmedTurnover(confirmedTurnover);
		task.setConfirmedCost(confirmedCost);
		task.setConfirmedMargin(confirmedMargin);
	}
	
	
	public void updateRealizedAmount(Task task)  {
		
		Query q = JPA.em().createQuery("select SUM(il.exTaxTotal) FROM InvoiceLine as il WHERE il.task = ?1 AND il.invoice.status.code = 'dis' AND (il.invoice.operationTypeSelect = 3 OR il.invoice.operationTypeSelect = 4)");
		q.setParameter(1, task);
				
		BigDecimal realizedTurnover = (BigDecimal) q.getSingleResult();
		
		q = JPA.em().createQuery("select SUM(il.exTaxTotal) FROM InvoiceLine as il WHERE il.task = ?1 AND il.invoice.status.code = 'dis' AND (il.invoice.operationTypeSelect = 1 OR il.invoice.operationTypeSelect = 2)");
		q.setParameter(1, task);
				
		BigDecimal supplierInvoiceLineRealizedCost = (BigDecimal) q.getSingleResult();
		
		q = JPA.em().createQuery("select SUM(il.product.costPrice * sol.qty) FROM InvoiceLine as il WHERE il.task = ?1 AND il.invoice.status.code = 'dis' AND (il.invoice.operationTypeSelect = 3 OR il.invoice.operationTypeSelect = 4)");
		q.setParameter(1, task);
				
		BigDecimal customerInvoiceLineRealizedCost = (BigDecimal) q.getSingleResult();
		
		
		q = JPA.em().createQuery("select SUM(tl.userInfo.employee.dailySalaryCost * tl.duration) FROM TimesheetLine as tl WHERE tl.task = ?1 and tl.statusSelect = 3");
		q.setParameter(1, task);
				
		BigDecimal timesheetLineRealizedCost = (BigDecimal) q.getSingleResult();
			
		
		q = JPA.em().createQuery("select SUM(el.total) FROM ExpenseLine as el WHERE el.task = ?1 and el.statusSelect = 4");
		q.setParameter(1, task);
				
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
		
		task.setRealizedTurnover(realizedTurnover);
		task.setRealizedCost(realizedCost);
		task.setRealizedMargin(realizedMargin);
	}


	
	
	
	/**  SalesOrder  **/
//	
//	public void completeSubmittedSalesOrderLines(SalesOrder salesOrder)  {
//		if (salesOrder.getSalesOrderLineList() != null) {
//			for(SalesOrderLine salesOrderLine : salesOrder.getSalesOrderLineList())  {
//				this.addSubmittedSalesOrderLine(salesOrderLine);
//			}
//		}
//	}
//	
//	public void completeValidatedSalesOrderLines(SalesOrder salesOrder)  {
//		if (salesOrder.getSalesOrderLineList() != null) {
//			for(SalesOrderLine salesOrderLine : salesOrder.getSalesOrderLineList())  {
//				this.addValidatedSalesOrderLine(salesOrderLine);
//			}
//		}
//	}
//		
//	public void addSubmittedSalesOrderLine(SalesOrderLine salesOrderLine)  {
//		Task task = salesOrderLine.getTask();
//		if(task != null)  {
//			if(task.getSubmittedSalesOrderLineSet() == null)  {
//				task.setSubmittedSalesOrderLineSet(new HashSet<SalesOrderLine>());
//			}
//			if(!task.getSubmittedSalesOrderLineSet().contains(salesOrderLine))  {
//				task.getSubmittedSalesOrderLineSet().add(salesOrderLine);
//			}
//		}
//	}
//	
//	public void addValidatedSalesOrderLine(SalesOrderLine salesOrderLine)  {
//		Task task = salesOrderLine.getTask();
//		if(task != null)  {
//			if(task.getValidatedSalesOrderLineSet() == null)  {
//				task.setValidatedSalesOrderLineSet(new HashSet<SalesOrderLine>());
//			}
//			if(!task.getValidatedSalesOrderLineSet().contains(salesOrderLine))  {
//				task.getValidatedSalesOrderLineSet().add(salesOrderLine);
//			}
//		}
//	}
//	
//	
/**  PurchaseOrder  **/
//	
//	public void completeSubmittedPurchaseOrderLines(PurchaseOrder purchaseOrder)  {
//		if (purchaseOrder.getPurchaseOrderLineList() != null) {
//			for(PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList())  {
//				this.addSubmittedPurchaseOrderLine(purchaseOrderLine);
//			}
//		}
//	}
//	
//	public void completeValidatedPurchaseOrderLines(PurchaseOrder purchaseOrder)  {
//		if (purchaseOrder.getPurchaseOrderLineList() != null) {
//			for(PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList())  {
//				this.addValidatedPurchaseOrderLine(purchaseOrderLine);
//			}
//		}
//	}
//		
//	public void addSubmittedPurchaseOrderLine(PurchaseOrderLine purchaseOrderLine)  {
//		Task task = purchaseOrderLine.getTask();
//		if(task != null)  {
//			if(task.getSubmittedSalesOrderLineSet() == null)  {
//				task.setSubmittedSalesOrderLineSet(new HashSet<SalesOrderLine>());
//			}
//			if(!task.getSubmittedPurchaseOrderLineSet().contains(purchaseOrderLine))  {
//				task.getSubmittedPurchaseOrderLineSet().add(purchaseOrderLine);
//			}
//		}
//	}
//	
//	public void addValidatedPurchaseOrderLine(PurchaseOrderLine purchaseOrderLine)  {
//		Task task = purchaseOrderLine.getTask();
//		if(task != null)  {
//			if(task.getValidatedSalesOrderLineSet() == null)  {
//				task.setValidatedSalesOrderLineSet(new HashSet<SalesOrderLine>());
//			}
//			if(!task.getValidatedPurchaseOrderLineSet().contains(purchaseOrderLine))  {
//				task.getValidatedPurchaseOrderLineSet().add(purchaseOrderLine);
//			}
//		}
//	}
	
}
