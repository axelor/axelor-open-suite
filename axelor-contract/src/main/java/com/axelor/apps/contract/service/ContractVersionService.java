/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.utils.date.DateTool;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ContractVersionService {

  /**
   * Waiting version at the today date.
   *
   * @param version of the contract will be waiting.
   */
  void waiting(ContractVersion version) throws AxelorException;

  /**
   * Waiting version at the specific date.
   *
   * @param version of the contract will be waiting.
   * @param date of waiting.
   */
  void waiting(ContractVersion version, LocalDate date) throws AxelorException;

  /**
   * Ongoing version at the today date.
   *
   * @param version of te contract will be ongoing.
   */
  void ongoing(ContractVersion version) throws AxelorException;

  /**
   * Ongoing version at the specific date.
   *
   * @param version of the contract will be ongoing.
   * @param date of activation.
   */
  void ongoing(ContractVersion version, LocalDateTime dateTime) throws AxelorException;

  /**
   * Terminate version at the today date.
   *
   * @param version of the contract will be terminate.
   */
  void terminate(ContractVersion version) throws AxelorException;

  /**
   * Terminate version at the specific date.
   *
   * @param version of the contract will be terminate.
   * @param date of terminate.
   */
  void terminate(ContractVersion version, LocalDateTime dateTime) throws AxelorException;

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
      if (version.getActivationDateTime() == null || version.getEndDateTime() == null) {
        continue;
      }
      if (DateTool.isBetween(
          version.getActivationDateTime().toLocalDate(),
          version.getEndDateTime().toLocalDate(),
          date)) {
        return version;
      }
    }
    return contract.getCurrentContractVersion();
  }
}
