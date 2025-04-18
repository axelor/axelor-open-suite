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
package com.axelor.apps.contract.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractVersion;
import java.math.BigDecimal;
import java.util.Map;

public interface ContractLineService {
  /**
   * Set to null ContractLine fields for form view.
   *
   * @param contractLine to reset.
   * @return ContractLine reset.
   */
  Map<String, Object> reset(ContractLine contractLine);

  /**
   * Fill ContractLine with Product information.
   *
   * @param contractLine to fill.
   * @param contract to give additional information like Company.
   * @param product to get information.
   * @return ContractLine filled with Product information.
   */
  ContractLine fill(ContractLine contractLine, Contract contract, Product product)
      throws AxelorException;

  ContractLine fillDefault(ContractLine contractLine, ContractVersion contractVersion);

  void updateContractLinesFromContractVersion(ContractVersion contractVersion);

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
  ContractLine computeTotal(ContractLine contractLine, Contract contract) throws AxelorException;

  ContractLine computePricesPerYear(ContractLine contractLine, ContractVersion contractVersion)
      throws AxelorException;

  void computeAnalytic(Contract contract, ContractLine contractLine) throws AxelorException;

  ContractLine resetProductInformation(ContractLine contractLine);

  Map<String, Object> getDiscountsFromPriceLists(
      Contract contract, ContractLine contractLine, BigDecimal price);

  PriceListLine getPriceListLine(ContractLine contractLine, PriceList priceList, BigDecimal price);

  String computeProductDomain(Contract contract);

  void checkAnalyticAxisByCompany(Contract contract) throws AxelorException;
}
