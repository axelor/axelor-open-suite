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

import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.repo.ContractLineRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class ContractLinePackServiceImpl implements ContractLinePackService {

  @Override
  public boolean hasEndOfPackTypeLine(List<ContractLine> contractLineList) {
    return CollectionUtils.isNotEmpty(contractLineList)
        && contractLineList.stream()
            .anyMatch(
                contractLine ->
                    contractLine.getTypeSelect() == ContractLineRepository.TYPE_END_OF_PACK);
  }

  @Override
  public void computePackTotal(List<ContractLine> contractLineList) {
    if (!hasEndOfPackTypeLine(contractLineList)) {
      return;
    }

    List<ContractLine> sortedLines = new ArrayList<>(contractLineList);
    sortedLines.sort(
        Comparator.comparing(
            ContractLine::getSequence, Comparator.nullsLast(Comparator.naturalOrder())));

    BigDecimal totalExTaxTotal = BigDecimal.ZERO;
    BigDecimal totalInTaxTotal = BigDecimal.ZERO;

    for (ContractLine contractLine : sortedLines) {
      switch (contractLine.getTypeSelect()) {
        case ContractLineRepository.TYPE_NORMAL:
          totalExTaxTotal = totalExTaxTotal.add(contractLine.getExTaxTotal());
          totalInTaxTotal = totalInTaxTotal.add(contractLine.getInTaxTotal());
          break;

        case ContractLineRepository.TYPE_START_OF_PACK:
          totalExTaxTotal = BigDecimal.ZERO;
          totalInTaxTotal = BigDecimal.ZERO;
          break;

        case ContractLineRepository.TYPE_END_OF_PACK:
          boolean showTotal = contractLine.getIsShowTotal();
          contractLine.setQty(BigDecimal.ZERO);
          contractLine.setExTaxTotal(showTotal ? totalExTaxTotal : BigDecimal.ZERO);
          contractLine.setInTaxTotal(showTotal ? totalInTaxTotal : BigDecimal.ZERO);
          contractLine.setInitialPricePerYear(BigDecimal.ZERO);
          contractLine.setYearlyPriceRevalued(BigDecimal.ZERO);
          totalExTaxTotal = BigDecimal.ZERO;
          totalInTaxTotal = BigDecimal.ZERO;
          break;

        case ContractLineRepository.TYPE_TITLE:
        default:
          break;
      }
    }
  }

  @Override
  public void resetPackTotal(List<ContractLine> contractLineList) {
    if (CollectionUtils.isEmpty(contractLineList)) {
      return;
    }
    contractLineList.stream()
        .filter(
            contractLine -> contractLine.getTypeSelect() == ContractLineRepository.TYPE_END_OF_PACK)
        .forEach(
            contractLine -> {
              contractLine.setIsHideUnitAmounts(Boolean.FALSE);
              contractLine.setIsShowTotal(Boolean.FALSE);
              contractLine.setExTaxTotal(BigDecimal.ZERO);
              contractLine.setInTaxTotal(BigDecimal.ZERO);
              contractLine.setInitialPricePerYear(BigDecimal.ZERO);
              contractLine.setYearlyPriceRevalued(BigDecimal.ZERO);
            });
  }
}
