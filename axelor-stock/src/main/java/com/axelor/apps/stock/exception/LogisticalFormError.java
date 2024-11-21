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
package com.axelor.apps.stock.exception;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.LogisticalFormLine;

public class LogisticalFormError extends AxelorException {

  private static final long serialVersionUID = 354779411257144849L;

  public LogisticalFormError(LogisticalForm logisticalForm, String message) {
    super(logisticalForm, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, message);
  }

  public LogisticalFormError(
      LogisticalFormLine logisticalFormLine, String message, Object... messageArgs) {
    super(
        logisticalFormLine,
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        String.format(message, messageArgs));
  }
}
