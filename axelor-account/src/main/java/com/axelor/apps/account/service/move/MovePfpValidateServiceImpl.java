package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpValidateService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MovePfpValidateServiceImpl implements MovePfpValidateService {

  protected InvoiceTermPfpValidateService invoiceTermPfpValidateService;
  protected MoveToolService moveToolService;
  protected MoveRepository moveRepository;

  @Inject
  public MovePfpValidateServiceImpl(
      InvoiceTermPfpValidateService invoiceTermPfpValidateService,
      MoveToolService moveToolService,
      MoveRepository moveRepository) {
    this.invoiceTermPfpValidateService = invoiceTermPfpValidateService;
    this.moveToolService = moveToolService;
    this.moveRepository = moveRepository;
  }

  @Transactional
  @Override
  public void validatePfp(Long moveId) {
    Move move = moveRepository.find(moveId);
    User pfpValidatorUser =
        move.getPfpValidatorUser() != null ? move.getPfpValidatorUser() : AuthUtils.getUser();

    moveToolService
        ._getInvoiceTermList(move)
        .forEach(
            invoiceTerm ->
                invoiceTermPfpValidateService.validatePfp(invoiceTerm, pfpValidatorUser));

    move.setPfpValidatorUser(pfpValidatorUser);
    move.setPfpValidateStatusSelect(InvoiceRepository.PFP_STATUS_VALIDATED);
  }
}
