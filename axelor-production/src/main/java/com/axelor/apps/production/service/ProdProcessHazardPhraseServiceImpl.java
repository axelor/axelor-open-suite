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

import com.axelor.apps.base.db.HazardPhrase;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.repo.ProdProcessRepository;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class ProdProcessHazardPhraseServiceImpl implements ProdProcessHazardPhraseService {

  protected final ProdProcessRepository prodProcessRepository;

  @Inject
  public ProdProcessHazardPhraseServiceImpl(ProdProcessRepository prodProcessRepository) {
    this.prodProcessRepository = prodProcessRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void computeHazardPhrasesAndSave(ProdProcess prodProcess, List<Product> allLineProducts) {
    computeHazardPhrases(prodProcess, allLineProducts);
    prodProcessRepository.save(prodProcess);
  }

  @Override
  public void computeHazardPhrases(ProdProcess prodProcess, List<Product> allLineProducts) {
    Objects.requireNonNull(prodProcess, "prodProcess cannot be null");

    Set<HazardPhrase> hazardPhrases = new HashSet<>();

    addHazardPhrasesFromProduct(prodProcess.getProduct(), hazardPhrases);

    if (allLineProducts != null) {
      allLineProducts.forEach(p -> addHazardPhrasesFromProduct(p, hazardPhrases));
    }

    prodProcess.clearHazardPhraseSet();
    hazardPhrases.forEach(prodProcess::addHazardPhraseSetItem);
  }

  protected void addHazardPhrasesFromProduct(Product product, Set<HazardPhrase> hazardPhrases) {
    if (product == null || product.getId() == null) {
      return;
    }
    if (CollectionUtils.isNotEmpty(product.getHazardPhraseSet())) {
      hazardPhrases.addAll(product.getHazardPhraseSet());
    }
  }
}
