package com.axelor.apps.purchase.service;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.exception.PurchaseExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PurchaseOrderValidationServiceImpl implements PurchaseOrderValidationService {

  protected TaxService taxService;

  @Inject
  public PurchaseOrderValidationServiceImpl(TaxService taxService) {
    this.taxService = taxService;
  }

  @Override
  public void checkNotOnlyNonDeductibleTaxes(PurchaseOrder purchaseOrder) throws AxelorException {
    List<PurchaseOrderLine> purchaseOrderLineList = purchaseOrder.getPurchaseOrderLineList();
    for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList) {
      Set<TaxLine> taxLineSet = purchaseOrderLine.getTaxLineSet();
      try {
        taxService.checkTaxLinesNotOnlyNonDeductibleTaxes(taxLineSet);
      } catch (AxelorException e) {
        String productFullName =
            Optional.of(purchaseOrderLine)
                .map(PurchaseOrderLine::getProduct)
                .map(Product::getFullName)
                .orElse(null);
        if (productFullName != null) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(
                  PurchaseExceptionMessage
                      .PURCHASE_ORDER_LINE_TAX_ONLY_NON_DEDUCTIBLE_TAXES_SELECTED_ERROR1),
              productFullName);
        } else {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(
                  PurchaseExceptionMessage
                      .PURCHASE_ORDER_LINE_TAX_ONLY_NON_DEDUCTIBLE_TAXES_SELECTED_ERROR2));
        }
      }
    }
  }
}
