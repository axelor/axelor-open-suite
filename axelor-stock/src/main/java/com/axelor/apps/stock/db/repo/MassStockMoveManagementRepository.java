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
package com.axelor.apps.stock.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;

public class MassStockMoveManagementRepository extends MassStockMoveRepository {

  protected SequenceService sequenceService;

  @Inject
  public MassStockMoveManagementRepository(SequenceService sequenceService) {
    this.sequenceService = sequenceService;
  }

  @Override
  public MassStockMove save(MassStockMove massStockMove) {
    try {
      if (StringUtils.isEmpty(massStockMove.getSequence())) {
        massStockMove.setSequence(getSequence(massStockMove));
      }
      return super.save(massStockMove);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e);
    }
  }

  protected String getSequence(MassStockMove massStockMove) throws AxelorException {
    Company company = massStockMove.getCompany();
    String sequence =
        sequenceService.getSequenceNumber(
            SequenceRepository.MASS_STOCK_MOVE,
            company,
            MassStockMove.class,
            "sequence",
            massStockMove);
    if (sequence == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.MASS_STOCK_MOVE_NO_SEQUENCE),
          company.getName());
    }
    return sequence;
  }
}
