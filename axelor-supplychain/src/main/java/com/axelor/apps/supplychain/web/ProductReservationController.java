package com.axelor.apps.supplychain.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.supplychain.db.ProductReservation;
import com.axelor.apps.supplychain.db.repo.AbstractProductReservationRepository;
import com.axelor.apps.supplychain.db.repo.ProductReservationRepository;
import com.axelor.apps.supplychain.service.ProductReservationService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

public class ProductReservationController {
  public static final String RESPONSE_ATTRIBUTE_PRODUCT_RESERVATION_REQUESTED_RESERVED_LIST =
      "$productReservationRequestedReservedList";
  public static final String RESPONSE_ATTRIBUTE_PRODUCT_RESERVATION_RESERVED_LIST =
      "$productReservationReservedList";
  public static final String RESPONSE_ATTRIBUTE_NAME_VALUE = "value";
  public static final String RESPONSE_FIELD_NAME_RESERVATION_TYPE = "ReservationType";
  public static final String RESPONSE_FIELD_NAME_ORIGIN_SALE_ORDER_LINE = "originSaleOrderLine";
  public static final String RESPONSE_FIELD_NAME_ORIGIN_MANUF_ORDER = "originManufOrder";
  public static final String RESPONSE_FIELD_NAME_STATUS = "status";
  public static final String RESPONSE_FIELD_NAME_AVAILABLE_QTY = "$availableQty";
  public static final String RESPONSE_FIELD_NAME_PRODUCT = "product";
  public static final String REQUEST_FIELD_NAME_PRODUCT_RESERVATION_REQUESTED_RESERVED_LIST =
      "productReservationRequestedReservedList";
  public static final String REQUEST_FIELD_NAME_PRODUCT_RESERVATION_RESERVED_LIST =
      "productReservationReservedList";

  public static final Map<Integer, String>
      MAP_RESPONSE_FIELD_NAME_PRODUCT_RESERVATION_LIST_BY_PRODUCT_RESERVATION_TYPE =
          Map.of(
              AbstractProductReservationRepository.TYPE_PRODUCT_RESERVATION_RESERVATION,
                  RESPONSE_ATTRIBUTE_PRODUCT_RESERVATION_REQUESTED_RESERVED_LIST,
              AbstractProductReservationRepository.TYPE_PRODUCT_RESERVATION_ALLOCATION,
                  RESPONSE_ATTRIBUTE_PRODUCT_RESERVATION_RESERVED_LIST);
  public static final String REQUEST_FIELDNAME_MODEL = "_model";

  @Inject public ProductReservationService productReservationService;

  // ACTION from FORM VIEW PRODUCT RESERVATION

  public void updateStatus(ActionRequest request, ActionResponse response) {
    try {
      ProductReservation productionReservationProxy =
          request.getContext().asType(ProductReservation.class);
      ProductReservation productReservation =
          Beans.get(ProductReservationRepository.class).find(productionReservationProxy.getId());
      productReservationService.updateStatus(productReservation, true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  // ACTION from FORM VIEW SALE ORDER LINE ON LOAD

  /**
   * Populate HTTP Response fields
   *
   * <ul>
   *   <li>{@value #RESPONSE_ATTRIBUTE_PRODUCT_RESERVATION_REQUESTED_RESERVED_LIST}
   *   <li>{@value #RESPONSE_ATTRIBUTE_PRODUCT_RESERVATION_RESERVED_LIST}
   * </ul>
   */
  public void getProductReservationListsOfSaleOrderLineByType(
      ActionRequest request, ActionResponse response) {
    try {
      /*
            SaleOrderLine saleOrderLineProxy = request.getContext().asType(SaleOrderLine.class);
            fillResponseWithProductReservationRequestedReservedList(response, saleOrderLineProxy);
            fillResponseWithProductReservationReservedList(response, saleOrderLineProxy);
      */
      sendResponseTwoProductReservationListOnLoad(request, response);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void getProductReservationListsOfManufOrderByType(
      ActionRequest request, ActionResponse response) {
    try {
      sendResponseTwoProductReservationListOnLoad(request, response);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  protected void sendResponseTwoProductReservationListOnLoad(
      ActionRequest request, ActionResponse response) {
    String model = request.getRawContext().get(REQUEST_FIELDNAME_MODEL).toString();
    Long originId = Long.valueOf(request.getRawContext().get("id").toString());
    Long productId = productReservationService.getProductIdFromMap(request.getRawContext());

    sendResponseTwoProductReservationListOnLoad(
        response,
        AbstractProductReservationRepository.TYPE_PRODUCT_RESERVATION_RESERVATION,
        productId,
        model,
        originId);
    sendResponseTwoProductReservationListOnLoad(
        response,
        AbstractProductReservationRepository.TYPE_PRODUCT_RESERVATION_ALLOCATION,
        productId,
        model,
        originId);
  }

  protected void sendResponseTwoProductReservationListOnLoad(
      ActionResponse response,
      int productReservationType,
      Long productId,
      String originModelClassName,
      Long originId) {

    // find list
    List<ProductReservation> productReservationList =
        productReservationService.findProductReservation(
            productReservationType, productId, originModelClassName, originId);

    // put in response
    response.setAttr(
        MAP_RESPONSE_FIELD_NAME_PRODUCT_RESERVATION_LIST_BY_PRODUCT_RESERVATION_TYPE.get(
            productReservationType),
        RESPONSE_ATTRIBUTE_NAME_VALUE,
        productReservationList);
  }

  // CLICK new Button

  public void onNewManufOrderProductReservationRequestedReserved(
      ActionRequest request, ActionResponse response) {
    try {
      sendResponseProductReservationOnNew(
          request,
          response,
          AbstractProductReservationRepository.TYPE_PRODUCT_RESERVATION_RESERVATION);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void onNewManufOrderProductReservationReserved(
      ActionRequest request, ActionResponse response) {
    try {
      sendResponseProductReservationOnNew(
          request,
          response,
          AbstractProductReservationRepository.TYPE_PRODUCT_RESERVATION_ALLOCATION);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  // ACTION FROM SALEORDERLINE VIEW : NEW PRODUCT RESERVATION

  public void onNewSaleOrderLineProductReservationRequestedReserved(
      ActionRequest request, ActionResponse response) {
    try {
      sendResponseProductReservationOnNew(
          request,
          response,
          AbstractProductReservationRepository.TYPE_PRODUCT_RESERVATION_RESERVATION);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void onNewSaleOrderLineProductReservationReserved(
      ActionRequest request, ActionResponse response) throws AxelorException {
    try {
      sendResponseProductReservationOnNew(
          request,
          response,
          AbstractProductReservationRepository.TYPE_PRODUCT_RESERVATION_ALLOCATION);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  protected void sendResponseProductReservationOnNew(
      ActionRequest request, ActionResponse response, int typeProductReservation)
      throws AxelorException {

    @SuppressWarnings("unchecked")
    Map<String, Object> mapParent =
        (Map<String, Object>) request.getRawContext().get("_parent"); // saleOrderLine

    // origin
    String originModelClassName = mapParent.get(REQUEST_FIELDNAME_MODEL).toString();
    Long originId = Long.valueOf(mapParent.get("id").toString());

    // product
    Long productId = productReservationService.getProductIdFromMap(mapParent);

    // entities from HttpRequest
    ProductReservation newProductReservation =
        request.getContext().asType(ProductReservation.class);

    // enriched the entity ProductReservation
    productReservationService.enrichProductReservationOnNew(
        newProductReservation, productId, originModelClassName, originId, typeProductReservation);

    // manage response
    fillResponseWithProductReservationOnNew(response, newProductReservation);
  }

  /*  protected void fillResponseWithSaleOrderLineProductReservationOnNew(
      ActionRequest request, ActionResponse response, int typeProductReservationReservation)
      throws AxelorException {

    // check Origin
    @SuppressWarnings("unchecked")
    Map<String, Object> mapParent =
        (Map<String, Object>) request.getRawContext().get("_parent"); // saleOrderLine
    if (!SaleOrderLine.class.getName().equals(mapParent.get(REQUEST_FIELDNAME_MODEL).toString())) {
      return;
    }

    // entities from HttpRequest
    ProductReservation newProductReservation =
        request.getContext().asType(ProductReservation.class);
    Class<SaleOrderLine> type = SaleOrderLine.class;
    SaleOrderLine proxySaleOrderLine = request.getContext().getParent().asType(type);

    // enriched the entity ProductReservation
    Long productId = productReservationService.getProductIdFromMap(mapParent);
    productReservationService.createSaleOrderLineProductReservation(
        newProductReservation, productId, proxySaleOrderLine, typeProductReservationReservation);

    // manage response
    fillResponseWithProductReservationOnNew(response, newProductReservation);
  }*/

  protected void fillResponseWithProductReservationOnNew(
      ActionResponse response, ProductReservation newProductReservation) throws AxelorException {
    response.setAttr(
        RESPONSE_FIELD_NAME_RESERVATION_TYPE,
        RESPONSE_ATTRIBUTE_NAME_VALUE,
        newProductReservation.getProductReservationType());
    response.setAttr(
        RESPONSE_FIELD_NAME_PRODUCT,
        RESPONSE_ATTRIBUTE_NAME_VALUE,
        newProductReservation.getProduct());
    response.setAttr(
        RESPONSE_FIELD_NAME_ORIGIN_SALE_ORDER_LINE,
        RESPONSE_ATTRIBUTE_NAME_VALUE,
        newProductReservation.getOriginSaleOrderLine());
    try {
      // TODO push in prodservprod
      Method getterManufOrder =
          ProductReservation.class.getMethod(
              "getOriginManufOrder"); // reflection because supplychain do not know ManufOrder class
      response.setAttr(
          RESPONSE_FIELD_NAME_ORIGIN_MANUF_ORDER,
          RESPONSE_ATTRIBUTE_NAME_VALUE,
          getterManufOrder.invoke(newProductReservation));
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      TraceBackService.trace(e); // never happen
    }
    response.setAttr(
        RESPONSE_FIELD_NAME_STATUS,
        RESPONSE_ATTRIBUTE_NAME_VALUE,
        newProductReservation.getStatus());
    BigDecimal availableQty = productReservationService.getAvailableQty(newProductReservation);
    response.setAttr(
        RESPONSE_FIELD_NAME_AVAILABLE_QTY, RESPONSE_ATTRIBUTE_NAME_VALUE, availableQty);
  }

  // ON CHANGE (confirm --> save)

  public void onChangeSaleOrderLineProductReservationRequestedReserved(
      ActionRequest request, ActionResponse response) {
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> changedProductReservationRequestedReservedMapList =
        (List<Map<String, Object>>)
            request
                .getRawContext()
                .get(REQUEST_FIELD_NAME_PRODUCT_RESERVATION_REQUESTED_RESERVED_LIST);
    productReservationService.saveSelectedProductReservationInMapList(
        changedProductReservationRequestedReservedMapList,
        AbstractProductReservationRepository.TYPE_PRODUCT_RESERVATION_RESERVATION);
  }

  public void onChangeSaleOrderLineProductReservationReserved(
      ActionRequest request, ActionResponse response) {
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> changedProductReservationReservedMapList =
        (List<Map<String, Object>>)
            request.getRawContext().get(REQUEST_FIELD_NAME_PRODUCT_RESERVATION_RESERVED_LIST);
    productReservationService.saveSelectedProductReservationInMapList(
        changedProductReservationReservedMapList,
        AbstractProductReservationRepository.TYPE_PRODUCT_RESERVATION_ALLOCATION);
  }

  public void onChangeManufOrderProductReservationRequestedReserved(
      ActionRequest request, ActionResponse response) {
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> changedProductReservationRequestedReservedMapList =
        (List<Map<String, Object>>)
            request
                .getRawContext()
                .get(REQUEST_FIELD_NAME_PRODUCT_RESERVATION_REQUESTED_RESERVED_LIST);
    productReservationService.saveSelectedProductReservationInMapList(
        changedProductReservationRequestedReservedMapList,
        AbstractProductReservationRepository.TYPE_PRODUCT_RESERVATION_RESERVATION);
  }

  public void onChangeManufOrderProductReservationReserved(
      ActionRequest request, ActionResponse response) {
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> changedProductReservationReservedMapList =
        (List<Map<String, Object>>)
            request.getRawContext().get(REQUEST_FIELD_NAME_PRODUCT_RESERVATION_RESERVED_LIST);
    productReservationService.saveSelectedProductReservationInMapList(
        changedProductReservationReservedMapList,
        AbstractProductReservationRepository.TYPE_PRODUCT_RESERVATION_ALLOCATION);
  }
}
