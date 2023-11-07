package com.axelor.apps.purchase.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.rpc.Context;
import java.util.List;

public interface PurchaseOrderMergingService {

  interface CommonFields {
    Company getCommonCompany();

    void setCommonCompany(Company commonCompany);

    Currency getCommonCurrency();

    void setCommonCurrency(Currency commonCurrency);

    Partner getCommonSupplierPartner();

    void setCommonSupplierPartner(Partner commonSupplierPartner);

    Partner getCommonContactPartner();

    void setCommonContactPartner(Partner commonContactPartner);

    PriceList getCommonPriceList();

    void setCommonPriceList(PriceList commonPriceList);

    TradingName getCommonTradingName();

    void setCommonTradingName(TradingName commonTradingName);

    boolean getAllTradingNamesAreNull();

    void setAllTradingNamesAreNull(boolean allTradingNamesAreNull);
  }

  interface Checks {
    boolean isExistCurrencyDiff();

    void setExistCurrencyDiff(boolean existCurrencyDiff);

    boolean isExistCompanyDiff();

    void setExistCompanyDiff(boolean existCompanyDiff);

    boolean isExistSupplierPartnerDiff();

    void setExistSupplierPartnerDiff(boolean existSupplierPartnerDiff);

    boolean isExistContactPartnerDiff();

    void setExistContactPartnerDiff(boolean existContactPartnerDiff);

    boolean isExistPriceListDiff();

    void setExistPriceListDiff(boolean existPriceListDiff);

    boolean isExistTradingNameDiff();

    void setExistTradingNameDiff(boolean existTradingNameDiff);
  }

  interface PurchaseOrderMergingResult {
    void setPurchaseOrder(PurchaseOrder purchaseOrder);

    PurchaseOrder getPurchaseOrder();

    void needConfirmation();

    boolean isConfirmationNeeded();
  }

  PurchaseOrderMergingResult create();

  CommonFields getCommonFields(PurchaseOrderMergingResult result);

  Checks getChecks(PurchaseOrderMergingResult result);

  PurchaseOrderMergingResult mergePurchaseOrders(List<PurchaseOrder> purchaseOrdersToMerge)
      throws AxelorException;

  PurchaseOrderMergingResult mergePurchaseOrdersWithContext(
      List<PurchaseOrder> saleOrdersToMerge, Context context) throws AxelorException;
}
