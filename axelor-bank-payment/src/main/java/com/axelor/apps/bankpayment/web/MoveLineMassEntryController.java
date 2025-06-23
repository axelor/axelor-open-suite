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
package com.axelor.apps.bankpayment.web;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.service.invoice.BankDetailsServiceAccount;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class MoveLineMassEntryController {
  @ErrorException
  public void checkPartnerBankDetails(ActionRequest request, ActionResponse response) {
    MoveLineMassEntry moveLineMassEntry = request.getContext().asType(MoveLineMassEntry.class);
    Move move = request.getContext().getParent().asType(Move.class);
    BankDetails movePartnerBankDetails =
        Beans.get(BankDetailsServiceAccount.class)
            .getDefaultBankDetails(
                moveLineMassEntry.getPartner(), move.getCompany(), move.getPaymentMode());
    response.setValue("movePartnerBankDetails", movePartnerBankDetails);
  }
}
