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
package com.axelor.apps.supplychain.service.packaging;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.supplychain.db.Packaging;
import com.axelor.apps.supplychain.db.PackagingLine;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class PackagingMassServiceImpl implements PackagingMassService {

  protected PackagingLineService packagingLineService;

  @Inject
  public PackagingMassServiceImpl(PackagingLineService packagingLineService) {
    this.packagingLineService = packagingLineService;
  }

  @Override
  public void updatePackagingMass(LogisticalForm logisticalForm) throws AxelorException {
    List<Packaging> packagingList = logisticalForm.getPackagingList();
    if (CollectionUtils.isEmpty(packagingList)) {
      logisticalForm.setTotalGrossMass(BigDecimal.ZERO);
      logisticalForm.setTotalNetMass(BigDecimal.ZERO);
      return;
    }
    for (Packaging packaging : packagingList) {
      updatePackagingMass(packaging);
    }
    logisticalForm.setTotalGrossMass(
        packagingList.stream()
            .map(Packaging::getTotalGrossMass)
            .reduce(BigDecimal.ZERO, BigDecimal::add));

    logisticalForm.setTotalNetMass(
        packagingList.stream()
            .map(Packaging::getTotalNetMass)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
  }

  @Override
  public void updatePackagingMass(Packaging packaging) throws AxelorException {
    BigDecimal grossMass = BigDecimal.ZERO;
    BigDecimal netMass = BigDecimal.ZERO;

    Product packageUsed = packaging.getPackageUsed();
    if (packageUsed != null) {
      grossMass = packageUsed.getGrossMass();
      netMass = packageUsed.getNetMass();
    }
    packaging.setNetMass(netMass);
    packaging.setGrossMass(grossMass);

    BigDecimal totalGrossMass = grossMass;
    BigDecimal totalNetMass = netMass;

    if (CollectionUtils.isNotEmpty(packaging.getPackagingLineList())) {
      for (PackagingLine line : packaging.getPackagingLineList()) {
        BigDecimal[] mass = packagingLineService.computePackagingLineMass(line);
        totalGrossMass = totalGrossMass.add(mass[0]);
        totalNetMass = totalNetMass.add(mass[1]);
      }
    }
    if (CollectionUtils.isNotEmpty(packaging.getChildrenPackagingList())) {
      for (Packaging childPackaging : packaging.getChildrenPackagingList()) {
        updatePackagingMass(childPackaging);
        totalGrossMass = totalGrossMass.add(childPackaging.getTotalGrossMass());
        totalNetMass = totalNetMass.add(childPackaging.getTotalNetMass());
      }
    }
    packaging.setTotalGrossMass(totalGrossMass);
    packaging.setTotalNetMass(totalNetMass);
  }
}
