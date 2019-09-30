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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.contract.db.ConsumptionLine;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractTemplate;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.exception.AxelorException;
import com.google.common.collect.Multimap;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public interface ContractService {

  /**
   * Active the contract at the specific date.
   *
   * @param contract to active.
   * @param date to use for active contract.
   */
  @Transactional
  void activeContract(Contract contract, LocalDate date);

  /**
   * Waiting current version
   *
   * @param contract
   * @param date
   */
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  void waitingCurrentVersion(Contract contract, LocalDate date) throws AxelorException;

  /**
   * On going current version. It : - Active the contrat if not yet active - Set current version
   * ongoing - Inc version number
   *
   * @param contract
   * @param date
   */
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  Invoice ongoingCurrentVersion(Contract contract, LocalDate date) throws AxelorException;

  /**
   * Waiting next version
   *
   * @param contract
   * @param date
   */
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  void waitingNextVersion(Contract contract, LocalDate date) throws AxelorException;

  /**
   * Active the next version. It : - Terminate currentVersion - Archive current version - Ongoing
   * next version (now consider as current version)
   *
   * @param contract
   * @param date
   */
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  void activeNextVersion(Contract contract, LocalDate date) throws AxelorException;

  /**
   * Archive the current version (moved to history) and move next version as current version
   *
   * @param contract
   * @param date
   */
  @Transactional
  void archiveVersion(Contract contract, LocalDate date);

  /**
   * Check if can terminate the contract.
   *
   * @param contract The contract to check.
   * @throws AxelorException Check condition failed.
   */
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  void checkCanTerminateContract(Contract contract) throws AxelorException;

  /**
   * Terminate the contract
   *
   * @param contract
   * @param isManual
   * @param date
   */
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  void terminateContract(Contract contract, Boolean isManual, LocalDate date)
      throws AxelorException;

  /**
   * Invoicing the contract
   *
   * @param contract
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  Invoice invoicingContract(Contract contract) throws AxelorException;

  /**
   * Renew a contract
   *
   * @param contract
   * @param date
   */
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  void renewContract(Contract contract, LocalDate date) throws AxelorException;

  /**
   * Generate a new contract based on template
   *
   * @param template
   */
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  Contract copyFromTemplate(Contract contract, ContractTemplate template) throws AxelorException;

  Contract increaseInvoiceDates(Contract contract);

  /**
   * Check if contract is valid, throws exceptions instead.
   *
   * @param contract to be check.
   * @throws AxelorException if the contract is invalid.
   */
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  void isValid(Contract contract) throws AxelorException;

  /**
   * Take each consumption line and convert it to contract line if a associate consumption contract
   * line is present in contract.
   *
   * @param contract contain consumption and contract lines.
   * @return Multimap of consumption lines successfully converted to contract lines.
   */
  Multimap<ContractLine, ConsumptionLine> mergeConsumptionLines(Contract contract);

  default List<ContractVersion> getVersions(Contract contract) {
    List<ContractVersion> versions = contract.getVersionHistory();
    if (versions == null) {
      versions = new ArrayList<>();
    }
    if (contract.getCurrentContractVersion() != null) {
      versions.add(contract.getCurrentContractVersion());
    }
    return versions;
  }

  default boolean isFullProrated(Contract contract) {
    return contract.getCurrentContractVersion() != null
        && (contract.getCurrentContractVersion().getIsTimeProratedInvoice()
            && contract.getCurrentContractVersion().getIsVersionProratedInvoice());
  }
}
