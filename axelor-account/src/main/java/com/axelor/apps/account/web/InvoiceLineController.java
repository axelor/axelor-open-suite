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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.FixedAssetCategory;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AccountManagementServiceAccountImpl;
import com.axelor.apps.account.service.AccountService;
import com.axelor.apps.account.service.analytic.AnalyticDistributionTemplateService;
import com.axelor.apps.account.service.analytic.AnalyticLineService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.analytic.AnalyticToolService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceLineAnalyticService;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.translation.ITranslation;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.auth.AuthUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.Strings;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

@Singleton
public class InvoiceLineController {

  private final int startAxisPosition = 1;
  private final int endAxisPosition = 5;

  public void getAndComputeAnalyticDistribution(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Context context = request.getContext();
    InvoiceLine invoiceLine = context.asType(InvoiceLine.class);
    Invoice invoice = this.getInvoice(context);

    response.setValue(
        "analyticMoveLineList",
        Beans.get(InvoiceLineAnalyticService.class)
            .getAndComputeAnalyticDistribution(invoiceLine, invoice));
    response.setValue(
        "analyticDistributionTemplate", invoiceLine.getAnalyticDistributionTemplate());
  }

  public void createAnalyticDistributionWithTemplate(ActionRequest request, ActionResponse response)
      throws AxelorException {
    InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);
    response.setValue(
        "analyticMoveLineList",
        Beans.get(InvoiceLineAnalyticService.class)
            .createAnalyticDistributionWithTemplate(invoiceLine));
  }

  public void computeAnalyticDistribution(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Context context = request.getContext();
    InvoiceLine invoiceLine = context.asType(InvoiceLine.class);

    if (Beans.get(AppAccountService.class).getAppAccount().getManageAnalyticAccounting()) {
      response.setValue(
          "analyticMoveLineList",
          Beans.get(InvoiceLineAnalyticService.class).computeAnalyticDistribution(invoiceLine));
    }
  }

  public void compute(ActionRequest request, ActionResponse response) throws AxelorException {

    Context context = request.getContext();

    InvoiceLine invoiceLine = context.asType(InvoiceLine.class);

    Invoice invoice = this.getInvoice(context);

    if (invoice == null
        || invoiceLine.getPrice() == null
        || invoiceLine.getInTaxPrice() == null
        || invoiceLine.getQty() == null) {
      return;
    }

    try {
      Beans.get(InvoiceLineService.class).compute(invoice, invoiceLine);

      response.setValues(invoiceLine);
      response.setAttr(
          "priceDiscounted",
          "hidden",
          invoiceLine
                  .getPriceDiscounted()
                  .compareTo(
                      invoice.getInAti() ? invoiceLine.getInTaxPrice() : invoiceLine.getPrice())
              == 0);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void getProductInformation(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Context context = request.getContext();
    InvoiceLine invoiceLine = context.asType(InvoiceLine.class);
    Invoice invoice = this.getInvoice(context);
    Product product = invoiceLine.getProduct();
    Map<String, Object> productInformation = new HashMap<>();
    if (invoice != null && product != null) {
      try {
        productInformation =
            Beans.get(InvoiceLineService.class).fillProductInformation(invoice, invoiceLine);

        String errorMsg = (String) productInformation.get("error");

        if (!Strings.isNullOrEmpty(errorMsg)) {
          response.setInfo(errorMsg);
        }
      } catch (Exception e) {
        TraceBackService.trace(response, e);
      }
    } else {
      productInformation = Beans.get(InvoiceLineService.class).resetProductInformation(invoice);
    }
    response.setValues(productInformation);
  }

  public void getDiscount(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();

    InvoiceLine invoiceLine = context.asType(InvoiceLine.class);

    Invoice invoice = this.getInvoice(context);

    if (invoice == null || invoiceLine.getProduct() == null) {
      return;
    }

    try {

      Map<String, Object> discounts =
          Beans.get(InvoiceLineService.class)
              .getDiscount(
                  invoice,
                  invoiceLine,
                  invoiceLine.getProduct().getInAti()
                      ? Beans.get(InvoiceLineService.class)
                          .getInTaxUnitPrice(
                              invoice,
                              invoiceLine,
                              invoiceLine.getTaxLine(),
                              InvoiceToolService.isPurchase(invoice))
                      : Beans.get(InvoiceLineService.class)
                          .getExTaxUnitPrice(
                              invoice,
                              invoiceLine,
                              invoiceLine.getTaxLine(),
                              InvoiceToolService.isPurchase(invoice)));
      discounts.remove("price");
      for (Entry<String, Object> entry : discounts.entrySet()) {
        response.setValue(entry.getKey(), entry.getValue());
      }

    } catch (Exception e) {
      response.setInfo(e.getMessage());
    }
  }

  /**
   * Update the ex. tax unit price of an invoice line from its in. tax unit price.
   *
   * @param request
   * @param response
   */
  public void updatePrice(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();
    InvoiceLine invoiceLine = context.asType(InvoiceLine.class);

    try {
      BigDecimal inTaxPrice = invoiceLine.getInTaxPrice();
      TaxLine taxLine = invoiceLine.getTaxLine();

      response.setValue(
          "price",
          Beans.get(TaxService.class)
              .convertUnitPrice(
                  true,
                  taxLine,
                  inTaxPrice,
                  Beans.get(AppBaseService.class).getNbDecimalDigitForUnitPrice()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Update the in. tax unit price of an invoice line from its ex. tax unit price.
   *
   * @param request
   * @param response
   */
  public void updateInTaxPrice(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();

    InvoiceLine invoiceLine = context.asType(InvoiceLine.class);

    try {
      BigDecimal exTaxPrice = invoiceLine.getPrice();
      TaxLine taxLine = invoiceLine.getTaxLine();

      response.setValue(
          "inTaxPrice",
          Beans.get(TaxService.class)
              .convertUnitPrice(
                  false,
                  taxLine,
                  exTaxPrice,
                  Beans.get(AppBaseService.class).getNbDecimalDigitForUnitPrice()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @ErrorException
  public void convertUnitPrice(ActionRequest request, ActionResponse response) {
    InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);

    response.setValue("inTaxPrice", Beans.get(InvoiceLineService.class).getInTaxPrice(invoiceLine));
  }

  public void emptyLine(ActionRequest request, ActionResponse response) {
    InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);

    if (invoiceLine.getTypeSelect() != InvoiceLineRepository.TYPE_NORMAL) {
      Map<String, Object> newInvoiceLine = Mapper.toMap(new InvoiceLine());
      newInvoiceLine.put("qty", BigDecimal.ZERO);
      newInvoiceLine.put("id", invoiceLine.getId());
      newInvoiceLine.put("version", invoiceLine.getVersion());
      newInvoiceLine.put("typeSelect", invoiceLine.getTypeSelect());
      if (invoiceLine.getTypeSelect() == InvoiceLineRepository.TYPE_END_OF_PACK) {
        newInvoiceLine.put("productName", I18n.get(ITranslation.INVOICE_LINE_END_OF_PACK));
      }
      response.setValues(newInvoiceLine);
    }
  }

  public Invoice getInvoice(Context context) {

    Context parentContext = context.getParent();

    Invoice invoice;

    if (parentContext == null
        || !parentContext.getContextClass().toString().equals(Invoice.class.toString())) {

      InvoiceLine invoiceLine = context.asType(InvoiceLine.class);

      invoice = invoiceLine.getInvoice();
    } else {
      invoice = parentContext.asType(Invoice.class);
    }

    return invoice;
  }

  public void getAccount(ActionRequest request, ActionResponse response) {

    try {

      InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);

      if (invoiceLine != null) {
        Product product = invoiceLine.getProduct();
        Invoice invoice = this.getInvoice(request.getContext());

        if (product != null) {
          FiscalPosition fiscalPosition = invoice.getFiscalPosition();

          Account account =
              Beans.get(AccountManagementServiceAccountImpl.class)
                  .getProductAccount(
                      product,
                      invoice.getCompany(),
                      fiscalPosition,
                      InvoiceToolService.isPurchase(invoice),
                      invoiceLine.getFixedAssets());
          response.setValue("account", account);
        }
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void filterAccount(ActionRequest request, ActionResponse response) throws AxelorException {
    Context context = request.getContext();
    Invoice invoice = this.getInvoice(context);
    InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);
    if (invoice != null && invoice.getCompany() != null) {
      List<String> technicalTypeSelectList = new ArrayList<>();
      if (InvoiceToolService.isPurchase(invoice)) {
        if (invoiceLine.getFixedAssets()) {
          technicalTypeSelectList.add(AccountTypeRepository.TYPE_IMMOBILISATION);
        } else {
          technicalTypeSelectList.add(AccountTypeRepository.TYPE_DEBT);
          technicalTypeSelectList.add(AccountTypeRepository.TYPE_CHARGE);
        }
      } else {
        technicalTypeSelectList.add(AccountTypeRepository.TYPE_INCOME);
      }
      String domain =
          "self.company.id = "
              + invoice.getCompany().getId()
              + " AND self.accountType.technicalTypeSelect IN "
              + technicalTypeSelectList.stream().collect(Collectors.joining("','", "('", "')"))
              + " AND self.statusSelect = "
              + AccountRepository.STATUS_ACTIVE;
      response.setAttr("account", "domain", domain);
    }
  }

  public void getFixedAssetCategory(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    InvoiceLine invoiceLine = context.asType(InvoiceLine.class);

    Invoice invoice = this.getInvoice(context);
    Product product = invoiceLine.getProduct();

    if (invoice == null || product == null) {
      return;
    }
    FixedAssetCategory fixedAssetCategory = null;
    if (!product.getAccountManagementList().isEmpty()
        && (invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
            || invoice.getOperationTypeSelect()
                == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND)) {

      Optional<AccountManagement> optionalFixedAssetCategory =
          product.getAccountManagementList().stream()
              .filter(am -> invoice.getCompany().equals(am.getCompany()))
              .findFirst();

      fixedAssetCategory =
          optionalFixedAssetCategory.isPresent()
              ? optionalFixedAssetCategory.get().getFixedAssetCategory()
              : null;
    }
    response.setValue("fixedAssetCategory", fixedAssetCategory);
  }

  public void selectDefaultDistributionTemplate(ActionRequest request, ActionResponse response) {
    try {
      InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);
      InvoiceLineAnalyticService invoiceLineAnalyticService =
          Beans.get(InvoiceLineAnalyticService.class);
      invoiceLine = invoiceLineAnalyticService.selectDefaultDistributionTemplate(invoiceLine);
      response.setValue(
          "analyticDistributionTemplate", invoiceLine.getAnalyticDistributionTemplate());
      response.setValue(
          "analyticMoveLineList",
          invoiceLineAnalyticService.createAnalyticDistributionWithTemplate(invoiceLine));

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void createAnalyticAccountLines(ActionRequest request, ActionResponse response) {
    try {
      InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);
      if (request.getContext().getParent() != null) {
        Invoice invoice = request.getContext().getParent().asType(Invoice.class);
        invoiceLine =
            Beans.get(InvoiceLineAnalyticService.class).analyzeInvoiceLine(invoiceLine, invoice);
        response.setValue("analyticMoveLineList", invoiceLine.getAnalyticMoveLineList());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setAxisDomains(ActionRequest request, ActionResponse response) {
    try {
      InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);
      if (request.getContext().getParent() != null) {
        Invoice invoice = request.getContext().getParent().asType(Invoice.class);
        List<Long> analyticAccountList = new ArrayList<Long>();
        AnalyticToolService analyticToolService = Beans.get(AnalyticToolService.class);
        AnalyticLineService analyticLineService = Beans.get(AnalyticLineService.class);
        for (int i = startAxisPosition; i <= endAxisPosition; i++) {
          if (invoice != null
              && invoice.getCompany() != null
              && analyticToolService.isPositionUnderAnalyticAxisSelect(invoice.getCompany(), i)) {
            analyticAccountList =
                analyticLineService.getAxisDomains(invoiceLine, invoice.getCompany(), i);
            if (CollectionUtils.isEmpty(analyticAccountList)) {
              response.setAttr(
                  "axis".concat(Integer.toString(i)).concat("AnalyticAccount"),
                  "domain",
                  "self.id IN (0)");
            } else {
              String idList =
                  analyticAccountList.stream()
                      .map(id -> id.toString())
                      .collect(Collectors.joining(","));
              response.setAttr(
                  "axis".concat(Integer.toString(i)).concat("AnalyticAccount"),
                  "domain",
                  "self.id IN (" + idList + ")");
            }
          }
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setRequiredAnalyticAccount(ActionRequest request, ActionResponse response) {
    try {
      InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);
      if (request.getContext().getParent() != null) {
        Invoice invoice = request.getContext().getParent().asType(Invoice.class);
        AnalyticLineService analyticLineService = Beans.get(AnalyticLineService.class);
        for (int i = startAxisPosition; i <= endAxisPosition; i++) {
          response.setAttr(
              "axis".concat(Integer.toString(i)).concat("AnalyticAccount"),
              "required",
              analyticLineService.isAxisRequired(invoiceLine, invoice.getCompany(), i));
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void clearAnalytic(ActionRequest request, ActionResponse response) {
    try {
      InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);
      if (invoiceLine != null) {
        Beans.get(InvoiceLineAnalyticService.class).clearAnalyticAccounting(invoiceLine);
        response.setValues(invoiceLine);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkAnalyticByTemplate(ActionRequest request, ActionResponse response) {
    try {
      InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);
      if (invoiceLine != null && invoiceLine.getAnalyticDistributionTemplate() != null) {
        AnalyticMoveLineService analyticMoveLineService = Beans.get(AnalyticMoveLineService.class);
        analyticMoveLineService.validateLines(
            invoiceLine.getAnalyticDistributionTemplate().getAnalyticDistributionLineList());
        if (!analyticMoveLineService.validateAnalyticMoveLines(
            invoiceLine.getAnalyticMoveLineList())) {
          response.setError(I18n.get(AccountExceptionMessage.INVALID_ANALYTIC_MOVE_LINE));
        }
        Beans.get(AnalyticDistributionTemplateService.class)
            .validateTemplatePercentages(invoiceLine.getAnalyticDistributionTemplate());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void printAnalyticAccounts(ActionRequest request, ActionResponse response) {
    try {
      InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);
      if (request.getContext().getParent() != null) {
        Invoice invoice = request.getContext().getParent().asType(Invoice.class);
        if (invoiceLine != null && invoice != null) {
          Beans.get(AnalyticLineService.class)
              .printAnalyticAccount(invoiceLine, invoice.getCompany());
          response.setValues(invoiceLine);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void translateProductDescriptionAndName(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      InvoiceLineService invoiceLineService = Beans.get(InvoiceLineService.class);
      InvoiceLine invoiceLine = context.asType(InvoiceLine.class);
      Invoice parent = this.getInvoice(context);
      String userLanguage = AuthUtils.getUser().getLanguage();

      Map<String, String> translation =
          invoiceLineService.getProductDescriptionAndNameTranslation(
              parent, invoiceLine, userLanguage);

      String description = translation.get("description");
      String productName = translation.get("productName");

      if (description != null
          && !description.isEmpty()
          && productName != null
          && !productName.isEmpty()) {
        response.setValue("description", description);
        response.setValue("productName", productName);
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkAnalyticMoveLineForAxis(ActionRequest request, ActionResponse response) {
    try {
      InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);
      if (invoiceLine != null) {
        Beans.get(AnalyticLineService.class).checkAnalyticLineForAxis(invoiceLine);
        response.setValues(invoiceLine);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void manageInvoiceLineAxis(ActionRequest request, ActionResponse response) {
    try {
      Context parentContext = request.getContext().getParent();
      if (parentContext != null && parentContext.getContextClass().equals(Invoice.class)) {
        Invoice invoice = request.getContext().getParent().asType(Invoice.class);
        if (invoice.getCompany() != null) {
          AccountConfig accountConfig =
              Beans.get(AccountConfigService.class).getAccountConfig(invoice.getCompany());
          if (accountConfig != null) {
            AnalyticAxis analyticAxis = null;
            for (int i = startAxisPosition; i <= endAxisPosition; i++) {
              response.setAttr(
                  "axis".concat(Integer.toString(i)).concat("AnalyticAccount"),
                  "hidden",
                  !(i <= accountConfig.getNbrOfAnalyticAxisSelect()));
              for (AnalyticAxisByCompany analyticAxisByCompany :
                  accountConfig.getAnalyticAxisByCompanyList()) {
                if (analyticAxisByCompany.getSequence() + 1 == i) {
                  analyticAxis = analyticAxisByCompany.getAnalyticAxis();
                }
              }
              if (analyticAxis != null) {
                response.setAttr(
                    "axis".concat(Integer.toString(i)).concat("AnalyticAccount"),
                    "title",
                    analyticAxis.getName());
                analyticAxis = null;
              }
            }
          }
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkAnalyticAccount(ActionRequest request, ActionResponse response) {
    try {
      AccountService accountService = Beans.get(AccountService.class);

      if (Invoice.class.equals(request.getContext().getContextClass())) {
        Invoice invoice = request.getContext().asType(Invoice.class);
        if (invoice != null && CollectionUtils.isNotEmpty(invoice.getInvoiceLineList())) {
          for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
            if (invoiceLine != null && invoiceLine.getAccount() != null) {
              accountService.checkAnalyticAxis(
                  invoiceLine.getAccount(),
                  invoiceLine.getAnalyticDistributionTemplate(),
                  false,
                  invoiceLine.getAccount().getAnalyticDistributionRequiredOnInvoiceLines());
            }
          }
        }
      } else {
        InvoiceLine invoiceLine = request.getContext().asType(InvoiceLine.class);
        if (invoiceLine != null && invoiceLine.getAccount() != null) {
          accountService.checkAnalyticAxis(
              invoiceLine.getAccount(),
              invoiceLine.getAnalyticDistributionTemplate(),
              false,
              invoiceLine.getAccount().getAnalyticDistributionRequiredOnInvoiceLines());
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
