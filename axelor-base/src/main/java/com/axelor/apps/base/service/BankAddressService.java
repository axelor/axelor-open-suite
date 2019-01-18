/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.BankAddress;

public class BankAddressService {

  public String computeFullAddress(String label, String address, String code) {
    // fullAddress = label address (code)
    String fullAddress = "";
    fullAddress = label;
    if (fullAddress == null || fullAddress.equals("")) {
      fullAddress = "";
    } else {
      fullAddress += " ";
    }
    fullAddress += address;

    if (code == null || code.equals("")) {
      return fullAddress;
    }

    return fullAddress + " ( " + code + " )";
  }

  public String computeFullAddress(BankAddress bankAddress) {
    return this.computeFullAddress(
        bankAddress.getLabel(), bankAddress.getAddress(), bankAddress.getCode());
  }
}
