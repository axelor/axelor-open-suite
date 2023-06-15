package com.axelor.apps.budget.db.repo;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.service.invoice.BudgetInvoiceLineService;
import com.axelor.apps.businessproject.db.repo.InvoiceProjectRepository;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import javax.persistence.PersistenceException;
import org.apache.commons.collections.CollectionUtils;

public class BudgetInvoiceRepository extends InvoiceProjectRepository {

  @Override
  public Invoice copy(Invoice entity, boolean deep) {
    Invoice copy = super.copy(entity, deep);

    if (deep) {
      if (copy.getInvoiceLineList() != null && !copy.getInvoiceLineList().isEmpty()) {
        for (InvoiceLine invoiceLine : copy.getInvoiceLineList()) {
          invoiceLine.setBudget(null);
          invoiceLine.setBudgetDistributionSumAmount(BigDecimal.ZERO);
          invoiceLine.clearBudgetDistributionList();
        }
      }
      copy.setBudgetDistributionGenerated(false);
    }

    return copy;
  }

  @Override
  public Invoice save(Invoice invoice) {
    try {
      if (!CollectionUtils.isEmpty(invoice.getInvoiceLineList())) {
        BudgetInvoiceLineService budgetInvoiceLineService =
            Beans.get(BudgetInvoiceLineService.class);
        for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
          budgetInvoiceLineService.checkAmountForInvoiceLine(invoiceLine);
        }
      }
    } catch (AxelorException e) {
      throw new PersistenceException(e.getLocalizedMessage());
    }

    super.save(invoice);
    return invoice;
  }
}
