package com.axelor.apps.supplychain.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.db.ProductReservation;
import com.axelor.apps.supplychain.db.repo.AbstractProductReservationRepository;
import com.axelor.apps.supplychain.db.repo.ProductReservationRepository;
import com.axelor.apps.supplychain.service.ProductReservationService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

public class ProductReservationController {
  public static final String COMPUTED_ATTR_PRODUCT_RESERVATION_REQUESTED_RESERVED_LIST =
      "$productReservationRequestedReservedList";
  public static final String COMPUTED_ATTR_PRODUCT_RESERVATION_RESERVED_LIST =
      "$productReservationReservedList";

  @Inject public ProductReservationService productReservationService;

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

  /**
   * Populate HTTP Response fields
   *
   * <ul>
   *   <li>{@value #COMPUTED_ATTR_PRODUCT_RESERVATION_REQUESTED_RESERVED_LIST}
   *   <li>{@value #COMPUTED_ATTR_PRODUCT_RESERVATION_RESERVED_LIST}
   * </ul>
   */
  public void populateProductReservationOfSaleOrderLineByType(
      ActionRequest request, ActionResponse response) {
    try {

      SaleOrderLine saleOrderLineProxy = request.getContext().asType(SaleOrderLine.class);

      populateResponseWithProductReservationRequestedReservedList(response, saleOrderLineProxy);
      populateResponseWithProductReservationReservedList(response, saleOrderLineProxy);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  protected void populateResponseWithProductReservationReservedList(
      ActionResponse response, SaleOrderLine saleOrderLineProxy) {
    List<ProductReservation> productReservationReservedList =
        productReservationService.findProductReservationReservedOfSaleOrderLine(saleOrderLineProxy);
    response.setAttr(
        COMPUTED_ATTR_PRODUCT_RESERVATION_RESERVED_LIST, "value", productReservationReservedList);
  }

  protected void populateResponseWithProductReservationRequestedReservedList(
      ActionResponse response, SaleOrderLine saleOrderLineProxy) {
    List<ProductReservation> productReservationRequestedReservedList =
        productReservationService.findProductReservationRequestedReservedOfSaleOrderLine(
            saleOrderLineProxy);
    response.setAttr(
        COMPUTED_ATTR_PRODUCT_RESERVATION_REQUESTED_RESERVED_LIST,
        "value",
        productReservationRequestedReservedList);
  }

  // ON NEW CHANGE (before save)

  public void onNewSaleOrderLineProductReservationRequestedReserved(
      ActionRequest request, ActionResponse response) throws AxelorException {

    fullFilSaleOrderLineProductReservationRequestedReserved(request, response);
  }

  protected void fullFilSaleOrderLineProductReservationRequestedReserved(ActionRequest request, ActionResponse response) throws AxelorException {

    // entity
    ProductReservation newProductReservation =
        request.getContext().asType(ProductReservation.class);

    // check Origin
    @SuppressWarnings("unchecked")
    Map<String,Object> mapParent = (Map<String,Object>) request.getRawContext().get("_parent");//saleOrderLine
    if (!SaleOrderLine.class.getName().equals(mapParent.get("_model").toString())) {
      return;
    }

    // set type
    newProductReservation.setProductReservationType(
        AbstractProductReservationRepository.TYPE_PRODUCT_RESERVATION_RESERVATION);
    response.setAttr(
        "ReservationType",
        "value",
        AbstractProductReservationRepository.TYPE_PRODUCT_RESERVATION_RESERVATION);

    // set product
    Product product = findProduct(mapParent, newProductReservation);
    newProductReservation.setProduct(product);
    response.setAttr("product", "value", product);

    // set origin
    SaleOrderLine proxySaleOrderLine = request.getContext().getParent().asType(SaleOrderLine.class);
    LinkedHashMap<Object, Object> mapSaleOrderLine = Beans.get(ProductReservationService.class).setMapSaleOrderLine(proxySaleOrderLine, mapParent, newProductReservation);
    response.setAttr("originSaleOrderLine", "value", mapSaleOrderLine);

    // set availableQty
    BigDecimal availableQty =
        Beans.get(ProductReservationService.class).getAvailableQty(newProductReservation);
    response.setAttr("$availableQty", "value", availableQty);

    // set status
    Beans.get(ProductReservationService.class).updateStatus(newProductReservation, false);
    response.setAttr("status", "value", newProductReservation.getStatus());
  }

  protected Product findProduct(Map<String, Object> mapParent, ProductReservation newProductReservation) {
    @SuppressWarnings("unchecked")
    Map<String,Object> mapProduct = (Map<String,Object>) mapParent.get("product");
    Object oId = mapProduct.get("id");
    Long id = Long.valueOf(oId.toString());
    Product product = Beans.get(ProductRepository.class).find(id);
    return product;
  }

  public void onNewSaleOrderLineProductReservationReserved(
      ActionRequest request, ActionResponse response) {
    ProductReservation newProductReservation =
        request.getContext().asType(ProductReservation.class);

    System.out.println("onNewSaleOrderLineProductReservationReserved " + newProductReservation);
    response.setError("Not implemented");
  }

  public void onNewManufOrderProductReservationRequestedReserved(
      ActionRequest request, ActionResponse response) {
    ProductReservation newProductReservation =
        request.getContext().asType(ProductReservation.class);

    System.out.println(
        "onNewManufOrderProductReservationRequestedReserved " + newProductReservation);
    response.setError("Not implemented");
  }

  public void onNewManufOrderProductReservationReserved(
      ActionRequest request, ActionResponse response) {
    ProductReservation newProductReservation =
        request.getContext().asType(ProductReservation.class);

    System.out.println("onNewManufOrderProductReservationReserved " + newProductReservation);
    response.setError("Not implemented");
  }

  // CHANGE (confirm save)

  public void onChangeSaleOrderLineProductReservationRequestedReserved(
      ActionRequest request, ActionResponse response) {
      @SuppressWarnings("unchecked")
      List<Map<String,Object>> rawProductReservationRequestedReservedList =
          (List<Map<String,Object>>) request.getRawContext().get("productReservationRequestedReservedList");
      productReservationService.saveSelectedProductReservation(rawProductReservationRequestedReservedList);
  }

  public void onChangeSaleOrderLineProductReservationReserved(
      ActionRequest request, ActionResponse response) {
    SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
    System.out.println("onChangeSaleOrderLineProductReservationReserved " + saleOrderLine);
    response.setError("Not implemented");
  }

  public void onChangeManufOrderProductReservationRequestedReserved(
      ActionRequest request, ActionResponse response) {

    System.out.println("onChangeManufOrderProductReservationRequestedReserved ");
    response.setError("Not implemented");
  }

  public void onChangeManufOrderProductReservationReserved(
      ActionRequest request, ActionResponse response) {

    System.out.println("onChangeManufOrderProductReservationReserved ");
    response.setError("Not implemented");
  }
}
