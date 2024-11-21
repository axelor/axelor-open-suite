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
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.Bank;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.repo.BankRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.BankDetailsServiceImpl;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import org.iban4j.IbanFormatException;
import org.iban4j.InvalidCheckDigitException;
import org.iban4j.UnsupportedCountryException;

@Singleton
public class BankDetailsController {

  public void validateIban(ActionRequest request, ActionResponse response) {
    response.setAttr("invalidIbanText", "hidden", true);

    if (request.getAction().endsWith("onnew")) {
      return;
    }

    BankDetails bankDetails = request.getContext().asType(BankDetails.class);
    Bank bank = bankDetails.getBank();

    if (bankDetails.getIban() != null
        && bank != null
        && bank.getBankDetailsTypeSelect() == BankRepository.BANK_IDENTIFIER_TYPE_IBAN) {
      try {
        Beans.get(BankDetailsService.class).validateIban(bankDetails.getIban());
      } catch (IbanFormatException | InvalidCheckDigitException | UnsupportedCountryException e) {
        if (request.getAction().endsWith("onchange")) {
          response.setInfo(I18n.get(BaseExceptionMessage.BANK_DETAILS_1));
        }
        response.setAttr("invalidIbanText", "hidden", false);
      } finally {
        bankDetails = Beans.get(BankDetailsServiceImpl.class).detailsIban(bankDetails);
        if (bank.getCountry() != null && bank.getCountry().getAlpha2Code().equals("FR")) {
          response.setValue("bankCode", bankDetails.getBankCode());
          response.setValue("sortCode", bankDetails.getSortCode());
          response.setValue("accountNbr", bankDetails.getAccountNbr());
          response.setValue("bbanKey", bankDetails.getBbanKey());
        }
      }
    }
  }
}
