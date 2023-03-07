package com.axelor.apps.base.service;

import com.axelor.apps.base.db.BankDetails;

public interface BankDetailsFullNameComputeService {
  String computeBankDetailsFullName(BankDetails bankDetails);

  String computeBankDetailsFullName(String code, String label, String iban, String bankFullName);
}
