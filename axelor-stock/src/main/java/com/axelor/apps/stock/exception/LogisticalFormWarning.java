/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.stock.exception;

import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;

public class LogisticalFormWarning extends AxelorException {

  private static final long serialVersionUID = 7036277936135855411L;

  public LogisticalFormWarning(
      LogisticalForm logisticalForm, String message, Object... messageArgs) {
    super(logisticalForm, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, message, messageArgs);
  }
}
