/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.supplychain.db.Mrp;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.MrpService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import javax.persistence.PersistenceException;

public class MrpManagementRepository extends MrpRepository {

  @Override
  public void remove(Mrp entity) {

    Beans.get(MrpService.class).reset(entity);

    super.save(entity);
  }

  @Override
  public Mrp save(Mrp entity) {

    try {
      if (Strings.isNullOrEmpty(entity.getMrpSeq())) {
        Company company = entity.getStockLocation().getCompany();
        String seq =
            Beans.get(SequenceService.class)
                .getSequenceNumber(
                    SequenceRepository.SUPPLYCHAIN_MRP, company, Mrp.class, "mrpSeq");

        if (seq == null) {
          throw new AxelorException(
              company,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(SupplychainExceptionMessage.SUPPLYCHAIN_MRP_SEQUENCE_ERROR),
              company.getName());
        }

        entity.setMrpSeq(seq);
      }
    } catch (AxelorException e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }

    return super.save(entity);
  }

  @Override
  public Mrp copy(Mrp entity, boolean deep) {

    Mrp copy = super.copy(entity, deep);
    copy.setMrpSeq(null);
    copy.setStartDateTime(null);
    copy.setEndDateTime(null);
    copy.setStatusSelect(MrpRepository.STATUS_DRAFT);
    return copy;
  }
}
