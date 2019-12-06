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
package com.axelor.apps.contract.service;

import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.tool.date.DateTool;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;

public interface ContractVersionService {

  /**
   * Waiting version at the today date.
   *
   * @param version of the contract will be waiting.
   */
  @Transactional(rollbackOn = {Exception.class})
  void waiting(ContractVersion version) throws AxelorException;

  /**
   * Waiting version at the specific date.
   *
   * @param version of the contract will be waiting.
   * @param date of waiting.
   */
  @Transactional(rollbackOn = {Exception.class})
  void waiting(ContractVersion version, LocalDate date) throws AxelorException;

  /**
   * Ongoing version at the today date.
   *
   * @param version of te contract will be ongoing.
   */
  @Transactional(rollbackOn = {Exception.class})
  void ongoing(ContractVersion version) throws AxelorException;

  /**
   * Ongoing version at the specific date.
   *
   * @param version of the contract will be ongoing.
   * @param date of activation.
   */
  @Transactional(rollbackOn = {Exception.class})
  void ongoing(ContractVersion version, LocalDate date) throws AxelorException;

  /**
   * Terminate version at the today date.
   *
   * @param version of the contract will be terminate.
   */
  @Transactional
  void terminate(ContractVersion version);

  /**
   * Terminate version at the specific date.
   *
   * @param version of the contract will be terminate.
   * @param date of terminate.
   */
  @Transactional
  void terminate(ContractVersion version, LocalDate date);

  /**
   * Create new version from contract but don't save it. There will be use for set values from form
   * view.
   *
   * @param contract for use the actual version as base.
   * @return the copy a contract's actual version.
   */
  ContractVersion newDraft(Contract contract);

  default ContractVersion getContractVersion(Contract contract, LocalDate date) {
    for (ContractVersion version : contract.getVersionHistory()) {
      if (version.getActivationDate() == null || version.getEndDate() == null) {
        continue;
      }
      if (DateTool.isBetween(version.getActivationDate(), version.getEndDate(), date)) {
        return version;
      }
    }
    ContractVersion version = contract.getCurrentContractVersion();
    if (DateTool.isBetween(version.getActivationDate(), version.getEndDate(), date)) {
      return version;
    }
    return null;
  }
}
