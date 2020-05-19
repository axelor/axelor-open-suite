/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service.declarationofexchanges;

import com.axelor.apps.base.db.EconomicArea;
import com.axelor.apps.supplychain.db.DeclarationOfExchanges;
import com.axelor.exception.AxelorException;
import java.nio.file.Path;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;

public interface DeclarationOfExchangesService {
  /**
   * Export declaration of exchanges.
   *
   * @param declarationOfExchanges
   * @return
   * @throws AxelorException
   */
  Pair<Path, String> export(DeclarationOfExchanges declarationOfExchanges) throws AxelorException;

  /**
   * Override this method to use custom class for export.
   *
   * @param economicArea
   * @return
   */
  Map<String, Map<String, Class<? extends DeclarationOfExchangesExporter>>>
      getExportServiceClassMap(EconomicArea economicArea);
}
