package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import java.util.Map;
import javax.persistence.PersistenceException;

public class AccountManagementAccountRepository extends AccountManagementRepository {

  @Override
  public Map<String, Object> validate(Map<String, Object> json, Map<String, Object> context) {
    boolean alreadyExists =
        all()
                .filter(
                    "self.interbankCodeLine = :interbankCodeLine and self.bankDetails = :bankDetails and self.paymentMode = :paymentMode and (:id = null or self.id != :id)")
                .bind("interbankCodeLine", json.get("interbankCodeLine"))
                .bind("bankDetails", json.get("bankDetails"))
                .bind("paymentMode", json.get("paymentMode"))
                .bind("id", json.get("id"))
                .count()
            > 0;
    if (alreadyExists) {
      try {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.ACCOUNT_MANAGEMENT_ALREADY_EXISTS));
      } catch (AxelorException e) {
        TraceBackService.traceExceptionFromSaveMethod(e);
        throw new PersistenceException(e.getMessage(), e);
      }
    }
    return super.validate(json, context);
  }
}
