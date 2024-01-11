/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.supplychain.db.repo.TimetableRepository;
import com.axelor.apps.supplychain.service.CommonInvoiceService;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.invoice.InvoiceServiceSupplychain;
import com.axelor.apps.supplychain.service.invoice.generator.InvoiceLineGeneratorSupplyChain;
import com.axelor.apps.supplychain.service.invoice.generator.InvoiceLineOrderService;
import com.axelor.inject.Beans;
import com.axelor.studio.db.repo.AppBaseRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PurchaseOrderInvoiceProjectServiceImpl extends PurchaseOrderInvoiceServiceImpl {

  private PriceListService priceListService;

  private PurchaseOrderLineService purchaseOrderLineService;

  protected AppBusinessProjectService appBusinessProjectService;

  protected ProductCompanyService productCompanyService;

  @Inject
  public PurchaseOrderInvoiceProjectServiceImpl(
      InvoiceServiceSupplychain invoiceServiceSupplychain,
      InvoiceService invoiceService,
      InvoiceRepository invoiceRepo,
      TimetableRepository timetableRepo,
      AppSupplychainService appSupplychainService,
      AccountConfigService accountConfigService,
      CommonInvoiceService commonInvoiceService,
      AddressService addressService,
      InvoiceLineOrderService invoiceLineOrderService,
      PriceListService priceListService,
      PurchaseOrderLineService purchaseOrderLineService,
      AppBusinessProjectService appBusinessProjectService,
      ProductCompanyService productCompanyService) {
    super(
        invoiceServiceSupplychain,
        invoiceService,
        invoiceRepo,
        timetableRepo,
        appSupplychainService,
        accountConfigService,
        commonInvoiceService,
        addressService,
        invoiceLineOrderService);
    this.priceListService = priceListService;
    this.purchaseOrderLineService = purchaseOrderLineService;
    this.appBusinessProjectService = appBusinessProjectService;
    this.productCompanyService = productCompanyService;
  }

  @Override
  public void processPurchaseOrderLine(
      Invoice invoice, List<InvoiceLine> invoiceLineList, PurchaseOrderLine purchaseOrderLine)
      throws AxelorException {
    super.processPurchaseOrderLine(invoice, invoiceLineList, purchaseOrderLine);

    if (appBusinessProjectService.isApp("business-project")) {
      invoiceLineList.get(invoiceLineList.size() - 1).setProject(purchaseOrderLine.getProject());
    }
  }

  @Override
  public List<InvoiceLine> createInvoiceLine(Invoice invoice, PurchaseOrderLine purchaseOrderLine)
      throws AxelorException {

    Product product = purchaseOrderLine.getProduct();
    Company company =
        purchaseOrderLine.getPurchaseOrder() != null
            ? purchaseOrderLine.getPurchaseOrder().getCompany()
            : null;
    BigDecimal price;

    if (product != null) {
      price = (BigDecimal) productCompanyService.get(product, "costPrice", company);
    } else {
      price = purchaseOrderLine.getPrice();
    }

    BigDecimal discountAmount = price;
    int discountTypeSelect = 1;
    if (invoice.getPartner().getChargeBackPurchaseSelect()
        == PartnerRepository.CHARGING_BACK_TYPE_PRICE_LIST) {
      PriceList priceList =
          Beans.get(PartnerPriceListService.class)
              .getDefaultPriceList(invoice.getPartner(), PriceListRepository.TYPE_SALE);
      if (priceList != null) {
        PriceListLine priceListLine =
            purchaseOrderLineService.getPriceListLine(purchaseOrderLine, priceList, price);
        if (priceListLine != null) {
          discountTypeSelect = priceListLine.getTypeSelect();
        }
        if ((appBusinessProjectService.getAppBase().getComputeMethodDiscountSelect()
                    == AppBaseRepository.INCLUDE_DISCOUNT_REPLACE_ONLY
                && discountTypeSelect == PriceListLineRepository.TYPE_REPLACE)
            || appBusinessProjectService.getAppBase().getComputeMethodDiscountSelect()
                == AppBaseRepository.INCLUDE_DISCOUNT) {
          Map<String, Object> discounts =
              priceListService.getDiscounts(priceList, priceListLine, price);
          if (discounts != null) {
            discountAmount = (BigDecimal) discounts.get("discountAmount");
            price =
                priceListService.computeDiscount(
                    price, (int) discounts.get("discountTypeSelect"), discountAmount);
          }

        } else {
          Map<String, Object> discounts =
              priceListService.getDiscounts(priceList, priceListLine, price);
          if (discounts != null) {
            discountAmount = (BigDecimal) discounts.get("discountAmount");
            if (discounts.get("price") != null) {
              price = (BigDecimal) discounts.get("price");
            }
          }
        }
      }

      InvoiceLineGenerator invoiceLineGenerator =
          new InvoiceLineGenerator(
              invoice,
              product,
              (String) productCompanyService.get(product, "name", company),
              price,
              price,
              price,
              purchaseOrderLine.getDescription(),
              purchaseOrderLine.getQty(),
              purchaseOrderLine.getUnit(),
              null,
              InvoiceLineGenerator.DEFAULT_SEQUENCE,
              discountAmount,
              discountTypeSelect,
              null,
              null,
              false) {
            @Override
            public List<InvoiceLine> creates() throws AxelorException {
              InvoiceLine invoiceLine = this.createInvoiceLine();
              invoiceLine.setProject(purchaseOrderLine.getProject());

              List<InvoiceLine> invoiceLines = new ArrayList<InvoiceLine>();
              invoiceLines.add(invoiceLine);

              return invoiceLines;
            }
          };
      return invoiceLineGenerator.creates();
    } else if (invoice.getPartner().getChargeBackPurchaseSelect()
        == PartnerRepository.CHARGING_BACK_TYPE_PERCENTAGE) {
      price =
          price
              .multiply(
                  invoice
                      .getPartner()
                      .getChargeBackPurchase()
                      .divide(
                          new BigDecimal(100),
                          appBusinessProjectService.getNbDecimalDigitForUnitPrice(),
                          BigDecimal.ROUND_HALF_UP))
              .setScale(
                  appBusinessProjectService.getNbDecimalDigitForUnitPrice(),
                  BigDecimal.ROUND_HALF_UP);
      InvoiceLineGenerator invoiceLineGenerator =
          new InvoiceLineGenerator(
              invoice,
              product,
              (String) productCompanyService.get(product, "name", company),
              price,
              price,
              price,
              purchaseOrderLine.getDescription(),
              purchaseOrderLine.getQty(),
              purchaseOrderLine.getUnit(),
              null,
              InvoiceLineGenerator.DEFAULT_SEQUENCE,
              discountAmount,
              discountTypeSelect,
              null,
              null,
              false) {
            @Override
            public List<InvoiceLine> creates() throws AxelorException {

              InvoiceLine invoiceLine = this.createInvoiceLine();
              invoiceLine.setProject(purchaseOrderLine.getProject());

              List<InvoiceLine> invoiceLines = new ArrayList<InvoiceLine>();
              invoiceLines.add(invoiceLine);

              return invoiceLines;
            }
          };
      return invoiceLineGenerator.creates();
    } else {
      InvoiceLineGeneratorSupplyChain invoiceLineGenerator =
          new InvoiceLineGeneratorSupplyChain(
              invoice,
              product,
              purchaseOrderLine.getProductName(),
              purchaseOrderLine.getDescription(),
              purchaseOrderLine.getQty(),
              purchaseOrderLine.getUnit(),
              purchaseOrderLine.getSequence(),
              false,
              null,
              purchaseOrderLine,
              null) {
            @Override
            public List<InvoiceLine> creates() throws AxelorException {

              InvoiceLine invoiceLine = this.createInvoiceLine();
              invoiceLine.setProject(purchaseOrderLine.getProject());

              List<InvoiceLine> invoiceLines = new ArrayList<InvoiceLine>();
              invoiceLines.add(invoiceLine);

              return invoiceLines;
            }
          };
      return invoiceLineGenerator.creates();
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Invoice generateInvoice(PurchaseOrder purchaseOrder) throws AxelorException {

    Invoice invoice = super.generateInvoice(purchaseOrder);
    if (purchaseOrder.getProject() != null) {
      invoice.setProject(purchaseOrder.getProject());
    }
    invoice = Beans.get(InvoiceRepository.class).save(invoice);
    return invoice;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Invoice generateSupplierAdvancePayment(
      PurchaseOrder purchaseOrder, BigDecimal amountToInvoice, boolean isPercent)
      throws AxelorException {
    Invoice invoice =
        super.generateSupplierAdvancePayment(purchaseOrder, amountToInvoice, isPercent);
    invoice.setProject(purchaseOrder.getProject());

    return invoiceRepo.save(invoice);
  }

  @Transactional(rollbackOn = {Exception.class})
  public Invoice mergeInvoice(
      List<Invoice> invoiceList,
      Company company,
      Currency currency,
      Partner partner,
      Partner contactPartner,
      PriceList priceList,
      PaymentMode paymentMode,
      PaymentCondition paymentCondition,
      TradingName tradingName,
      FiscalPosition fiscalPosition,
      String supplierInvoiceNb,
      LocalDate originDate,
      PurchaseOrder purchaseOrder,
      Project project)
      throws AxelorException {
    Invoice invoiceMerged =
        super.mergeInvoice(
            invoiceList,
            company,
            currency,
            partner,
            contactPartner,
            priceList,
            paymentMode,
            paymentCondition,
            tradingName,
            fiscalPosition,
            supplierInvoiceNb,
            originDate,
            purchaseOrder);
    if (project != null
        && !appBusinessProjectService.getAppBusinessProject().getProjectInvoiceLines()) {
      invoiceMerged.setProject(project);
      for (InvoiceLine invoiceLine : invoiceMerged.getInvoiceLineList()) {
        invoiceLine.setProject(project);
      }
    }
    return invoiceMerged;
  }
}
