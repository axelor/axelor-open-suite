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
package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.supplychain.db.Packaging;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import jakarta.persistence.PersistenceException;

public class PackagingSupplychainRepository extends PackagingRepository {

  protected final SequenceService sequenceService;

  @Inject
  public PackagingSupplychainRepository(SequenceService sequenceService) {
    this.sequenceService = sequenceService;
  }

  @Override
  public Packaging save(Packaging packaging) {
    try {
      if (Strings.isNullOrEmpty(packaging.getPackagingNumber())) {
        setPackagingNumber(packaging);
      }
      for (Packaging childPackaging : packaging.getChildrenPackagingList()) {
        if (Strings.isNullOrEmpty(childPackaging.getPackagingNumber())) {
          setPackagingNumber(childPackaging);
        }
      }
      return super.save(packaging);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
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
