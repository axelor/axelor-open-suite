/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.ProductMergeResult;
import com.axelor.apps.supplychain.service.ProductMergeService;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.db.Wizard;
import jakarta.inject.Singleton;
import java.util.Map;

@Singleton
public class ProductMergeController {

  public void openWizard(ActionRequest request, ActionResponse response) {
    try {
      Beans.get(ProductMergeService.class).checkAuthorization(AuthUtils.getUser());
      response.setView(
          ActionView.define(I18n.get("Product merge"))
              .model(Wizard.class.getName())
              .add("form", "product-merge-wizard-form")
              .param("popup", "reload")
              .param("show-toolbar", "false")
              .param("show-confirm", "false")
              .param("popup-save", "false")
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void merge(ActionRequest request, ActionResponse response) {
    try {
      Product absorbedProduct = getProduct(request, "absorbedProduct");
      Product keptProduct = getProduct(request, "keptProduct");
      ProductMergeResult result =
          Beans.get(ProductMergeService.class).merge(absorbedProduct, keptProduct);

      StringBuilder message =
          new StringBuilder(
              String.format(
                  I18n.get(SupplychainExceptionMessage.PRODUCT_MERGE_SUCCESS),
                  absorbedProduct.getFullName(),
                  keptProduct.getFullName()));
      result.getLogLines().forEach(logLine -> message.append("\n").append(logLine));
      response.setInfo(message.toString());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  protected Product getProduct(ActionRequest request, String contextField) throws AxelorException {
    Map<String, Object> productMap = (Map<String, Object>) request.getContext().get(contextField);
    if (productMap == null || productMap.get("id") == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(SupplychainExceptionMessage.PRODUCT_MERGE_MISSING_PRODUCT));
    }
    return Beans.get(ProductRepository.class).find(Long.valueOf(productMap.get("id").toString()));
  }
}
