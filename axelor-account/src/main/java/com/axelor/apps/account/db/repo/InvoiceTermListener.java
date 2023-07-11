package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.service.move.record.MoveRecordUpdateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.inject.Beans;
import javax.persistence.PostUpdate;

public class InvoiceTermListener {
  @PostUpdate
  protected void updateMovePfpValidateStatus(InvoiceTerm invoiceTerm) throws AxelorException {
    if (invoiceTerm.getMoveLine() != null) {
      Beans.get(MoveRecordUpdateService.class)
          .updateInvoiceTerms(invoiceTerm.getMoveLine().getMove(), false, false);
    }
  }
}
