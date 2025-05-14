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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.ProdProcessLine;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Comparator;
import org.apache.commons.collections.CollectionUtils;

public class ProdProcessLineServiceImpl implements ProdProcessLineService {

  protected WorkCenterService workCenterService;
  protected final ProdProcessLineComputationService prodProcessLineComputationService;

  @Inject
  public ProdProcessLineServiceImpl(
      WorkCenterService workCenterService,
      ProdProcessLineComputationService prodProcessLineComputationService) {
    this.workCenterService = workCenterService;
    this.prodProcessLineComputationService = prodProcessLineComputationService;
  }

  @Override
  public long computeEntireDuration(ProdProcess prodProcess, BigDecimal qty)
      throws AxelorException {
    long totalDuration = 0;
    for (ProdProcessLine prodProcessLine : prodProcess.getProdProcessLineList()) {
      totalDuration +=
          prodProcessLineComputationService.computeEntireCycleDuration(null, prodProcessLine, qty);
    }
    return totalDuration;
  }

  @Override
  public Integer getNextPriority(ProdProcess prodProcess, Integer priority) {
    if (priority == null
        || prodProcess == null
        || CollectionUtils.isEmpty(prodProcess.getProdProcessLineList())) {
      return null;
    }
    return prodProcess.getProdProcessLineList().stream()
        .filter(ppl -> ppl.getPriority() > priority)
        .min(Comparator.comparingInt(ProdProcessLine::getPriority))
        .map(ProdProcessLine::getPriority)
        .orElse(null);
  }
}
