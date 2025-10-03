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
package com.axelor.apps.bankpayment.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.service.move.attributes.MoveAttrsService;
import com.axelor.apps.account.service.move.record.MoveGroupOnChangeServiceImpl;
import com.axelor.apps.account.service.move.record.MoveRecordSetService;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsBankPaymentService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.google.inject.Inject;
import java.util.Map;

public class MoveGroupOnChangeServiceBankPaymentImpl extends MoveGroupOnChangeServiceImpl {

  protected BankDetailsBankPaymentService bankDetailsBankPaymentService;

  @Inject
  public MoveGroupOnChangeServiceBankPaymentImpl(
      MoveRecordSetService moveRecordSetService,
      MoveAttrsService moveAttrsService,
      BankDetailsBankPaymentService bankDetailsBankPaymentService) {
    super(moveRecordSetService, moveAttrsService);
    this.bankDetailsBankPaymentService = bankDetailsBankPaymentService;
  }

  @Override
  public Map<String, Object> getPaymentModeOnChangeValuesMap(Move move) throws AxelorException {
    Map<String, Object> valuesMap = super.getPaymentModeOnChangeValuesMap(move);
    BankDetails currentPartnerBankDetails = move.getPartnerBankDetails();
    PaymentMode paymentMode = move.getPaymentMode();

    if (bankDetailsBankPaymentService.isBankDetailsNotLinkedToActiveUmr(
        paymentMode, move.getCompany(), currentPartnerBankDetails)) {
      valuesMap.put("partnerBankDetails", null);
    }

    return valuesMap;
  }
}
