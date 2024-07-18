package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.db.Language;
import com.axelor.apps.base.db.Localization;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SaleOrderLineDummyServiceImpl implements SaleOrderLineDummyService {

  public Map<String, Object> getOnNewDummies(SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    Map<String, Object> dummyFields = new HashMap<>();
    dummyFields.putAll(initPartnerLanguage(saleOrder));
    return dummyFields;
  }

  @Override
  public Map<String, Object> getOnLoadDummies(SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    Map<String, Object> dummyFields = new HashMap<>();
    dummyFields.putAll(initPartnerLanguage(saleOrder));
    return dummyFields;
  }

  protected Map<String, Object> initPartnerLanguage(SaleOrder saleOrder) {
    Map<String, Object> dummyFields = new HashMap<>();
    String languageCode =
        Optional.ofNullable(saleOrder.getClientPartner())
            .map(Partner::getLocalization)
            .map(Localization::getLanguage)
            .map(Language::getCode)
            .orElse("");
    dummyFields.put("$partnerLanguage", languageCode.toUpperCase());
    return dummyFields;
  }
}
