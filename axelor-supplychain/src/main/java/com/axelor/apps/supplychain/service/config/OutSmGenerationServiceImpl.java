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
package com.axelor.apps.supplychain.service.config;

import com.axelor.apps.supplychain.db.SupplyChainConfig;
import com.axelor.apps.supplychain.db.repo.SupplyChainConfigRepository;

public class OutSmGenerationServiceImpl implements OutSmGenerationService {

  @Override
  public boolean isGeneratedForStorable(SupplyChainConfig config) {
    int select = config.getOutSmGenerationSelect();
    return select == SupplyChainConfigRepository.OUT_SM_GENERATION_STORABLE
        || select == SupplyChainConfigRepository.OUT_SM_GENERATION_ALL;
  }

  @Override
  public boolean isGeneratedForServices(SupplyChainConfig config) {
    int select = config.getOutSmGenerationSelect();
    return select == SupplyChainConfigRepository.OUT_SM_GENERATION_SERVICES
        || select == SupplyChainConfigRepository.OUT_SM_GENERATION_ALL;
  }

  @Override
  public boolean isOnlyForManagedLines(SupplyChainConfig config) {
    return config.getOutSmGenerationSelect()
        == SupplyChainConfigRepository.OUT_SM_GENERATION_MANAGED_LINES;
  }
}
