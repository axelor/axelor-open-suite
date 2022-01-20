package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.InvoiceTerm;

public class InvoiceTermAccountRepository extends InvoiceTermRepository {
  @Override
  public void remove(InvoiceTerm entity) {
    if (!entity.getIsPaid()) {
      super.remove(entity);
    }
  }
}
