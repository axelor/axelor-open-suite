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
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.supplychain.db.Packaging;
import com.axelor.apps.supplychain.db.repo.PackagingRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;

public class PackagingCreateServiceImpl implements PackagingCreateService {

  protected final PackagingRepository packagingRepository;

  @Inject
  public PackagingCreateServiceImpl(PackagingRepository packagingRepository) {
    this.packagingRepository = packagingRepository;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public Packaging createPackaging(
      LogisticalForm logisticalForm, Packaging parentPackaging, Product packageUsed)
      throws AxelorException {
    if (logisticalForm == null && parentPackaging == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(SupplychainExceptionMessage.PACKAGING_PARENT_ERROR));
    }
    Packaging packaging = new Packaging();

    setParent(logisticalForm, parentPackaging, packaging);

    updatePackageUsed(packageUsed, packaging);

    return packagingRepository.save(packaging);
  }

  protected void setParent(
      LogisticalForm logisticalForm, Packaging parentPackaging, Packaging packaging) {
    if (logisticalForm != null && parentPackaging != null) {
      packaging.setParentPackaging(parentPackaging);
      return;
    }
    packaging.setParentPackaging(parentPackaging);
    packaging.setLogisticalForm(logisticalForm);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void updatePackageUsed(Product packageUsed, Packaging packaging) throws AxelorException {
    if (packageUsed != null) {
      if (!packageUsed.getIsPackaging()) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY, I18n.get("Product must be a packaging."));
      }
      packaging.setPackageUsed(packageUsed);
      packaging.setPackagingLevelSelect(packageUsed.getPackagingLevelSelect());
    }
    packagingRepository.save(packaging);
  }
}
