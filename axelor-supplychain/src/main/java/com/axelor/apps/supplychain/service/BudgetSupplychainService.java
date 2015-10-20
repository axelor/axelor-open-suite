package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.LocalDate;

import com.axelor.apps.account.db.Budget;
import com.axelor.apps.account.db.BudgetDistribution;
import com.axelor.apps.account.db.BudgetLine;
import com.axelor.apps.account.db.repo.BudgetDistributionRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.BudgetService;
import com.axelor.apps.purchase.db.IPurchaseOrder;
import com.axelor.inject.Beans;

public class BudgetSupplychainService extends BudgetService{
	
	
	@Override
	public List<BudgetLine> updateLines(Budget budget){
		if(budget.getBudgetLineList() != null && !budget.getBudgetLineList().isEmpty()){
			for (BudgetLine budgetLine : budget.getBudgetLineList()) {
				budgetLine.setAmountCommitted(BigDecimal.ZERO);
				budgetLine.setAmountRealized(BigDecimal.ZERO);
			}
			List<BudgetDistribution> budgetDistributionList = null;
			budgetDistributionList = Beans.get(BudgetDistributionRepository.class).all().filter("self.budget.id = ?1 AND (self.purchaseOrderLine.purchaseOrder.statusSelect = ?2 OR self.purchaseOrderLine.purchaseOrder.statusSelect = ?3)", budget.getId(), IPurchaseOrder.STATUS_VALIDATED, IPurchaseOrder.STATUS_FINISHED).fetch();
			for (BudgetDistribution budgetDistribution : budgetDistributionList) {
				LocalDate orderDate = budgetDistribution.getPurchaseOrderLine().getPurchaseOrder().getOrderDate();
				if(orderDate != null){
					for (BudgetLine budgetLine : budget.getBudgetLineList()) {
						LocalDate fromDate = budgetLine.getFromDate();
						LocalDate toDate = budgetLine.getToDate();
						if((fromDate.isBefore(orderDate) || fromDate.isEqual(orderDate)) && (toDate.isAfter(orderDate) || toDate.isEqual(orderDate))){
							budgetLine.setAmountCommitted(budgetLine.getAmountCommitted().add(budgetDistribution.getAmount()));
							break;
						}
					}
				}
			}
			budgetDistributionList = Beans.get(BudgetDistributionRepository.class).all().filter("self.budget.id = ?1 AND (self.invoiceLine.invoice.statusSelect = ?2 OR self.invoiceLine.invoice.statusSelect = ?3)", budget.getId(), InvoiceRepository.STATUS_VALIDATED, InvoiceRepository.STATUS_VENTILATED).fetch();
			for (BudgetDistribution budgetDistribution : budgetDistributionList) {
				LocalDate orderDate = budgetDistribution.getInvoiceLine().getInvoice().getInvoiceDate();
				if(orderDate != null){
					for (BudgetLine budgetLine : budget.getBudgetLineList()) {
						LocalDate fromDate = budgetLine.getFromDate();
						LocalDate toDate = budgetLine.getToDate();
						if((fromDate.isBefore(orderDate) || fromDate.isEqual(orderDate)) && (toDate.isAfter(orderDate) || toDate.isEqual(orderDate))){
							budgetLine.setAmountRealized(budgetLine.getAmountRealized().add(budgetDistribution.getAmount()));
							break;
						}
					}
				}
			}
		}
		return budget.getBudgetLineList();
	}
}
