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
package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.db.repo.MassStockMoveRepository;
import com.axelor.apps.stock.db.repo.PickedProductRepository;
import com.axelor.apps.stock.db.repo.StoredProductRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Objects;

public class PickedProductProcessingRealizeServiceImpl
    implements MassStockMovableProductProcessingRealizeService<PickedProduct> {

  protected final PickedProductRepository pickedProductRepository;
  protected final PickedProductService pickedProductService;
  protected final StoredProductRepository storedProductRepository;

  @Inject
  public PickedProductProcessingRealizeServiceImpl(
      PickedProductRepository pickedProductRepository,
      PickedProductService pickedProductService,
      StoredProductRepository storedProductRepository) {
    this.pickedProductRepository = pickedProductRepository;
    this.pickedProductService = pickedProductService;
    this.storedProductRepository = storedProductRepository;
  }

  @Override
  public void preRealize(PickedProduct movableProduct) {
    // NOTHING
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void postRealize(PickedProduct movableProduct) throws AxelorException {
    Objects.requireNonNull(movableProduct);

    var createdStoredProduct = pickedProductService.createFromPickedProduct(movableProduct);
    var massStockMove = movableProduct.getMassStockMove();
    massStockMove.setStatusSelect(MassStockMoveRepository.STATUS_IN_PROGRESS);

    storedProductRepository.save(createdStoredProduct);
    pickedProductRepository.save(movableProduct);
  }
}
