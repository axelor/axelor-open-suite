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
package com.axelor.apps.account.service.fecimport;

import com.axelor.apps.account.db.FECImport;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import java.time.LocalDate;
import java.util.Map;

public interface ImportMoveFecService {
  Move createOrGetMove(
      Map<String, Object> values,
      Company company,
      FECImport fecImport,
      LocalDate moveLineDate,
      String importReference)
      throws Exception;

  MoveLine fillMoveLineInformation(
      MoveLine moveLine,
      Map<String, Object> values,
      Move move,
      FECImport fecImport,
      String importReference)
      throws AxelorException;
}
