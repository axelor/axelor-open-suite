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
package com.axelor.apps.production.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BillOfMaterialHazardPhraseServiceImpl implements BillOfMaterialHazardPhraseService {

  protected final BillOfMaterialRepository billOfMaterialRepository;

  @Inject
  public BillOfMaterialHazardPhraseServiceImpl(BillOfMaterialRepository billOfMaterialRepository) {
    this.billOfMaterialRepository = billOfMaterialRepository;
  }

  @Override
  public List<Product> getLineProductsForProdProcess(ProdProcess prodProcess) {
    if (prodProcess == null || prodProcess.getId() == null) {
      return List.of();
    }
    return billOfMaterialRepository
        .all()
        .filter("self.prodProcess = :prodProcess")
        .bind("prodProcess", prodProcess)
        .fetch()
        .stream()
        .flatMap(b -> b.getBillOfMaterialLineList().stream())
        .map(BillOfMaterialLine::getProduct)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }
}
