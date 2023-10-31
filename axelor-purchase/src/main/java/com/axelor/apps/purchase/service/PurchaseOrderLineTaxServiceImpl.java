package com.axelor.apps.purchase.service;

import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.service.tax.OrderLineTaxService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.PurchaseOrderLineTax;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurchaseOrderLineTaxServiceImpl implements PurchaseOrderLineTaxService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected OrderLineTaxService orderLineTaxService;

  @Inject
  public PurchaseOrderLineTaxServiceImpl(OrderLineTaxService orderLineTaxService) {
    this.orderLineTaxService = orderLineTaxService;
  }

  /**
   * Créer les lignes de TVA de la commande. La création des lignes de TVA se basent sur les lignes
   * de commande.
   *
   * @param purchaseOrder La commande.
   * @param purchaseOrderLineList Les lignes de commandes.
   * @return La liste des lignes de TVA de la commande.
   */
  @Override
  public List<PurchaseOrderLineTax> createsPurchaseOrderLineTax(
      PurchaseOrder purchaseOrder, List<PurchaseOrderLine> purchaseOrderLineList) {

    List<PurchaseOrderLineTax> purchaseOrderLineTaxList = new ArrayList<>();
    Map<TaxLine, PurchaseOrderLineTax> map = new HashMap<>();
    Set<String> specificNotes = new HashSet<>();
    boolean customerSpecificNote = orderLineTaxService.isCustomerSpecificNote(purchaseOrder);

    if (CollectionUtils.isNotEmpty(purchaseOrderLineList)) {
      LOG.debug("Creation of tax lines for purchase order lines.");
      for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList) {
        getOrCreateLines(
            purchaseOrder, purchaseOrderLine, map, customerSpecificNote, specificNotes);
      }
    }

    computeAndAddTaxToList(map, purchaseOrderLineTaxList);
    orderLineTaxService.setSpecificNotes(
        customerSpecificNote,
        purchaseOrder,
        specificNotes,
        purchaseOrder.getSupplierPartner().getSpecificTaxNote());

    return purchaseOrderLineTaxList;
  }

  protected void getOrCreateLines(
      PurchaseOrder purchaseOrder,
      PurchaseOrderLine purchaseOrderLine,
      Map<TaxLine, PurchaseOrderLineTax> map,
      boolean customerSpecificNote,
      Set<String> specificNotes) {
    TaxLine taxLine = purchaseOrderLine.getTaxLine();
    TaxEquiv taxEquiv = purchaseOrderLine.getTaxEquiv();
    TaxLine taxLineRC =
        (taxEquiv != null && taxEquiv.getReverseCharge() && taxEquiv.getReverseChargeTax() != null)
            ? taxEquiv.getReverseChargeTax().getActiveTaxLine()
            : null;

    getOrCreateLine(purchaseOrder, purchaseOrderLine, taxLine, map, false);

    // Reverse charged process
    getOrCreateLine(purchaseOrder, purchaseOrderLine, taxLineRC, map, true);

    orderLineTaxService.addTaxEquivSpecificNote(
        purchaseOrderLine, customerSpecificNote, specificNotes);
  }

  protected void getOrCreateLine(
      PurchaseOrder purchaseOrder,
      PurchaseOrderLine purchaseOrderLine,
      TaxLine taxLine,
      Map<TaxLine, PurchaseOrderLineTax> map,
      boolean reverseCharged) {
    if (taxLine != null) {
      LOG.debug("VAT {}", taxLine);
      if (map.containsKey(taxLine)) {
        PurchaseOrderLineTax purchaseOrderLineVat = map.get(taxLine);
        purchaseOrderLineVat.setReverseCharged(reverseCharged);
        purchaseOrderLineVat.setExTaxBase(
            purchaseOrderLineVat.getExTaxBase().add(purchaseOrderLine.getExTaxTotal()));

      } else {
        PurchaseOrderLineTax purchaseOrderLineTax =
            createPurchaseOrderLineTax(purchaseOrder, purchaseOrderLine, taxLine, reverseCharged);
        map.put(taxLine, purchaseOrderLineTax);
      }
    }
  }

  protected PurchaseOrderLineTax createPurchaseOrderLineTax(
      PurchaseOrder purchaseOrder,
      PurchaseOrderLine purchaseOrderLine,
      TaxLine taxLine,
      boolean reverseCharged) {
    PurchaseOrderLineTax purchaseOrderLineTax = new PurchaseOrderLineTax();
    purchaseOrderLineTax.setPurchaseOrder(purchaseOrder);
    purchaseOrderLineTax.setReverseCharged(reverseCharged);
    purchaseOrderLineTax.setExTaxBase(purchaseOrderLine.getExTaxTotal());
    purchaseOrderLineTax.setTaxLine(taxLine);
    return purchaseOrderLineTax;
  }

  protected void computeAndAddTaxToList(
      Map<TaxLine, PurchaseOrderLineTax> map, List<PurchaseOrderLineTax> purchaseOrderLineTaxList) {
    for (PurchaseOrderLineTax purchaseOrderLineTax : map.values()) {
      // Dans la devise de la commande
      orderLineTaxService.computeTax(purchaseOrderLineTax);
      purchaseOrderLineTaxList.add(purchaseOrderLineTax);

      LOG.debug(
          "Tax line : Tax total => {}, Total W.T. => {}",
          purchaseOrderLineTax.getTaxTotal(),
          purchaseOrderLineTax.getInTaxTotal());
    }
  }
}
