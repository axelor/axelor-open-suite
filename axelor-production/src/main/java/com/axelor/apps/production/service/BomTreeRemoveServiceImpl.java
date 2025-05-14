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
package com.axelor.apps.production.service;

import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.db.TempBomTree;
import com.axelor.apps.production.db.repo.TempBomTreeRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class BomTreeRemoveServiceImpl implements BomTreeRemoveService {

  protected TempBomTreeRepository tempBomTreeRepository;

  @Inject
  public BomTreeRemoveServiceImpl(TempBomTreeRepository tempBomTreeRepository) {
    this.tempBomTreeRepository = tempBomTreeRepository;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void removeLinkedBomTrees(BillOfMaterialLine billOfMaterialLine) {
    List<TempBomTree> linkedBomTrees =
        tempBomTreeRepository
            .all()
            .autoFlush(false)
            .filter("self.billOfMaterialLine = :billOfMaterialLine")
            .bind("billOfMaterialLine", billOfMaterialLine)
            .fetch();
    removeBomTrees(linkedBomTrees);
  }

  protected void removeChildBomTrees(TempBomTree parentTree) {
    List<TempBomTree> childBomTrees =
        tempBomTreeRepository
            .all()
            .autoFlush(false)
            .filter("self.parent = :parentTree")
            .bind("parentTree", parentTree)
            .fetch();
    removeBomTrees(childBomTrees);
  }

  protected void removeBomTrees(List<TempBomTree> bomTrees) {
    if (CollectionUtils.isEmpty(bomTrees)) {
      return;
    }
    for (TempBomTree bomTree : bomTrees) {
      removeChildBomTrees(bomTree);
      bomTree.setParent(null);
      bomTree.setParentBom(null);
      tempBomTreeRepository.remove(bomTree);
    }
  }
}
