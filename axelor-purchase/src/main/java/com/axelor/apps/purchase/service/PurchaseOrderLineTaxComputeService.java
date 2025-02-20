package com.axelor.apps.purchase.service;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.purchase.db.PurchaseOrderLineTax;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PurchaseOrderLineTaxComputeService {

  void computeAndAddTaxToList(
      Map<TaxLine, PurchaseOrderLineTax> map,
      List<PurchaseOrderLineTax> purchaseOrderLineTaxList,
      Currency currency,
      List<PurchaseOrderLineTax> currentPurchaseOrderLineTaxList);

  void computePurchaseOrderLineTax(
      PurchaseOrderLineTax purchaseOrderLineTax,
      Currency currency,
      BigDecimal taxTotal,
      List<PurchaseOrderLineTax> currentPurchaseOrderLineTaxList,
      List<PurchaseOrderLineTax> purchaseOrderLineTaxList);

  BigDecimal computeTaxLineTaxTotal(TaxLine taxLine, PurchaseOrderLineTax purchaseOrderLineTax);
}
