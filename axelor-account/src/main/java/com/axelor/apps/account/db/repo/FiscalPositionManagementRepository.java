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
package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import java.util.stream.Collectors;
import javax.persistence.PersistenceException;

public class FiscalPositionManagementRepository extends FiscalPositionRepository {

  @Override
  public FiscalPosition save(FiscalPosition fiscalPosition) {
    try {
      if (isFromTaxUniqueForFiscalPosition(fiscalPosition)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_UNIQUE_KEY,
            I18n.get(AccountExceptionMessage.FISCAL_POSITION_DUPLICATE_FROM_TAX_SET));
      }
      return super.save(fiscalPosition);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }

  protected boolean isFromTaxUniqueForFiscalPosition(FiscalPosition fiscalPosition) {
    return ObjectUtils.notEmpty(fiscalPosition.getTaxEquivList())
        && fiscalPosition.getTaxEquivList().stream()
            .collect(Collectors.groupingBy(TaxEquiv::getFromTaxSet))
            .values()
            .stream()
            .anyMatch(list -> list.size() > 1);
  }
}
