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
package com.axelor.apps.base.service.advancedExport;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.AdvancedExport;
import com.axelor.meta.db.MetaModel;
import com.axelor.rpc.filter.Filter;
import java.io.File;
import java.util.List;
import javax.persistence.Query;

public interface AdvancedExportService {

  public static final String LANGUAGE_FR = "fr";

  public static final String PDF = "PDF";
  public static final String EXCEL = "EXCEL";
  public static final String CSV = "CSV";

  public Query getAdvancedExportQuery(AdvancedExport advancedExport, List<Long> recordIds)
      throws AxelorException;

  public File export(AdvancedExport advancedExport, List<Long> recordIds, String fileType)
      throws AxelorException;

  public Filter getJpaSecurityFilter(MetaModel metaModel);

  public boolean getIsReachMaxExportLimit();

  public String getExportFileName();

  public boolean checkAdvancedExportExist(String metaModelName);
}
