/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.service.FixedAssetService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import javax.persistence.PersistenceException;

public class FixedAssetManagementRepository extends FixedAssetRepository {

  @Override
  public FixedAsset save(FixedAsset fixedAsset) {
    try {
      computeReference(fixedAsset);
      computeDepreciation(fixedAsset);
      return super.save(fixedAsset);
    } catch (Exception e) {
      e.printStackTrace();
      throw new PersistenceException(e);
    }
  }

  private void computeReference(FixedAsset fixedAsset) {
    try {

      if (fixedAsset.getId() != null && Strings.isNullOrEmpty(fixedAsset.getReference())) {
        fixedAsset.setReference(
            Beans.get(SequenceService.class).getDraftSequenceNumber(fixedAsset));
      }
    } catch (Exception e) {
      throw new PersistenceException(e.getLocalizedMessage());
    }
  }

  private void computeDepreciation(FixedAsset fixedAsset) {
    if ((fixedAsset.getFixedAssetLineList() == null || fixedAsset.getFixedAssetLineList().isEmpty())
        && fixedAsset.getGrossValue().compareTo(BigDecimal.ZERO) > 0) {

      Beans.get(FixedAssetService.class).generateAndcomputeLines(fixedAsset);
    }
  }

  @Override
  public FixedAsset copy(FixedAsset entity, boolean deep) {

    FixedAsset copy = super.copy(entity, deep);
    copy.setStatusSelect(STATUS_DRAFT);
    copy.setReference(null);
    copy.setResidualValue(entity.getGrossValue());
    copy.setFixedAssetLineList(null);
    return copy;
  }
}
