package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpToolService;
import com.google.inject.Inject;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class MovePfpToolServiceImpl implements MovePfpToolService {

  protected InvoiceTermPfpToolService invoiceTermPfpToolService;
  protected MoveToolService moveToolService;

  @Inject
  public MovePfpToolServiceImpl(
      InvoiceTermPfpToolService invoiceTermPfpToolService, MoveToolService moveToolService) {
    this.invoiceTermPfpToolService = invoiceTermPfpToolService;
    this.moveToolService = moveToolService;
  }

  @Override
  public Integer checkOtherInvoiceTerms(Move move) {
    if (move == null || CollectionUtils.isEmpty(move.getMoveLineList())) {
      return null;
    }
    List<InvoiceTerm> invoiceTermList = moveToolService._getInvoiceTermList(move);
    if (!CollectionUtils.isEmpty(invoiceTermList)) {
      return invoiceTermPfpToolService.checkOtherInvoiceTerms(invoiceTermList);
    }
    return null;
  }

  @Override
  public void fillMovePfpValidateStatus(Move move) {
    Integer pfpStatus = checkOtherInvoiceTerms(move);
    if (pfpStatus != null) {
      move.setPfpValidateStatusSelect(pfpStatus);
    }
  }
}
