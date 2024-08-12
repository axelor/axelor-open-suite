package com.axelor.apps.account.web;

import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.Set;

public class TaxEquivController {
  public void checkTaxesNotOnlyNonDeductibleTaxes(ActionRequest request, ActionResponse response)
      throws AxelorException {
    String sourceFieldName = (String) request.getContext().get("_source");
    TaxService taxService = Beans.get(TaxService.class);
    TaxEquiv taxEquiv = request.getContext().asType(TaxEquiv.class);
    Set<Tax> taxes;
    if ("fromTaxSet".equals(sourceFieldName)) {
      taxes = taxEquiv.getFromTaxSet();
    } else if ("toTaxSet".equals(sourceFieldName)) {
      taxes = taxEquiv.getToTaxSet();
    } else {
      // "reverseChargeTaxSet"
      taxes = taxEquiv.getReverseChargeTaxSet();
    }
    boolean checkResult = taxService.checkTaxesNotOnlyNonDeductibleTaxes(taxes);
    if (!checkResult) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.TAX_ONLY_NON_DEDUCTIBLE_TAXES_SELECTED_ERROR2));
    }
  }
}
