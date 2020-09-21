/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.Product;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.exception.AxelorException;

public interface ContractLineService {
  /**
   * Set to null ContractLine fields for form view.
   *
   * @param contractLine to reset.
   * @return ContractLine reset.
   */
  ContractLine reset(ContractLine contractLine);

  /**
   * Fill ContractLine with Product information.
   *
   * @param contractLine to fill.
   * @param product to get information.
   * @return ContractLine filled with Product information.
   */
  ContractLine fill(ContractLine contractLine, Product product) throws AxelorException;

  /**
   * Compute price and tax of Product to ContractLine.
   *
   * @param contractLine to save price and tax.
   * @param contract to give additional information like Partner and Company.
   * @param product to use for computing.
   * @return ContractLine price and tax computed.
   * @throws AxelorException if a error occurred when we get tax line.
   */
  ContractLine compute(ContractLine contractLine, Contract contract, Product product)
      throws AxelorException;

  /**
   * Fill and compute ContractLine with Product.
   *
   * @param contractLine to fill and compute.
   * @param contract to give additional information.
   * @param product to use operation.
   * @return ContractLine filled and computed.
   * @throws AxelorException if a error occurred when we get tax line.
   */
  ContractLine fillAndCompute(ContractLine contractLine, Contract contract, Product product)
      throws AxelorException;

  /**
   * Compute ex and in tax total for ContractLine.
   *
   * @param contractLine to compute ex/in tax total.
   * @return ContractLine with ex/in tax total computed.
   */
  ContractLine computeTotal(ContractLine contractLine);

  /**
   * Create analytic move lines using analytic distribution template
   *
   * @param contractLine
   * @return ContractLine filled with analytic move lines
   */
  ContractLine createAnalyticDistributionWithTemplate(ContractLine contractLine, Contract contract);
}
