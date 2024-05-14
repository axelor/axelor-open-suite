/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.util;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.PfpService;
import com.axelor.apps.account.service.accountingsituation.AccountingSituationService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.utils.service.ArchivingService;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;

public class MoveUtilsServiceImpl implements MoveUtilsService {

  protected ArchivingService archivingService;
  protected AccountingSituationService accountingSituationService;
  protected PfpService pfpService;

  @Inject
  public MoveUtilsServiceImpl(
      ArchivingService archivingService,
      AccountingSituationService accountingSituationService,
      PfpService pfpService) {
    this.archivingService = archivingService;
    this.accountingSituationService = accountingSituationService;
    this.pfpService = pfpService;
  }

  @Override
  public void checkMoveBeforeRemove(Move move) throws Exception {
    String errorMessage = "";
    Map<String, String> objectsLinkToMoveMap = archivingService.getObjectLinkTo(move, move.getId());
    String moveModelError = null;
    for (Map.Entry<String, String> entry : objectsLinkToMoveMap.entrySet()) {
      String modelName = I18n.get(archivingService.getModelTitle(entry.getKey()));
      if (!entry.getKey().equals("MoveLine")) {
        if (moveModelError == null) {
          moveModelError = modelName;
        } else {
          moveModelError += ", " + modelName;
        }
      }
    }
    if (moveModelError != null && move.getStatusSelect() == MoveRepository.STATUS_DAYBOOK) {
      errorMessage +=
          String.format(
              I18n.get(AccountExceptionMessage.MOVE_ARCHIVE_NOT_OK_BECAUSE_OF_LINK_WITH),
              move.getReference(),
              moveModelError);
    } else if (moveModelError != null
        && (move.getStatusSelect() == MoveRepository.STATUS_NEW
            || move.getStatusSelect() == MoveRepository.STATUS_SIMULATED)) {
      errorMessage +=
          String.format(
              I18n.get(AccountExceptionMessage.MOVE_REMOVE_NOT_OK_BECAUSE_OF_LINK_WITH),
              move.getReference(),
              moveModelError);
    }

    for (MoveLine moveLine : move.getMoveLineList()) {

      errorMessage += checkMoveLineBeforeRemove(moveLine);
    }
    if (errorMessage != null && !errorMessage.isEmpty()) {
      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, errorMessage);
    }
  }

  protected String checkMoveLineBeforeRemove(MoveLine moveLine) throws AxelorException {
    String errorMessage = "";
    Map<String, String> objectsLinkToMoveLineMap =
        archivingService.getObjectLinkTo(moveLine, moveLine.getId());
    for (Map.Entry<String, String> entry : objectsLinkToMoveLineMap.entrySet()) {
      String modelName = entry.getKey();
      List<String> modelsToIgnore = getModelsToIgnoreList();
      if (!modelsToIgnore.contains(modelName)
          && moveLine.getMove().getStatusSelect() == MoveRepository.STATUS_DAYBOOK) {
        errorMessage +=
            String.format(
                I18n.get(AccountExceptionMessage.MOVE_LINE_ARCHIVE_NOT_OK_BECAUSE_OF_LINK_WITH),
                moveLine.getName(),
                modelName);
      } else if (!modelsToIgnore.contains(modelName)
          && (moveLine.getMove().getStatusSelect() == MoveRepository.STATUS_NEW
              || moveLine.getMove().getStatusSelect() == MoveRepository.STATUS_SIMULATED)) {
        errorMessage +=
            String.format(
                I18n.get(AccountExceptionMessage.MOVE_LINE_REMOVE_NOT_OK_BECAUSE_OF_LINK_WITH),
                moveLine.getName(),
                modelName);
      }
    }
    return errorMessage;
  }

  @Override
  public List<String> getModelsToIgnoreList() {
    return Lists.newArrayList(
        "Move", "Reconcile", "InvoiceTerm", "AnalyticMoveLine", "TaxPaymentMoveLine");
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

  @Override
  public boolean _getPfpCondition(Move move) throws AxelorException {
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
