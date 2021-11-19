package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import java.util.Map;
import javax.persistence.PersistenceException;

public class AccountAccountManagementRepository extends AccountManagementRepository {

  @Override
  public Map<String, Object> validate(Map<String, Object> json, Map<String, Object> context) {
    boolean alreadyExists =
        all()
                .filter(
                    "self.interbankCodeLine = :interbankCodeLine and self.bankDetails = :bankDetails and self.paymentMode = :paymentMode")
                .bind("interbankCodeLine", json.get("interbankCodeLine"))
                .bind("bankDetails", json.get("bankDetails"))
                .bind("paymentMode", json.get("paymentMode"))
                .count()
            > 0;
    if (alreadyExists) {
      try {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.ACCOUNT_MANAGEMENT_ALREADY_EXISTS),
            null);
      } catch (AxelorException e) {
        throw new PersistenceException(e);
      }
    }
    return super.validate(json, context);
  }
}
