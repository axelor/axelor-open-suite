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
