package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.google.inject.Inject;
import org.apache.commons.collections.CollectionUtils;

public class MoveInvoiceTermServiceImpl implements MoveInvoiceTermService {
  protected MoveLineInvoiceTermService moveLineInvoiceTermService;

  @Inject
  public MoveInvoiceTermServiceImpl(MoveLineInvoiceTermService moveLineInvoiceTermService) {
    this.moveLineInvoiceTermService = moveLineInvoiceTermService;
  }

  @Override
  public void generateInvoiceTerms(Move move) {
    if (CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      move.getMoveLineList().stream()
          .filter(
              it ->
                  it.getAccount() != null
                      && it.getAccount().getHasInvoiceTerm()
                      && CollectionUtils.isEmpty(it.getInvoiceTermList()))
          .forEach(moveLineInvoiceTermService::generateDefaultInvoiceTerm);
    }
  }
}
