/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.web;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.apps.bankpayment.report.ITranslation;
import com.axelor.apps.bankpayment.service.moveline.MoveLineGroupBankPaymentService;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class MoveLineController {
  public void bankReconciledAmountOnChange(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);

      response.setValues(
          Beans.get(MoveLineGroupBankPaymentService.class)
              .getBankReconciledAmountOnChangeValuesMap(moveLine));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setSelectedBankReconciliation(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);

      // Count currently selected moveLines
      MoveLineRepository moveLineRepository = Beans.get(MoveLineRepository.class);
      long selectedCount =
          moveLineRepository
              .all()
              .filter("self.isSelectedBankReconciliation = true")
              .count();

      // Check if we're trying to select (not unselect) and already have 2 selected
      boolean isCurrentlySelected =
          moveLine.getIsSelectedBankReconciliation() != null
              && moveLine.getIsSelectedBankReconciliation();

      // Block selection if trying to select a 3rd line (when 2 are already selected)
      if (!isCurrentlySelected && selectedCount >= 2) {
        response.setError(I18n.get(ITranslation.BANK_RECONCILIATION_MAX_TWO_MOVE_LINES));
        return;
      }

      // If selecting (not unselecting), perform additional validations
      if (!isCurrentlySelected) {
        // Check if moveLine already has a bankReconciledAmount
        if (moveLine.getBankReconciledAmount() != null
            && moveLine.getBankReconciledAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
          response.setError(
              I18n.get(ITranslation.BANK_RECONCILIATION_MOVE_LINE_ALREADY_RECONCILED));
          return;
        }

        // If there's already one selected moveLine, check debit vs credit
        if (selectedCount == 1) {
          MoveLine otherMoveLine =
              moveLineRepository
                  .all()
                  .filter("self.isSelectedBankReconciliation = true")
                  .fetchOne();

          if (otherMoveLine != null) {
            boolean currentIsDebit =
                moveLine.getDebit() != null
                    && moveLine.getDebit().compareTo(java.math.BigDecimal.ZERO) > 0;
            boolean currentIsCredit =
                moveLine.getCredit() != null
                    && moveLine.getCredit().compareTo(java.math.BigDecimal.ZERO) > 0;
            boolean otherIsDebit =
                otherMoveLine.getDebit() != null
                    && otherMoveLine.getDebit().compareTo(java.math.BigDecimal.ZERO) > 0;
            boolean otherIsCredit =
                otherMoveLine.getCredit() != null
                    && otherMoveLine.getCredit().compareTo(java.math.BigDecimal.ZERO) > 0;

            // Both are debit or both are credit - not allowed
            if ((currentIsDebit && otherIsDebit) || (currentIsCredit && otherIsCredit)) {
              response.setError(
                  I18n.get(ITranslation.BANK_RECONCILIATION_MOVE_LINES_MUST_BE_DEBIT_VS_CREDIT));
              return;
            }
          }
        }
      }

      Beans.get(MoveLineService.class).setIsSelectedBankReconciliation(moveLine);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
