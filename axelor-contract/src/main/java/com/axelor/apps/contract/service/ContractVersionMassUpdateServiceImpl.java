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
package com.axelor.apps.contract.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.contract.db.repo.AbstractContractVersionRepository;
import com.axelor.apps.contract.db.repo.ContractVersionRepository;
import com.axelor.db.JPA;
import jakarta.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContractVersionMassUpdateServiceImpl implements ContractVersionMassUpdateService {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final int FETCH_LIMIT = 10;

  protected final ContractVersionRepository contractVersionRepository;
  protected final ContractService contractService;
  protected final AppBaseService appBaseService;

  @Inject
  public ContractVersionMassUpdateServiceImpl(
      ContractVersionRepository contractVersionRepository,
      ContractService contractService,
      AppBaseService appBaseService) {
    this.contractVersionRepository = contractVersionRepository;
    this.contractService = contractService;
    this.appBaseService = appBaseService;
  }

  @Override
  public int massWaiting(List<Long> contractVersionIds) {
    if (contractVersionIds == null || contractVersionIds.isEmpty()) {
      return 0;
    }

    int skippedCount = 0;

    for (int offset = 0; offset < contractVersionIds.size(); offset += FETCH_LIMIT) {
      int end = Math.min(offset + FETCH_LIMIT, contractVersionIds.size());
      List<Long> batch = contractVersionIds.subList(offset, end);

      for (Long contractVersionId : batch) {
        try {
          ContractVersion contractVersion = contractVersionRepository.find(contractVersionId);

          if (contractVersion == null) {
            log.warn("ContractVersion not found for ID: {}", contractVersionId);
            continue;
          }

          if (contractVersion.getStatusSelect()
              != AbstractContractVersionRepository.DRAFT_VERSION) {
            skippedCount++;
            continue;
          }

          LocalDate todayDate =
              appBaseService.getTodayDate(
                  contractVersion.getNextContract() != null
                      ? contractVersion.getNextContract().getCompany()
                      : null);

          contractService.waitingNextVersion(contractVersion.getNextContract(), todayDate);
        } catch (AxelorException e) {
          log.error(
              "Error transitioning ContractVersion {} to Waiting status", contractVersionId, e);
          skippedCount++;
        }
      }

      JPA.clear();
    }

    return skippedCount;
  }
}
