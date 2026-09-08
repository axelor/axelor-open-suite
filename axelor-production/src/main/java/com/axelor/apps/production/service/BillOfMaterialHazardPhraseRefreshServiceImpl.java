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
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.repo.ProdProcessRepository;
import com.axelor.apps.production.record.BomSnapshot;
import com.axelor.db.JPA;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class BillOfMaterialHazardPhraseRefreshServiceImpl
    implements BillOfMaterialHazardPhraseRefreshService {

  protected final ProdProcessRepository prodProcessRepository;
  protected final ProdProcessHazardPhraseService prodProcessHazardPhraseService;

  @Inject
  public BillOfMaterialHazardPhraseRefreshServiceImpl(
      ProdProcessRepository prodProcessRepository,
      ProdProcessHazardPhraseService prodProcessHazardPhraseService) {
    this.prodProcessRepository = prodProcessRepository;
    this.prodProcessHazardPhraseService = prodProcessHazardPhraseService;
  }

  @Override
  public void refreshProdProcessHazardPhrases(Long oldBomId, BillOfMaterial savedBom) {
    BomSnapshot snapshot = oldBomId != null ? readBomSnapshot(oldBomId) : BomSnapshot.empty();
    // Note: readBomSnapshot also returns BomSnapshot.empty() when the BOM has a pre-assigned
    // sequence id but does not yet exist in the committed DB (new entity, first save).

    ProdProcess currentProdProcess = savedBom.getProdProcess();
    Long previousId = snapshot.prodProcessId();
    Long currentId = currentProdProcess != null ? currentProdProcess.getId() : null;

    boolean prodProcessChanged = !Objects.equals(previousId, currentId);

    // Recompute the old ProdProcess so it no longer includes this BOM's contribution.
    if (previousId != null && prodProcessChanged) {
      computeHazardPhrasesForProdProcess(prodProcessRepository.find(previousId));
    }

    // Recompute the current ProdProcess only if something affecting it actually changed.
    if (currentProdProcess != null
        && (prodProcessChanged || hasLineProductsChanged(snapshot, savedBom))) {
      computeHazardPhrasesForProdProcess(currentProdProcess);
    }
  }

  /**
   * Reads the committed DB state for a BOM using a separate EntityManager so that any pending flush
   * in the current transaction does not interfere.
   */
  protected BomSnapshot readBomSnapshot(Long bomId) {
    // Axelor flushes the entity to DB (within the current transaction) before repository.save()
    // is called, via auto-flush triggered by internal queries. Any query on the same EntityManager
    // (even with FlushModeType.COMMIT) therefore reads the already-flushed new state.
    // A separate EntityManager uses its own connection from the pool; with PostgreSQL's default
    // READ COMMITTED isolation it reads the last *committed* state, i.e. the old values.
    EntityManagerFactory emf = JPA.em().getEntityManagerFactory();
    try (EntityManager readEm = emf.createEntityManager()) {
      // getResultList() instead of getSingleResult(): Hibernate assigns the sequence ID before
      // repository.save() is called, so a new BOM may already have a non-null id in memory
      // while not yet existing in the committed DB. In that case the query returns 0 rows.
      List<Long> prodProcessResult =
          readEm
              .createQuery(
                  "SELECT pp.id FROM BillOfMaterial b LEFT JOIN b.prodProcess pp WHERE b.id = :id",
                  Long.class)
              .setParameter("id", bomId)
              .getResultList();
      if (prodProcessResult.isEmpty()) {
        return BomSnapshot.empty();
      }
      Long prodProcessId = prodProcessResult.get(0);
      Set<Long> lineProductIds =
          new HashSet<>(
              readEm
                  .createQuery(
                      "SELECT l.product.id FROM BillOfMaterialLine l"
                          + " WHERE l.billOfMaterial.id = :id AND l.product IS NOT NULL",
                      Long.class)
                  .setParameter("id", bomId)
                  .getResultList());
      return new BomSnapshot(prodProcessId, lineProductIds);
    }
  }

  protected void computeHazardPhrasesForProdProcess(ProdProcess prodProcess) {
    // JPA.all() bypasses the BillOfMaterialRepository binding, avoiding a circular dependency.
    List<Product> allLineProducts =
        JPA
            .all(BillOfMaterial.class)
            .filter("self.prodProcess = :prodProcess")
            .bind("prodProcess", prodProcess)
            .fetch()
            .stream()
            .flatMap(b -> b.getBillOfMaterialLineList().stream())
            .map(BillOfMaterialLine::getProduct)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    prodProcessHazardPhraseService.computeHazardPhrasesAndSave(prodProcess, allLineProducts);
  }

  protected boolean hasLineProductsChanged(BomSnapshot snapshot, BillOfMaterial savedBom) {
    List<BillOfMaterialLine> lines = savedBom.getBillOfMaterialLineList();
    Set<Long> newProductIds =
        lines == null
            ? Set.of()
            : lines.stream()
                .map(BillOfMaterialLine::getProduct)
                .filter(Objects::nonNull)
                .map(Product::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    return !snapshot.lineProductIds().equals(newProductIds);
  }
}
