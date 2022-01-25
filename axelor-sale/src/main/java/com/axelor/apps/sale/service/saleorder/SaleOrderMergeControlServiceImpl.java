package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.exception.IExceptionMessage;
import com.axelor.apps.sale.service.saleorder.model.SaleOrderMergeControlResponse;
import com.axelor.apps.sale.service.saleorder.model.SaleOrderMergeObject;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class SaleOrderMergeControlServiceImpl implements SaleOrderMergeControlService {

  @Override
  public SaleOrderMergeControlResponse controlFieldsBeforeMerge(List<SaleOrder> saleOrderList)
      throws AxelorException {

    SaleOrderMergeControlResponse response = new SaleOrderMergeControlResponse();
    Objects.requireNonNull(saleOrderList);

    if (saleOrderList.size() < 1) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY, "Sale order list size is <= 1");
    }

    Map<String, SaleOrderMergeObject> commonMap = new HashMap<>();
    SaleOrder firstSaleOrder = saleOrderList.get(0);
    initCommonMap(commonMap, firstSaleOrder);

    saleOrderList.stream()
        .skip(1)
        .forEach(
            saleOrder -> {
              if (isDiffWithCommonMap(saleOrder, commonMap)) {
                response.setDiff(true);
              }
              ;
            });

    StringBuilder responseMessage = buildMessageFieldsError(commonMap);
    response.setCommonMap(commonMap);
    response.setMessage(responseMessage.toString());

    return response;
  }

  protected StringBuilder buildMessageFieldsError(Map<String, SaleOrderMergeObject> commonMap) {
    StringBuilder sb = new StringBuilder();

    Objects.requireNonNull(commonMap);
    if (commonMap.get("currency") == null
        || commonMap.get("clientPartner") == null
        || commonMap.get("company") == null
        || commonMap.get("fiscalPosition") == null
        || commonMap.get("taxNumber") == null) {
      throw new IllegalStateException(
          "Entry of currency, clientPartner or company, taxNumber or fiscalPosition in map should not be null when calling this function");
    }

    if (commonMap.get("currency").getExistDiff()) {
      sb.append(I18n.get(IExceptionMessage.SALE_ORDER_MERGE_ERROR_CURRENCY) + "<br/>");
    }
    if (commonMap.get("clientPartner").getExistDiff()) {
      sb.append(I18n.get(IExceptionMessage.SALE_ORDER_MERGE_ERROR_CLIENT_PARTNER) + "<br/>");
    }
    if (commonMap.get("company").getExistDiff()) {
      sb.append(I18n.get(IExceptionMessage.SALE_ORDER_MERGE_ERROR_COMPANY) + "<br/>");
    }
    if (commonMap.get("taxNumber").getExistDiff()) {
      sb.append(I18n.get(IExceptionMessage.SALE_ORDER_MERGE_ERROR_TAX_NUMBER) + "<br/>");
    }
    if (commonMap.get("fiscalPosition").getExistDiff()) {
      sb.append(I18n.get(IExceptionMessage.SALE_ORDER_MERGE_ERROR_FISCAL_POSITION) + "<br/>");
    }
    return sb;
  }

  /**
   * This method will compares {@link SaleOrderMergeObject#getCommonObject()} of entry in commonMap
   * with values of saleOrder. If there is a difference, the method will set the commonObject of
   * {@link SaleOrderMergeObject#getCommonObject()} to null and set {@link
   * SaleOrderMergeObject#setExistDiff(boolean)} to true.
   *
   * @param saleOrder
   * @param commonMap
   * @return true if one value is different, else false
   */
  protected boolean isDiffWithCommonMap(
      SaleOrder saleOrder, Map<String, SaleOrderMergeObject> commonMap) {
    Objects.requireNonNull(saleOrder);
    Objects.requireNonNull(commonMap);
    AtomicBoolean isDiff = new AtomicBoolean(false);
    commonMap.entrySet().stream()
        .filter(entry -> entry.getValue() != null)
        .forEach(
            entry -> {
              try {
                Mapper soMapper = Mapper.of(SaleOrder.class);

                SaleOrderMergeObject mapValue = commonMap.get(entry.getKey());
                Object saleOrderValue = soMapper.get(saleOrder, entry.getKey());
                if (mapValue.isDifferent(saleOrderValue)) {
                  mapValue.setCommonObject(null);
                  isDiff.set(true);
                  mapValue.setExistDiff(true);
                }

              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            });

    return isDiff.get();
  }

  protected void initCommonMap(
      Map<String, SaleOrderMergeObject> commonMap, SaleOrder firstSaleOrder) {
    commonMap.put("currency", new SaleOrderMergeObject(firstSaleOrder.getCurrency(), false));
    commonMap.put(
        "clientPartner", new SaleOrderMergeObject(firstSaleOrder.getClientPartner(), false));
    commonMap.put("company", new SaleOrderMergeObject(firstSaleOrder.getCompany(), false));
    commonMap.put("taxNumber", new SaleOrderMergeObject(firstSaleOrder.getTaxNumber(), true));
    commonMap.put(
        "fiscalPosition", new SaleOrderMergeObject(firstSaleOrder.getFiscalPosition(), true));
    commonMap.put("team", new SaleOrderMergeObject(firstSaleOrder.getTeam(), true));
    commonMap.put(
        "contactPartner", new SaleOrderMergeObject(firstSaleOrder.getContactPartner(), true));
    commonMap.put("priceList", new SaleOrderMergeObject(firstSaleOrder.getPriceList(), true));
  }
}
