/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.purchase.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Blocking;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.BlockingRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.ProductConversionService;
import com.axelor.apps.base.service.ProductService;
import com.axelor.apps.base.service.ShippingCoefService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.PurchaseOrderLineTax;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.exception.PurchaseExceptionMessage;
import com.axelor.apps.purchase.service.app.AppPurchaseService;
import com.axelor.apps.purchase.service.config.PurchaseConfigService;
import com.axelor.apps.purchase.service.print.PurchaseOrderPrintService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurchaseOrderServiceImpl implements PurchaseOrderService {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject protected PurchaseOrderLineTaxService purchaseOrderLineVatService;

  @Inject protected SequenceService sequenceService;

  @Inject protected PartnerRepository partnerRepo;

  @Inject protected AppPurchaseService appPurchaseService;

  @Inject protected PurchaseOrderRepository purchaseOrderRepo;

  @Inject protected ProductCompanyService productCompanyService;

  @Inject protected CurrencyService currencyService;

  @Inject protected PurchaseConfigService purchaseConfigService;

  @Inject protected ProductConversionService productConversionService;

  @Inject protected PurchaseOrderPrintService purchaseOrderPrintService;

  @Inject protected PurchaseOrderSequenceService purchaseOrderSequenceService;

  @Inject protected PurchaseOrderLineTaxService purchaseOrderLineTaxService;

  @Override
  public PurchaseOrder _computePurchaseOrderLines(PurchaseOrder purchaseOrder)
      throws AxelorException {

    if (purchaseOrder.getPurchaseOrderLineList() != null) {
      PurchaseOrderLineService purchaseOrderLineService = Beans.get(PurchaseOrderLineService.class);
      for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
        purchaseOrderLine.setExTaxTotal(
            purchaseOrderLineService.computePurchaseOrderLine(purchaseOrderLine));
        purchaseOrderLine.setCompanyExTaxTotal(
            purchaseOrderLineService.getCompanyExTaxTotal(
                purchaseOrderLine.getExTaxTotal(), purchaseOrder));
      }
    }

    return purchaseOrder;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public PurchaseOrder computePurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException {

    this.initPurchaseOrderLineTax(purchaseOrder);

    this._computePurchaseOrderLines(purchaseOrder);

    this._populatePurchaseOrder(purchaseOrder);

    this._computePurchaseOrder(purchaseOrder);

    return purchaseOrder;
  }

  /**
   * Peupler une commande.
   *
   * <p>Cette fonction permet de déterminer les tva d'une commande à partir des lignes de factures
   * passées en paramètres.
   *
   * @param purchaseOrder
   * @throws AxelorException
   */
  @Override
  public void _populatePurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException {
    List<PurchaseOrderLine> purchaseOrderLineList = purchaseOrder.getPurchaseOrderLineList();
    if (purchaseOrderLineList == null) {
      return;
    }

    logger.debug(
        "Populate an invoice => purchase order lines: {} ",
        new Object[] {purchaseOrderLineList.size()});

    // create Tva lines
    purchaseOrder
        .getPurchaseOrderLineTaxList()
        .addAll(
            purchaseOrderLineVatService.createsPurchaseOrderLineTax(
                purchaseOrder, purchaseOrderLineList));
  }

  /**
   * Compute the purchase order total amounts
   *
   * @param purchaseOrder
   * @throws AxelorException
   */
  @Override
  public void _computePurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException {

    purchaseOrder.setExTaxTotal(BigDecimal.ZERO);
    purchaseOrder.setCompanyExTaxTotal(BigDecimal.ZERO);
    purchaseOrder.setTaxTotal(BigDecimal.ZERO);
    purchaseOrder.setInTaxTotal(BigDecimal.ZERO);

    List<PurchaseOrderLine> purchaseOrderLineList = purchaseOrder.getPurchaseOrderLineList();
    List<PurchaseOrderLineTax> purchaseOrderLineTaxList =
        purchaseOrder.getPurchaseOrderLineTaxList();
    if (purchaseOrderLineList != null) {
      for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList) {
        purchaseOrder.setExTaxTotal(
            purchaseOrder.getExTaxTotal().add(purchaseOrderLine.getExTaxTotal()));

        // In the company accounting currency
        purchaseOrder.setCompanyExTaxTotal(
            purchaseOrder.getCompanyExTaxTotal().add(purchaseOrderLine.getCompanyExTaxTotal()));
      }
    }

    if (purchaseOrderLineTaxList != null) {
      for (PurchaseOrderLineTax purchaseOrderLineVat : purchaseOrderLineTaxList) {

        // In the purchase order currency
        purchaseOrder.setTaxTotal(
            purchaseOrder.getTaxTotal().add(purchaseOrderLineVat.getTaxTotal()));
      }
    }

    purchaseOrder.setInTaxTotal(purchaseOrder.getExTaxTotal().add(purchaseOrder.getTaxTotal()));

    logger.debug(
        "Purchase order amounts: W.T. = {}, VAT = {}, A.T.I. = {}",
        purchaseOrder.getExTaxTotal(),
        purchaseOrder.getTaxTotal(),
        purchaseOrder.getInTaxTotal());
  }

  /**
   * Permet de réinitialiser la liste des lignes de TVA
   *
   * @param purchaseOrder Une commande.
   */
  @Override
  public void initPurchaseOrderLineTax(PurchaseOrder purchaseOrder) {

    if (purchaseOrder.getPurchaseOrderLineTaxList() == null) {
      purchaseOrder.setPurchaseOrderLineTaxList(new ArrayList<>());
    } else {
      List<PurchaseOrderLineTax> purchaseOrderLineTaxList =
          purchaseOrderLineTaxService.getUpdatedPurchaseOrderLineTax(purchaseOrder);
      purchaseOrder.getPurchaseOrderLineTaxList().clear();
      purchaseOrder.getPurchaseOrderLineTaxList().addAll(purchaseOrderLineTaxList);
    }
  }

  @Override
  @Transactional
  public Partner validateSupplier(PurchaseOrder purchaseOrder) {

    Partner supplierPartner = partnerRepo.find(purchaseOrder.getSupplierPartner().getId());
    supplierPartner.setIsSupplier(true);

    return partnerRepo.save(supplierPartner);
  }

  @Override
  public void savePurchaseOrderPDFAsAttachment(PurchaseOrder purchaseOrder) throws AxelorException {
    purchaseOrderPrintService.print(
        purchaseOrder,
        purchaseConfigService.getPurchaseOrderPrintTemplate(purchaseOrder.getCompany()),
        true);
  }

  @Override
  public void checkPrintingSettings(PurchaseOrder purchaseOrder) throws AxelorException {
    if (purchaseOrder.getPrintingSettings() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          String.format(
              I18n.get(PurchaseExceptionMessage.PURCHASE_ORDER_MISSING_PRINTING_SETTINGS),
              purchaseOrder.getPurchaseOrderSeq()));
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void requestPurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException {

    if (purchaseOrder.getStatusSelect() == null
        || purchaseOrder.getStatusSelect() != PurchaseOrderRepository.STATUS_DRAFT) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(PurchaseExceptionMessage.PURCHASE_ORDER_REQUEST_WRONG_STATUS));
    }

    purchaseOrder.setStatusSelect(PurchaseOrderRepository.STATUS_REQUESTED);
    Partner partner = purchaseOrder.getSupplierPartner();
    Company company = purchaseOrder.getCompany();
    Blocking blocking =
        Beans.get(BlockingService.class)
            .getBlocking(partner, company, BlockingRepository.PURCHASE_BLOCKING);

    if (blocking != null) {
      String reason =
          blocking.getBlockingReason() != null ? blocking.getBlockingReason().getName() : "";
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(PurchaseExceptionMessage.SUPPLIER_BLOCKED) + " " + reason,
          partner);
    }
    if (purchaseOrder.getVersionNumber() == 1
        && sequenceService.isEmptyOrDraftSequenceNumber(purchaseOrder.getPurchaseOrderSeq())) {
      purchaseOrder.setPurchaseOrderSeq(
          purchaseOrderSequenceService.getSequence(purchaseOrder.getCompany(), purchaseOrder));
    }
    purchaseOrderRepo.save(purchaseOrder);
    if (appPurchaseService.getAppPurchase().getManagePurchaseOrderVersion()) {
      this.savePurchaseOrderPDFAsAttachment(purchaseOrder);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateCostPrice(PurchaseOrder purchaseOrder) throws AxelorException {
    if (purchaseOrder.getPurchaseOrderLineList() != null) {
      for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
        Product product = purchaseOrderLine.getProduct();
        if (product != null) {
          BigDecimal lastPurchasePrice =
              (Boolean) productCompanyService.get(product, "inAti", purchaseOrder.getCompany())
                  ? purchaseOrderLine.getInTaxPrice()
                  : purchaseOrderLine.getPrice();
          lastPurchasePrice =
              currencyService.getAmountCurrencyConvertedAtDate(
                  purchaseOrder.getCurrency(),
                  purchaseOrder.getCompany().getCurrency(),
                  lastPurchasePrice,
                  appPurchaseService.getTodayDate(purchaseOrder.getCompany()));

          productCompanyService.set(
              product, "lastPurchasePrice", lastPurchasePrice, purchaseOrder.getCompany());

          LocalDate lastPurchaseDate =
              Beans.get(AppBaseService.class)
                  .getTodayDate(
                      Optional.ofNullable(AuthUtils.getUser())
                          .map(User::getActiveCompany)
                          .orElse(null));
          productCompanyService.set(
              product, "lastPurchaseDate", lastPurchaseDate, purchaseOrder.getCompany());

          if ((Boolean)
              productCompanyService.get(
                  product, "defShipCoefByPartner", purchaseOrder.getCompany())) {
            Unit productPurchaseUnit =
                (Unit)
                    productCompanyService.get(product, "purchasesUnit", purchaseOrder.getCompany());
            productPurchaseUnit =
                productPurchaseUnit != null
                    ? productPurchaseUnit
                    : (Unit) productCompanyService.get(product, "unit", purchaseOrder.getCompany());
            BigDecimal convertedQty =
                Beans.get(UnitConversionService.class)
                    .convert(
                        purchaseOrderLine.getUnit(),
                        productPurchaseUnit,
                        purchaseOrderLine.getQty(),
                        purchaseOrderLine.getQty().scale(),
                        product);
            BigDecimal shippingCoef =
                Beans.get(ShippingCoefService.class)
                    .getShippingCoefDefByPartner(
                        product,
                        purchaseOrder.getSupplierPartner(),
                        purchaseOrder.getCompany(),
                        convertedQty);
            if (shippingCoef.compareTo(BigDecimal.ZERO) != 0) {
              productCompanyService.set(
                  product, "shippingCoef", shippingCoef, purchaseOrder.getCompany());
            }
          }
          if ((Integer)
                  productCompanyService.get(product, "costTypeSelect", purchaseOrder.getCompany())
              == ProductRepository.COST_TYPE_LAST_PURCHASE_PRICE) {
            productCompanyService.set(
                product,
                "costPrice",
                productConversionService.convertFromPurchaseToStockUnitPrice(
                    product, lastPurchasePrice),
                purchaseOrder.getCompany());
            if ((Boolean)
                productCompanyService.get(
                    product, "autoUpdateSalePrice", purchaseOrder.getCompany())) {
              Beans.get(ProductService.class).updateSalePrice(product, purchaseOrder.getCompany());
            }
          }
        }
      }
      purchaseOrderRepo.save(purchaseOrder);
    }
  }
}
