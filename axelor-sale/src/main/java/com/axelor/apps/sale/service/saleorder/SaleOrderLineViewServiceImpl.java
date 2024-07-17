package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Language;
import com.axelor.apps.base.db.Localization;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.Group;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SaleOrderLineViewServiceImpl implements SaleOrderLineViewService {
  public static final String HIDDEN_ATTR = "hidden";

  @Override
  public Map<String, Map<String, Object>> getOnNewAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    attrs.putAll(hideAti(saleOrder));
    attrs.putAll(hideDifferentLanguageMessage(saleOrder));
    attrs.putAll(hidePriceDiscounted(saleOrder, saleOrderLine));
    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> getOnLoadAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    attrs.putAll(hideAti(saleOrder));
    attrs.putAll(hideFieldsForClient());
    attrs.putAll(hideDifferentLanguageMessage(saleOrder));
    attrs.putAll(hidePriceDiscounted(saleOrder, saleOrderLine));
    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> getProductOnChangeAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    attrs.putAll(hideAti(saleOrder));
    attrs.putAll(hidePriceDiscounted(saleOrder, saleOrderLine));
    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> hidePriceDiscounted(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    BigDecimal priceDiscounted = saleOrderLine.getPriceDiscounted();
    BigDecimal saleOrderLinePrice =
        saleOrder.getInAti() ? saleOrderLine.getInTaxPrice() : saleOrderLine.getPrice();
    attrs.put(
        "priceDiscounted", Map.of(HIDDEN_ATTR, priceDiscounted.compareTo(saleOrderLinePrice) == 0));
    return attrs;
  }

  protected Map<String, Map<String, Object>> hideAti(SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    Boolean inAti = saleOrder.getInAti();
    attrs.put("exTaxTotal", Map.of(HIDDEN_ATTR, inAti));
    attrs.put("inTaxTotal", Map.of(HIDDEN_ATTR, !inAti));
    attrs.put("price", Map.of(HIDDEN_ATTR, inAti));
    attrs.put("inTaxPrice", Map.of(HIDDEN_ATTR, !inAti));
    return attrs;
  }

  protected Map<String, Map<String, Object>> hideFieldsForClient() {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    Group group = AuthUtils.getUser().getGroup();
    boolean isClient = group != null && group.getIsClient();
    attrs.put("marginPanel", Map.of(HIDDEN_ATTR, isClient));
    return attrs;
  }

  protected Map<String, Map<String, Object>> hideDifferentLanguageMessage(SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    Language userLanguage =
        Optional.ofNullable(AuthUtils.getUser().getLocalization())
            .map(Localization::getLanguage)
            .orElse(null);
    Language clientLanguage =
        Optional.ofNullable(saleOrder.getClientPartner())
            .map(Partner::getLocalization)
            .map(Localization::getLanguage)
            .orElse(null);
    if (userLanguage == null || clientLanguage == null) {
      return attrs;
    }
    boolean hideMessage = userLanguage.equals(clientLanguage);
    attrs.put("$differentLanguageMessage", Map.of(HIDDEN_ATTR, hideMessage));
    return attrs;
  }
}
