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
package com.axelor.apps.stock.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.db.repo.PickedProductRepository;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductAttrsService;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductCancelService;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductQuantityService;
import com.axelor.apps.stock.service.massstockmove.MassStockMovableProductRealizeService;
import com.axelor.apps.stock.service.massstockmove.PickedProductAttrsService;
import com.axelor.apps.stock.service.massstockmove.PickedProductService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.Optional;

public class PickedProductController {

  public void setFromStockLocationDomain(ActionRequest request, ActionResponse response) {
    var massStockMove = request.getContext().getParent().asType(MassStockMove.class);

    if (massStockMove != null) {
      response.setAttr(
          "fromStockLocation",
          "domain",
          Beans.get(MassStockMovableProductAttrsService.class)
              .getStockLocationDomain(massStockMove));
    }
  }

  public void realizePicking(ActionRequest request, ActionResponse response)
      throws AxelorException {
    var pickedProduct = request.getContext().asType(PickedProduct.class);

    var pickedProductInDB =
        Optional.of(request.getContext().asType(PickedProduct.class))
            .map(pp -> Beans.get(PickedProductRepository.class).find(pp.getId()))
            .map(
                productInDb ->
                    Beans.get(PickedProductService.class).copy(pickedProduct, productInDb))
            .orElse(pickedProduct);

    Beans.get(MassStockMovableProductRealizeService.class).realize(pickedProductInDB);
    response.setReload(true);
  }

  public void cancelPicking(ActionRequest request, ActionResponse response) throws AxelorException {
    var pickedProduct =
        Optional.of(request.getContext().asType(PickedProduct.class))
            .map(pp -> Beans.get(PickedProductRepository.class).find(pp.getId()));

    if (pickedProduct.isPresent()) {
      Beans.get(MassStockMovableProductCancelService.class).cancel(pickedProduct.get());
    }

    response.setReload(true);
  }

  public void setCurrentQty(ActionRequest request, ActionResponse response) throws AxelorException {
    var pickedProduct = request.getContext().asType(PickedProduct.class);

    response.setValue(
        "currentQty",
        Beans.get(MassStockMovableProductQuantityService.class)
            .getCurrentAvailableQty(pickedProduct, pickedProduct.getFromStockLocation()));
  }

  public void setPickedProductDomain(ActionRequest request, ActionResponse response) {
    var pickedProduct = request.getContext().asType(PickedProduct.class);

    if (pickedProduct != null) {
      response.setAttr(
          "pickedProduct",
          "domain",
          Beans.get(PickedProductAttrsService.class).getPickedProductDomain(pickedProduct));
    }
  }

  public void setTrackingNumberDomain(ActionRequest request, ActionResponse response) {
    var pickedProduct = request.getContext().asType(PickedProduct.class);

    if (pickedProduct != null) {
      response.setAttr(
          "trackingNumber",
          "domain",
          Beans.get(PickedProductAttrsService.class).getTrackingNumberDomain(pickedProduct));
    }
  }
}
