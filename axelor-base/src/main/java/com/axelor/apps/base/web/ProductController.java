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
package com.axelor.apps.base.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductService;
import com.axelor.apps.base.service.ProductUpdateService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.birt.template.BirtTemplateService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JpaSecurity;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Criteria;
import com.google.common.base.Joiner;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

      response.setInfo(I18n.get(BaseExceptionMessage.PRODUCT_1));
      response.setReload(true);
    }
  }

  public void checkPriceList(ActionRequest request, ActionResponse response) {
    try {

      Product newProduct = request.getContext().asType(Product.class);

      if ((!newProduct.getSellable() || newProduct.getIsUnrenewed())
          && Beans.get(ProductService.class).hasActivePriceList(newProduct)) {
        response.setAlert(I18n.get("Warning, this product is present in at least one price list"));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setPriceListLineAnomaly(ActionRequest request, ActionResponse response) {
    try {

      Product newProduct = request.getContext().asType(Product.class);
      // Set anomaly when a product exists in list Price
      if (newProduct.getId() != null) {
        Beans.get(PriceListService.class).setPriceListLineAnomaly(newProduct);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateProductsPrices(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Product product = request.getContext().asType(Product.class);

    product = Beans.get(ProductRepository.class).find(product.getId());

    Beans.get(ProductService.class).updateProductPrice(product);

    response.setInfo(I18n.get(BaseExceptionMessage.PRODUCT_2));
    response.setReload(true);
  }

  public void printProductCatalog(ActionRequest request, ActionResponse response)
      throws AxelorException {

    BirtTemplate productCatalogPGQLBirtTemplate =
        Beans.get(AppBaseService.class).getAppBase().getProductCatalogPGQLBirtTemplate();
    if (ObjectUtils.isEmpty(productCatalogPGQLBirtTemplate)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.BIRT_TEMPLATE_CONFIG_NOT_FOUND));
    }

    String name = I18n.get("Product Catalog");
    String fileLink =
        Beans.get(BirtTemplateService.class)
            .generateBirtTemplateLink(
                productCatalogPGQLBirtTemplate,
                null,
                Map.of("ProductIds", getSelectedOrAllRecordIds(request)),
                name + "-${date}",
                false,
                productCatalogPGQLBirtTemplate.getFormat());

    logger.debug("Printing " + name);

    response.setView(ActionView.define(name).add("html", fileLink).map());
  }

  @SuppressWarnings("unchecked")
  private <T extends Model> String getSelectedOrAllRecordIds(ActionRequest request) {
    String recordIds = "";

    List<Integer> idList = (List<Integer>) request.getContext().get("_ids");

    if (idList == null) {
      idList = new ArrayList<>();
      JpaSecurity security = Beans.get(JpaSecurity.class);
      Criteria criteria = Criteria.parse(request);
      Class<T> contextClass = (Class<T>) request.getContext().getContextClass();
      List<?> objects =
          criteria
              .createQuery(contextClass, security.getFilter(JpaSecurity.CAN_READ, contextClass))
              .select("id")
              .fetch(-1, -1);
      for (Object object : objects) {
        if (object instanceof Map) {
          Long id = (Long) ((Map<String, Object>) object).get("id");
          idList.add(Integer.parseInt(id.toString()));
        }
      }
    }
    if (ObjectUtils.notEmpty(idList)) {
      recordIds = Joiner.on(",").join(idList);
    }
    return recordIds;
  }

  public void updateCostPrice(ActionRequest request, ActionResponse response) {
    try {

      Product product = request.getContext().asType(Product.class);
      // Set anomaly when a product exists in list Price
      if (product.getId() != null) {
        Beans.get(ProductUpdateService.class).updateCostPriceFromView(product);
        response.setValue("costPrice", product.getCostPrice());
        response.setValue("productCompanyList", product.getProductCompanyList());
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
