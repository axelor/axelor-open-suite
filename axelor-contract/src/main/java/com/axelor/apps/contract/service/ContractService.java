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
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractTemplate;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;

public interface ContractService {

  /**
   * Active the contract
   *
   * @param contract
   * @param date
   */
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void activeContract(Contract contract, LocalDate date);

  /**
   * Waiting current version
   *
   * @param contract
   * @param date
   */
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void waitingCurrentVersion(Contract contract, LocalDate date);

  /**
   * On going current version. It : - Active the contrat if not yet active - Set current version
   * ongoing - Inc version number
   *
   * @param contract
   * @param date
   */
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Invoice ongoingCurrentVersion(Contract contract, LocalDate date) throws AxelorException;

  /**
   * Waiting next version
   *
   * @param contract
   * @param date
   */
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void waitingNextVersion(Contract contract, LocalDate date);

  /**
   * Active the next version. It : - Terminate currentVersion - Archive current version - Ongoing
   * next version (now consider as current version)
   *
   * @param contract
   * @param date
   */
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void activeNextVersion(Contract contract, LocalDate date) throws AxelorException;

  /**
   * Archive the current version (moved to history) and move next version as current version
   *
   * @param contract
   * @param date
   */
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void archiveVersion(Contract contract, LocalDate date);

  /**
   * Terminate the contract
   *
   * @param contract
   * @param isManual
   * @param date
   */
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void terminateContract(Contract contract, Boolean isManual, LocalDate date)
      throws AxelorException;

  /**
   * Invoicing the contract
   *
   * @param contract
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Invoice invoicingContract(Contract contract) throws AxelorException;

  /**
   * Renew a contract
   *
   * @param contract
   * @param date
   */
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void renewContract(Contract contract, LocalDate date) throws AxelorException;

  /**
   * Generate a new contract based on template
   *
   * @param template
   */
  @Transactional
  public Contract createContractFromTemplate(ContractTemplate template);
}
