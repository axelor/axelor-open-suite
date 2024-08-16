package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ProductFamily;
import com.axelor.apps.base.db.repo.ProductFamilyRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.List;
import java.util.Set;
import javax.persistence.PersistenceException;

public class ProductFamilyAccountRepository extends ProductFamilyRepository {

  protected TaxService taxService;

  @Inject
  public ProductFamilyAccountRepository(TaxService taxService) {
    this.taxService = taxService;
  }

  @Override
  public ProductFamily save(ProductFamily productFamily) {
    List<AccountManagement> accountManagementList = productFamily.getAccountManagementList();
    for (AccountManagement accountManagement : accountManagementList) {
      Set<Tax> purchaseTaxSet = accountManagement.getPurchaseTaxSet();

      boolean result = taxService.checkTaxesNotOnlyNonDeductibleTaxes(purchaseTaxSet);
      if (!result) {
        AxelorException axelorException =
            new AxelorException(
                TraceBackRepository.CATEGORY_INCONSISTENCY,
                I18n.get(AccountExceptionMessage.TAX_ONLY_NON_DEDUCTIBLE_TAXES_SELECTED_ERROR1),
                accountManagement.getPurchaseAccount().getLabel());
        TraceBackService.traceExceptionFromSaveMethod(axelorException);
        throw new PersistenceException(axelorException.getMessage(), axelorException);
      }
    }
    return super.save(productFamily);
  }
}
