/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.csv.script;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.service.fixedasset.FixedAssetGenerationService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.util.Map;

public class ImportFixedAsset {

  @Inject FixedAssetGenerationService fixedAssetGenerationService;

  public Object importFixedAsset(Object bean, Map<String, Object> values) throws AxelorException {
    assert bean instanceof FixedAsset;
    FixedAsset fixedAsset = (FixedAsset) bean;
    if (fixedAsset != null && fixedAsset.getOriginSelect() == null) {
      fixedAsset.setOriginSelect(FixedAssetRepository.ORIGINAL_SELECT_IMPORT);
    }
    fixedAssetGenerationService.generateAndComputeLines(fixedAsset);
    return fixedAsset;
  }
}
