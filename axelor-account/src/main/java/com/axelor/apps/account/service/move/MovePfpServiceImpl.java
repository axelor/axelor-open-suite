/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.PfpService;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.Company;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class MovePfpServiceImpl implements MovePfpService {
  protected MoveRepository moveRepository;

  protected InvoiceTermPfpService invoiceTermPfpService;
  protected AccountingSituationService accountingSituationService;
  protected PfpService pfpService;

  @Inject
  public MovePfpServiceImpl(
      MoveRepository moveRepository,
      InvoiceTermPfpService invoiceTermPfpService,
      AccountingSituationService accountingSituationService,
      PfpService pfpService) {
    this.moveRepository = moveRepository;
    this.invoiceTermPfpService = invoiceTermPfpService;
    this.accountingSituationService = accountingSituationService;
    this.pfpService = pfpService;
  }

  @Transactional
  @Override
  public void validatePfp(Long moveId) {
    Move move = moveRepository.find(moveId);
    User pfpValidatorUser =
        move.getPfpValidatorUser() != null ? move.getPfpValidatorUser() : AuthUtils.getUser();

    _getInvoiceTermList(move)
        .forEach(invoiceTerm -> invoiceTermPfpService.validatePfp(invoiceTerm, pfpValidatorUser));

    move.setPfpValidatorUser(pfpValidatorUser);
    move.setPfpValidateStatusSelect(InvoiceRepository.PFP_STATUS_VALIDATED);
  }

  @Override
  @Transactional
  public void refusalToPay(
      Move move, CancelReason reasonOfRefusalToPay, String reasonOfRefusalToPayStr) {

    User currentUser = AuthUtils.getUser();

    _getInvoiceTermList(move)
        .forEach(
            invoiceTerm ->
                invoiceTermPfpService.refusalToPay(
                    invoiceTerm, reasonOfRefusalToPay, reasonOfRefusalToPayStr));

    move.setPfpValidatorUser(currentUser);
    move.setPfpValidateStatusSelect(MoveRepository.PFP_STATUS_LITIGATION);
    move.setReasonOfRefusalToPay(reasonOfRefusalToPay);
    move.setReasonOfRefusalToPayStr(
        reasonOfRefusalToPayStr != null ? reasonOfRefusalToPayStr : reasonOfRefusalToPay.getName());

    moveRepository.save(move);
  }

  @Override
  public boolean isPfpButtonVisible(Move move, User user, boolean litigation)
      throws AxelorException {
    boolean pfpCondition = this._getPfpCondition(move);

    boolean validatorUserCondition =
        invoiceTermPfpService.getUserCondition(move.getPfpValidatorUser(), user);

    boolean statusCondition = this._getStatusCondition(move);

    boolean pfpValidateStatusCondition = this._getPfpValidateStatusCondition(move, litigation);

    List<InvoiceTerm> invoiceTermList = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      invoiceTermList.addAll(_getInvoiceTermList(move));
    }

    boolean invoiceTermsCondition = invoiceTermPfpService.getInvoiceTermsCondition(invoiceTermList);

    return pfpCondition
        && validatorUserCondition
        && statusCondition
        && pfpValidateStatusCondition
        && invoiceTermsCondition;
  }

  @Override
  public void setPfpStatus(Move move) throws AxelorException {
    Company company = move.getCompany();

    if (this._getPfpCondition(move)) {
      AccountingSituation accountingSituation =
          accountingSituationService.getAccountingSituation(move.getPartner(), company);
      if (accountingSituation != null) {
        move.setPfpValidatorUser(accountingSituation.getPfpValidatorUser());
      }
      move.setPfpValidateStatusSelect(MoveRepository.PFP_STATUS_AWAITING);
    } else {
      move.setPfpValidateStatusSelect(MoveRepository.PFP_NONE);
    }
  }

  protected boolean _getStatusCondition(Move move) {
    return move.getStatusSelect() == MoveRepository.STATUS_DAYBOOK
        || move.getStatusSelect() == MoveRepository.STATUS_ACCOUNTED;
  }

  @Override
  public boolean isValidatorUserVisible(Move move) throws AxelorException {
    boolean pfpCondition = this._getPfpCondition(move);

    boolean statusCondition = this._getStatusNotNewCondition(move);

    return statusCondition && pfpCondition;
  }

  protected boolean _getPfpValidateStatusCondition(Move move, boolean litigation) {
    return move.getPfpValidateStatusSelect() == MoveRepository.PFP_STATUS_AWAITING
        || (!litigation
            || move.getPfpValidateStatusSelect() == MoveRepository.PFP_STATUS_LITIGATION);
  }

  protected List<InvoiceTerm> _getInvoiceTermList(Move move) {
    List<InvoiceTerm> invoiceTermList = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      invoiceTermList.addAll(
          move.getMoveLineList().stream()
              .map(MoveLine::getInvoiceTermList)
              .filter(Objects::nonNull)
              .flatMap(Collection::stream)
              .collect(Collectors.toList()));
    }
    return invoiceTermList;
  }

  protected boolean _getStatusNotNewCondition(Move move) {
    return move.getStatusSelect() > MoveRepository.STATUS_NEW;
  }

  protected boolean _getPfpCondition(Move move) throws AxelorException {
    return pfpService.isManagePassedForPayment(move.getCompany())
        && this._getJournalTypePurchaseCondition(move);
  }

  protected boolean _getJournalTypePurchaseCondition(Move move) throws AxelorException {
    Company company = move.getCompany();
    if (move.getJournal() == null) {
      return false;
    }

    boolean isSupplierPurchase =
        move.getJournal().getJournalType().getTechnicalTypeSelect()
            == JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE;
    boolean isSupplierRefund =
        move.getJournal().getJournalType().getTechnicalTypeSelect()
            == JournalTypeRepository.TECHNICAL_TYPE_SELECT_CREDIT_NOTE;

    return pfpService.isManagePassedForPayment(company)
        && (isSupplierPurchase || (isSupplierRefund && pfpService.isManagePFPInRefund(company)));
  }
}
