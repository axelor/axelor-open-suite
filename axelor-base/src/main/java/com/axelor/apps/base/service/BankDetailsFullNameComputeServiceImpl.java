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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.BankDetails;

public class BankDetailsFullNameComputeServiceImpl implements BankDetailsFullNameComputeService {

  @Override
  public String computeBankDetailsFullName(BankDetails bankDetails) {
    if (bankDetails.getBank() != null) {
      return computeBankDetailsFullName(
          bankDetails.getCode(),
          bankDetails.getLabel(),
          bankDetails.getIban(),
          bankDetails.getBank().getFullName());
    } else {
      return computeBankDetailsFullName(
          bankDetails.getCode(), bankDetails.getLabel(), bankDetails.getIban(), null);
    }
  }

  @Override
  public String computeBankDetailsFullName(
      String code, String label, String iban, String bankFullName) {
    StringBuilder stringBuilder = new StringBuilder();

    if (code != null && !code.isEmpty()) {
      stringBuilder.append(code);
    }

    if (label != null && !label.isEmpty()) {
      if (stringBuilder.toString().isEmpty()) {
        stringBuilder.append(label);
      } else {
        stringBuilder.append(" - ").append(label);
      }
    }

    if (!stringBuilder.toString().isEmpty()) {
      stringBuilder.append(" - ");
    }

    if (iban != null && !iban.isEmpty()) {
      stringBuilder.append(iban);
    }

    if (bankFullName != null && !bankFullName.isEmpty()) {
      stringBuilder.append(" - ").append(bankFullName);
    }

    return stringBuilder.toString();
  }
}
