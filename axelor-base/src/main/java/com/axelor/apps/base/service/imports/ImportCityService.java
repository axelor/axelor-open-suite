/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.imports;

import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.service.imports.ImportCityServiceImpl.GEONAMES_FILE;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaFile;
import java.io.IOException;
import java.util.Map;

public interface ImportCityService {

  /**
   * Import city
   *
   * @param typeSelect
   * @param dataFile
   * @return
   */
  public ImportHistory importCity(String typeSelect, MetaFile dataFile)
      throws AxelorException, IOException;

  public MetaFile downloadZip(String downloadFileName, GEONAMES_FILE geonamesFile)
      throws AxelorException;

  public Map<String, Object> importFromGeonamesAutoConfig(
      String downloadFileName, String typeSelect);

  public Map<String, Object> importFromGeonamesManualConfig(
      Map<String, Object> map, String typeSelect);
}
