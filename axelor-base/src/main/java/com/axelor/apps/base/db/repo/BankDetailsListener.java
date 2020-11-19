package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import javax.persistence.PostUpdate;

public class BankDetailsListener {
  @PostUpdate
  private void onPostUpdate(BankDetails bankDetails) throws AxelorException {
    if (bankDetails.getActive()) {
      Company company = bankDetails.getCompany();

      if (company != null) {
        for (BankDetails details : company.getBankDetailsList()) {
          if (!details.getId().equals(bankDetails.getId())
              && details.getIban().equals(bankDetails.getIban())
              && details.getActive()) {
            throw new AxelorException(
                TraceBackRepository.CATEGORY_INCONSISTENCY,
                I18n.get(IExceptionMessage.DUPLICATE_ACTIVE_BANK_DETAILS));
          }
        }
      }
    }
  }
}
