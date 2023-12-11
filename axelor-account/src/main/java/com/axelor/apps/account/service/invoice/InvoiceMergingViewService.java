package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.invoice.InvoiceMergingService.InvoiceMergingResult;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import java.util.List;

public interface InvoiceMergingViewService {

  /**
   * Method that build a ActionViewBuilder for confirm view in the sale order merge process.
   *
   * @param InvoiceMergingResult
   * @return ActionViewBuilder
   */
  ActionViewBuilder buildConfirmView(InvoiceMergingResult result, List<Invoice> invoicesToMerge);
}
