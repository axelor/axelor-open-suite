/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.move.massentry;

import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.repo.MoveLineMassEntryRepository;
import com.axelor.apps.account.service.moveline.massentry.MoveLineMassEntryRecordService;
import com.axelor.apps.account.service.moveline.massentry.MoveLineMassEntryService;
import com.axelor.apps.base.db.Company;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.List;

public class MassEntryServiceImpl implements MassEntryService {

  protected MoveLineMassEntryService moveLineMassEntryService;
  protected MoveLineMassEntryRecordService moveLineMassEntryRecordService;
  protected MassEntryToolService massEntryToolService;

  @Inject
  public MassEntryServiceImpl(
      MoveLineMassEntryService moveLineMassEntryService,
      MoveLineMassEntryRecordService moveLineMassEntryRecordService,
      MassEntryToolService massEntryToolService) {
    this.moveLineMassEntryService = moveLineMassEntryService;
    this.moveLineMassEntryRecordService = moveLineMassEntryRecordService;
    this.massEntryToolService = massEntryToolService;
  }

  @Override
  public MoveLineMassEntry getFirstMoveLineMassEntryInformations(
      List<MoveLineMassEntry> moveLineList, MoveLineMassEntry inputLine, Company company) {
    if (ObjectUtils.notEmpty(moveLineList)) {
      inputLine.setInputAction(MoveLineMassEntryRepository.MASS_ENTRY_INPUT_ACTION_LINE);
      if (inputLine.getTemporaryMoveNumber() <= 0) {
        inputLine.setTemporaryMoveNumber(
            massEntryToolService.getMaxTemporaryMoveNumber(moveLineList));
        inputLine.setCounter(moveLineList.size() + 1);
      }

      for (MoveLineMassEntry moveLine : moveLineList) {
        if (moveLine.getTemporaryMoveNumber().equals(inputLine.getTemporaryMoveNumber())) {
          inputLine.setPartner(moveLine.getPartner());
          inputLine.setPartnerId(moveLine.getPartnerId());
          inputLine.setPartnerSeq(moveLine.getPartnerSeq());
          inputLine.setPartnerFullName(moveLine.getPartnerFullName());
          inputLine.setDate(moveLine.getDate());
          inputLine.setDueDate(moveLine.getDueDate());
          inputLine.setOriginDate(moveLine.getOriginDate());
          inputLine.setOrigin(moveLine.getOrigin());
          inputLine.setMoveStatusSelect(moveLine.getMoveStatusSelect());
          inputLine.setInterbankCodeLine(moveLine.getInterbankCodeLine());
          inputLine.setMoveDescription(moveLine.getMoveDescription());
          inputLine.setDescription(moveLine.getMoveDescription());
          inputLine.setExportedDirectDebitOk(moveLine.getExportedDirectDebitOk());
          inputLine.setMovePaymentCondition(moveLine.getMovePaymentCondition());
          inputLine.setMovePaymentMode(moveLine.getMovePaymentMode());
          inputLine.setMovePartnerBankDetails(moveLine.getMovePartnerBankDetails());
          inputLine.setCutOffStartDate(moveLine.getCutOffStartDate());
          inputLine.setCutOffEndDate(moveLine.getCutOffEndDate());
          inputLine.setDeliveryDate(moveLine.getDeliveryDate());
          inputLine.setVatSystemSelect(moveLine.getVatSystemSelect());
          inputLine.setIsEdited(MoveLineMassEntryRepository.MASS_ENTRY_IS_EDITED_NULL);
          moveLineMassEntryRecordService.setAnalytics(inputLine, moveLine);
          moveLineMassEntryRecordService.fillAnalyticMoveLineList(inputLine, moveLine);
          break;
        }
      }
    } else {
      inputLine = moveLineMassEntryService.createMoveLineMassEntry(company);
    }
    return inputLine;
  }
}
