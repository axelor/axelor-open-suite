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

import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;

public interface ContractVersionService {

  /**
   * Waiting version
   *
   * @param version
   * @param date
   */
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  void waiting(ContractVersion version, LocalDate date);

  /**
   * Ongoing version
   *
   * @param version
   * @param date
   */
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  void ongoing(ContractVersion version, LocalDate date) throws AxelorException;

  /**
   * terminate version
   *
   * @param version
   * @param date
   */
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  void terminate(ContractVersion version, LocalDate date);
}
