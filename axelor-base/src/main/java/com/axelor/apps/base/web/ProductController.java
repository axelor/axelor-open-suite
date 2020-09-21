/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.web;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCompany;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.report.IReport;
import com.axelor.apps.base.service.ProductService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.Joiner;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ProductController {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void generateProductVariants(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Product product = request.getContext().asType(Product.class);
    product = Beans.get(ProductRepository.class).find(product.getId());

    if (product.getProductVariantConfig() != null) {
      Beans.get(ProductService.class).generateProductVariants(product);

      response.setFlash(I18n.get(IExceptionMessage.PRODUCT_1));
      response.setReload(true);
    }
  }

  public void updateProductsPrices(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Product product = request.getContext().asType(Product.class);

    product = Beans.get(ProductRepository.class).find(product.getId());

    Beans.get(ProductService.class).updateProductPrice(product);

    response.setFlash(I18n.get(IExceptionMessage.PRODUCT_2));
    response.setReload(true);
  }

  @SuppressWarnings("unchecked")
  public void printProductCatalog(ActionRequest request, ActionResponse response)
      throws AxelorException {

    User user = Beans.get(UserService.class).getUser();

    int currentYear = Beans.get(AppBaseService.class).getTodayDateTime().getYear();
    String productIds = "";

    List<Integer> lstSelectedProduct = (List<Integer>) request.getContext().get("_ids");

    if (lstSelectedProduct != null) {
      productIds = Joiner.on(",").join(lstSelectedProduct);
    }

    String name = I18n.get("Product Catalog");

    String fileLink =
        ReportFactory.createReport(IReport.PRODUCT_CATALOG, name + "-${date}")
            .addParam("UserId", user.getId())
            .addParam("CurrYear", Integer.toString(currentYear))
            .addParam("ProductIds", productIds)
            .addParam("Locale", ReportSettings.getPrintingLocale(null))
            .addParam(
                "Timezone",
                user.getActiveCompany() != null ? user.getActiveCompany().getTimezone() : null)
            .generate()
            .getFileLink();

    logger.debug("Printing " + name);

    response.setView(ActionView.define(name).add("html", fileLink).map());
  }

  public void printProductSheet(ActionRequest request, ActionResponse response)
      throws AxelorException {
    try {
      Product product = request.getContext().asType(Product.class);
      User user = Beans.get(UserService.class).getUser();

      String name = I18n.get("Product") + " " + product.getCode();

      if (user.getActiveCompany() == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.PRODUCT_NO_ACTIVE_COMPANY));
      }

      String fileLink =
          ReportFactory.createReport(IReport.PRODUCT_SHEET, name + "-${date}")
              .addParam("ProductId", product.getId())
              .addParam("CompanyId", user.getActiveCompany().getId())
              .addParam("Locale", ReportSettings.getPrintingLocale(null))
              .addParam(
                  "Timezone",
                  user.getActiveCompany() != null ? user.getActiveCompany().getTimezone() : null)
              .generate()
              .getFileLink();

      logger.debug("Printing " + name);

      response.setView(ActionView.define(name).add("html", fileLink).map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void addProductCompany(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();

    if (context.getParent() != null) {
      Context parentContext = context.getParent();
      List<ProductCompany> productCompanieList = new ArrayList<>();
      CompanyRepository companyRepository = Beans.get(CompanyRepository.class);

      if (parentContext.get("company") != null) {
        Company company = companyRepository.find(((Company) parentContext.get("company")).getId());
        if (company != null) {
          ProductCompany productCompany = new ProductCompany();
          productCompany.setCompany(company);
          productCompanieList.add(productCompany);
        }

      } else if (parentContext.get("companySet") != null) {
        Set<Company> companyList = (Set<Company>) parentContext.get("companySet");
        if (companyList != null) {

          for (Company company : companyList) {
            Company parentCompany = companyRepository.find(company.getId());
            ProductCompany productCompany = new ProductCompany();
            productCompany.setCompany(parentCompany);
            productCompanieList.add(productCompany);
          }
        }
      }

      response.setValue("productCompanyList", productCompanieList);
    }
  }

  public void setProductCompanyData(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();

    if (context.getParent() != null) {

      Product product = context.asType(Product.class);
      List<ProductCompany> productCompanyList = product.getProductCompanyList();

      if (productCompanyList != null) {

        for (ProductCompany productCompany : productCompanyList) {
          if (productCompany.getSalePrice().compareTo(BigDecimal.ZERO) == 0) {
            productCompany.setSalePrice(product.getSalePrice());
          }

          if (productCompany.getSaleCurrency() == null) {
            productCompany.setSaleCurrency(product.getSaleCurrency());
          }

          if (productCompany.getPurchasePrice().compareTo(BigDecimal.ZERO) == 0) {
            productCompany.setPurchasePrice(product.getPurchasePrice());
          }

          if (productCompany.getPurchaseCurrency() == null) {
            productCompany.setPurchaseCurrency(product.getPurchaseCurrency());
          }

          if (productCompany.getCostPrice().compareTo(BigDecimal.ZERO) == 0) {
            productCompany.setCostPrice(product.getCostPrice());
          }

          if (productCompany.getManagPriceCoef().compareTo(BigDecimal.ZERO) == 0) {
            productCompany.setManagPriceCoef(product.getManagPriceCoef());
          }
        }

        response.setValue("productCompanyList", product.getProductCompanyList());
      }
    }
  }
}
