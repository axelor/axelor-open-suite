package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.apache.commons.collections.CollectionUtils;

public class MoveInvoiceTermServiceImpl implements MoveInvoiceTermService {
  protected MoveLineInvoiceTermService moveLineInvoiceTermService;
  protected MoveRepository moveRepo;

  @Inject
  public MoveInvoiceTermServiceImpl(
      MoveLineInvoiceTermService moveLineInvoiceTermService, MoveRepository moveRepo) {
    this.moveLineInvoiceTermService = moveLineInvoiceTermService;
    this.moveRepo = moveRepo;
  }

  @Override
  public void generateInvoiceTerms(Move move) {
    if (CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      move.getMoveLineList().stream()
          .filter(
              it ->
                  it.getAccount() != null
                      && it.getAccount().getUseForPartnerBalance()
                      && CollectionUtils.isEmpty(it.getInvoiceTermList()))
          .forEach(moveLineInvoiceTermService::generateDefaultInvoiceTerm);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  public void updateInvoiceTermsParentFields(Move move) {
    move.getMoveLineList().forEach(moveLineInvoiceTermService::updateInvoiceTermsParentFields);
    moveRepo.save(move);
  }
}
