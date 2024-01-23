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
package com.axelor.apps.base.service.advanced.imports;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.AdvancedImport;
import com.axelor.db.mapper.Mapper;
import com.axelor.utils.reader.DataReaderService;

public interface AdvancedImportService {

  public boolean apply(AdvancedImport advanceImport) throws AxelorException, ClassNotFoundException;

  public Mapper getMapper(String modelFullName) throws ClassNotFoundException;

  public int getTabConfigRowCount(
      String sheet, DataReaderService reader, int totalLines, String[] objectRow);

  public boolean resetImport(AdvancedImport advancedImport) throws ClassNotFoundException;
}
