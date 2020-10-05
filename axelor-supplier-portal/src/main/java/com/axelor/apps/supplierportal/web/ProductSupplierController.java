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
package com.axelor.apps.supplierportal.web;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.supplierportal.db.ProductSupplier;
import com.axelor.apps.supplierportal.db.repo.ProductSupplierRepository;
import com.axelor.apps.supplierportal.service.ProductSupplierService;
import com.axelor.exception.ResponseMessageType;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ProductSupplierController {

  public void addOnCatalog(ActionRequest request, ActionResponse response) {
    ProductSupplier productToAdd = request.getContext().asType(ProductSupplier.class);
    productToAdd = Beans.get(ProductSupplierRepository.class).find(productToAdd.getId());
    try {
      Product newProduct = Beans.get(ProductSupplierService.class).addOnCatalog(productToAdd);
      if (newProduct != null) {
        response.setView(
            ActionView.define(I18n.get("Product"))
                .model(Product.class.getName())
                .add("form", "product-form")
                .param("forceEdit", "true")
                .context("_showRecord", newProduct.getId())
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
