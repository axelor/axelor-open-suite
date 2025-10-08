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
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.supplychain.db.Packaging;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class PackagingSequenceServiceImpl implements PackagingSequenceService {

  protected final SequenceService sequenceService;

  @Inject
  public PackagingSequenceServiceImpl(SequenceService sequenceService) {
    this.sequenceService = sequenceService;
  }

  @Override
  public void generatePackagingNumber(LogisticalForm logisticalForm) throws AxelorException {
    List<Packaging> packagingList = logisticalForm.getPackagingList();
    if (CollectionUtils.isEmpty(packagingList)) {
      return;
    }
    for (Packaging packaging : packagingList) {
      generatePackagingNumber(packaging);
    }
  }

  @Override
  public void generatePackagingNumber(Packaging packaging) throws AxelorException {
    if (Strings.isNullOrEmpty(packaging.getPackagingNumber())) {
      setPackagingNumber(packaging);
    }
    List<Packaging> childrenPackagingList = packaging.getChildrenPackagingList();
    if (CollectionUtils.isNotEmpty(childrenPackagingList)) {
      for (Packaging childPackaging : childrenPackagingList) {
        generatePackagingNumber(childPackaging);
      }
    }
  }

  protected void setPackagingNumber(Packaging packaging) throws AxelorException {
    String packagingNumber =
        sequenceService.getSequenceNumber(
            SequenceRepository.SUPPLYCHAIN_PACKAGING,
            Packaging.class,
            "packagingNumber",
            packaging);
    if (Strings.isNullOrEmpty(packagingNumber)) {
      throw new AxelorException(
          Sequence.class,
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(SupplychainExceptionMessage.SUPPLYCHAIN_PACKAGING_SEQUENCE_ERROR));
    }
    packaging.setPackagingNumber(packagingNumber);
  }
}
