/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.invoice.generator;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.invoice.generator.line.InvoiceLineManagement;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Alarm;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.UnitConversion;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.db.repo.UnitConversionRepository;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Classe de création de ligne de facture abstraite. */
public abstract class InvoiceLineGenerator extends InvoiceLineManagement {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected CurrencyService currencyService;
  protected UnitConversionRepository unitConversionRepo;
  protected AppBaseService appBaseService;
  protected AppAccountService appAccountService;
  protected InvoiceLineService invoiceLineService;
  protected AccountManagementAccountService accountManagementService;
  protected ProductCompanyService productCompanyService;

  protected Invoice invoice;
  protected Product product;
  protected String productName;
  protected BigDecimal price;
  protected BigDecimal inTaxPrice;
  protected BigDecimal priceDiscounted;
  protected String description;
  protected BigDecimal qty;
  protected Unit unit;
  protected TaxLine taxLine;
  protected int sequence;
  protected LocalDate today;
  protected boolean isTaxInvoice;
  protected BigDecimal discountAmount;
  protected int discountTypeSelect;
  protected BigDecimal exTaxTotal;
  protected BigDecimal inTaxTotal;
  protected Integer typeSelect = 0;

  public static final int DEFAULT_SEQUENCE = 0;

  protected InvoiceLineGenerator() {}

  protected InvoiceLineGenerator(Invoice invoice) {

    this.invoice = invoice;
    this.unitConversionRepo = Beans.get(UnitConversionRepository.class);
    this.appBaseService = Beans.get(AppBaseService.class);
    this.appAccountService = Beans.get(AppAccountService.class);
    this.invoiceLineService = Beans.get(InvoiceLineService.class);
    this.accountManagementService = Beans.get(AccountManagementAccountService.class);
    this.productCompanyService = Beans.get(ProductCompanyService.class);
  }

  protected InvoiceLineGenerator(
      Invoice invoice,
      Product product,
      String productName,
      String description,
      BigDecimal qty,
      Unit unit,
      int sequence,
      boolean isTaxInvoice) {

    this(invoice);

    this.product = product;
    this.productName = productName;
    this.description = description;
    this.qty = qty.setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP);
    this.unit = unit;
    this.sequence = sequence;
    this.isTaxInvoice = isTaxInvoice;
    this.today = appAccountService.getTodayDate(invoice.getCompany());
    this.currencyService = new CurrencyService(this.appBaseService, this.today);
  }

  protected InvoiceLineGenerator(
      Invoice invoice,
      Product product,
      String productName,
      BigDecimal price,
      BigDecimal inTaxPrice,
      BigDecimal priceDiscounted,
      String description,
      BigDecimal qty,
      Unit unit,
      TaxLine taxLine,
      int sequence,
      BigDecimal discountAmount,
      int discountTypeSelect,
      BigDecimal exTaxTotal,
      BigDecimal inTaxTotal,
      boolean isTaxInvoice) {

    this(invoice, product, productName, description, qty, unit, sequence, isTaxInvoice);

    this.price = price;
    this.inTaxPrice = inTaxPrice;
    this.priceDiscounted = priceDiscounted;
    this.taxLine = taxLine;
    this.discountTypeSelect = discountTypeSelect;
    this.discountAmount = discountAmount;
    this.exTaxTotal = exTaxTotal;
    this.inTaxTotal = inTaxTotal;
  }

  public Invoice getInvoice() {
    return invoice;
  }

  public void setInvoice(Invoice invoice) {
    this.invoice = invoice;
  }

  @Override
  public abstract List<InvoiceLine> creates() throws AxelorException;

  /**
   * @return
   * @throws AxelorException
   */
  protected InvoiceLine createInvoiceLine() throws AxelorException {

    InvoiceLine invoiceLine = new InvoiceLine();
    boolean isPurchase = InvoiceToolService.isPurchase(invoice);
    Partner partner = invoice.getPartner();
    Company company = invoice.getCompany();

    invoiceLine.setInvoice(invoice);

    invoiceLine.setProduct(product);

    invoiceLine.setProductName(productName);
    if (product != null) {
      invoiceLine.setProductCode((String) productCompanyService.get(product, "code", company));
      Account account =
          accountManagementService.getProductAccount(
              product,
              company,
              invoice.getFiscalPosition(),
              isPurchase,
              invoiceLine.getFixedAssets());
      invoiceLine.setAccount(account);
    }

    invoiceLine.setDescription(description);
    invoiceLine.setPrice(price);
    invoiceLine.setInTaxPrice(inTaxPrice);

    invoiceLine.setPriceDiscounted(priceDiscounted);
    invoiceLine.setQty(qty);
    invoiceLine.setUnit(unit);

    invoiceLine.setTypeSelect(typeSelect);

    if (taxLine == null) {
      this.determineTaxLine();
    }

    if (product != null) {
      TaxEquiv taxEquiv =
          Beans.get(AccountManagementService.class)
              .getProductTaxEquiv(product, company, invoice.getFiscalPosition(), isPurchase);

      invoiceLine.setTaxEquiv(taxEquiv);
    }

    invoiceLine.setTaxLine(taxLine);

    if (taxLine != null) {
      invoiceLine.setTaxRate(taxLine.getValue());
      invoiceLine.setTaxCode(taxLine.getTax().getCode());
    }

    if ((exTaxTotal == null || inTaxTotal == null)) {
      this.computeTotal();
    }

    invoiceLine.setExTaxTotal(exTaxTotal);
    invoiceLine.setInTaxTotal(inTaxTotal);

    this.computeCompanyTotal(invoiceLine);

    invoiceLine.setSequence(sequence);

    invoiceLine.setDiscountTypeSelect(discountTypeSelect);
    invoiceLine.setDiscountAmount(discountAmount);

    return invoiceLine;
  }

  public void determineTaxLine() throws AxelorException {

    if (product != null) {

      Company company = invoice.getCompany();
      Partner partner = invoice.getPartner();
      FiscalPosition fiscalPosition = invoice.getFiscalPosition();

      taxLine =
          accountManagementService.getTaxLine(
              today, product, company, fiscalPosition, InvoiceToolService.isPurchase(invoice));
    }
  }

  public void computeTotal() {

    if (typeSelect == InvoiceLineRepository.TYPE_TITLE) {
      return;
    }

    BigDecimal taxRate = BigDecimal.ZERO;
    if (taxLine != null) {
      taxRate = taxLine.getValue().divide(new BigDecimal(100));
    }

    if (!invoice.getInAti()) {
      exTaxTotal = computeAmount(this.qty, this.priceDiscounted, 2);
      inTaxTotal = exTaxTotal.add(exTaxTotal.multiply(taxRate)).setScale(2, RoundingMode.HALF_UP);
    } else {
      inTaxTotal = computeAmount(this.qty, this.priceDiscounted, 2);
      exTaxTotal = inTaxTotal.divide(taxRate.add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
    }
  }

  public void computeCompanyTotal(InvoiceLine invoiceLine) throws AxelorException {

    if (typeSelect == InvoiceLineRepository.TYPE_TITLE) {
      return;
    }

    Company company = invoice.getCompany();

    Currency companyCurrency = company.getCurrency();

    if (companyCurrency == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.INVOICE_LINE_GENERATOR_2),
          company.getName());
    }

    invoiceLine.setCompanyExTaxTotal(
        currencyService
            .getAmountCurrencyConvertedAtDate(
                invoice.getCurrency(), companyCurrency, exTaxTotal, today)
            .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP));

    invoiceLine.setCompanyInTaxTotal(
        currencyService
            .getAmountCurrencyConvertedAtDate(
                invoice.getCurrency(), companyCurrency, inTaxTotal, today)
            .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP));
  }

  /**
   * Rembourser une ligne de facture.
   *
   * @param invoice La facture concernée.
   * @param invoiceLine La ligne de facture.
   * @return La ligne de facture de remboursement.
   */
  protected InvoiceLine refundInvoiceLine(InvoiceLine invoiceLine, boolean daysQty) {

    LOG.debug("Reimbursement of an invoice line (quantity = number of day ? {}).", daysQty);

    InvoiceLine refundInvoiceLine = JPA.copy(invoiceLine, true);

    refundInvoiceLine.setInvoice(invoice);

    BigDecimal quantity = invoiceLine.getQty();

    refundInvoiceLine.setQty(quantity.negate());

    LOG.debug("Quantity reimbursed : {}", refundInvoiceLine.getQty());

    refundInvoiceLine.setExTaxTotal(
        computeAmount(refundInvoiceLine.getQty(), refundInvoiceLine.getPrice()));

    LOG.debug(
        "Reimbursement of the invoice line {} => amount W.T : {}",
        new Object[] {invoiceLine.getId(), refundInvoiceLine.getExTaxTotal()});

    return refundInvoiceLine;
  }

  protected InvoiceLine substractInvoiceLine(InvoiceLine invoiceLine1, InvoiceLine invoiceLine2) {

    InvoiceLine substract = JPA.copy(invoiceLine1, false);

    substract.setQty(invoiceLine1.getQty().add(invoiceLine2.getQty()));
    substract.setExTaxTotal(computeAmount(substract.getQty(), substract.getPrice()));

    LOG.debug("Subtraction of two invoice lines: {}", substract);

    return substract;
  }

  /**
   * Convertir le prix d'une unité de départ vers une unité d'arrivée.
   *
   * @param price
   * @param startUnit
   * @param endUnit
   * @return Le prix converti
   */
  protected BigDecimal convertPrice(BigDecimal price, Unit startUnit, Unit endUnit) {

    BigDecimal convertPrice = convert(startUnit, endUnit, price);

    LOG.debug(
        "Price conversion {} {} : {} {}", new Object[] {price, startUnit, convertPrice, endUnit});

    return convertPrice;
  }

  /**
   * Récupérer la bonne unité.
   *
   * @param unit Unité de base.
   * @param displayUnit Unité à afficher.
   * @return L'unité à utiliser.
   */
  protected Unit unit(Unit unit, Unit displayUnit) {

    Unit resUnit = unit;

    if (displayUnit != null) {
      resUnit = displayUnit;
    }

    LOG.debug(
        "Get unit : Unit {}, Unit displayed {} : {}", new Object[] {unit, displayUnit, resUnit});

    return resUnit;
  }

  // HELPER

  /**
   * Convertir le prix d'une unité de départ version une unité d'arrivée.
   *
   * @param price
   * @param startUnit
   * @param endUnit
   * @return Le prix converti
   */
  protected BigDecimal convert(Unit startUnit, Unit endUnit, BigDecimal value) {

    if (value == null || startUnit == null || endUnit == null || startUnit.equals(endUnit)) {
      return value;
    } else {
      return value.multiply(convertCoef(startUnit, endUnit)).setScale(6, RoundingMode.HALF_UP);
    }
  }

  /**
   * Obtenir le coefficient de conversion d'une unité de départ vers une unité d'arrivée.
   *
   * @param startUnit
   * @param endUnit
   * @return Le coefficient de conversion.
   */
  protected BigDecimal convertCoef(Unit startUnit, Unit endUnit) {

    UnitConversion unitConversion =
        unitConversionRepo
            .all()
            .filter("self.startUnit = ?1 AND self.endUnit = ?2", startUnit, endUnit)
            .fetchOne();

    if (unitConversion != null) {
      return unitConversion.getCoef();
    } else {
      return BigDecimal.ONE;
    }
  }

  protected void addAlarm(Alarm alarm, Partner partner) {

    if (alarm != null) {

      alarm.setInvoice(invoice);
      alarm.setPartner(partner);
    }
  }
}
