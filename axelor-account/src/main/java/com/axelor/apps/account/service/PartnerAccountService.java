/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.base.db.Partner;

public class PartnerAccountService {

  public String getDefaultSpecificTaxNote(Partner partner) {
    FiscalPosition fiscalPosition = partner.getFiscalPosition();

    if (fiscalPosition == null || !fiscalPosition.getCustomerSpecificNote()) {
      return "";
    }

    return fiscalPosition.getCustomerSpecificNoteText();
  }

  public boolean isRegistrationCodeRequired(Partner partner) {
    boolean hasFrenchAddress =
        partner.getPartnerAddressList() != null
            && partner.getPartnerAddressList().stream()
                    .filter(
                        partnerAddress ->
                            partnerAddress.getIsInvoicingAddr()
                                && partnerAddress.getAddress() != null
                                && partnerAddress.getAddress().getAddressL7Country() != null
                                && partnerAddress.getAddress().getAddressL7Country().getAlpha2Code()
                                    != null
                                && partnerAddress
                                    .getAddress()
                                    .getAddressL7Country()
                                    .getAlpha2Code()
                                    .equals("FR"))
                    .count()
                > 0;
    boolean hasFrenchCompany =
        partner.getCompanySet() != null
            && partner.getCompanySet().stream()
                    .filter(
                        company ->
                            company.getAddress() != null
                                && company.getAddress().getAddressL7Country() != null
                                && company.getAddress().getAddressL7Country().getAlpha2Code()
                                    != null
                                && company
                                    .getAddress()
                                    .getAddressL7Country()
                                    .getAlpha2Code()
                                    .equals("FR"))
                    .count()
                > 0;
    return hasFrenchCompany && hasFrenchAddress;
  }
}
