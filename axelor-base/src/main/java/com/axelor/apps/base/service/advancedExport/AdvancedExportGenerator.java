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
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AdvancedExportGenerator {

  private final Logger log = LoggerFactory.getLogger(AdvancedExportGenerator.class);

  private boolean isReachMaxExportLimit;

  /**
   * This method generate the header of export file.
   *
   * @throws IOException
   * @throws DocumentException
   * @throws AxelorException
   */
  public abstract void generateHeader() throws AxelorException;

  /**
   * This method generate the body of export file.
   *
   * @param dataList
   * @throws AxelorException
   */
  @SuppressWarnings("rawtypes")
  public abstract void generateBody(List<List> dataList) throws AxelorException;

  /**
   * This method close the object.
   *
   * @throws AxelorException
   * @throws IOException
   * @throws DocumentException
   */
  public abstract void close() throws AxelorException;

  /**
   * This method return the object of <i>AdvancedExport</i>.
   *
   * @return
   */
  public abstract AdvancedExport getAdvancedExport();

  /**
   * Get the export file.
   *
   * @return
   */
  public abstract File getExportFile();

  /**
   * Get the name of export file.
   *
   * @return
   */
  public abstract String getFileName();

  /**
   * This method is used to generate the export file.
   *
   * @param advancedExport
   * @param query
   * @return
   * @throws AxelorException
   * @throws IOException
   * @throws DocumentException
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public File generateFile(Query query) throws AxelorException {

    AdvancedExport advancedExport = getAdvancedExport();

    log.debug("Export file : {}", getFileName());

    generateHeader();

    int startPosition = 0;
    int reachLimit = 0;
    int maxExportLimit = advancedExport.getMaxExportLimit();
    int queryFetchLimit = advancedExport.getQueryFetchSize();
    List<List> dataList = new ArrayList<>();
    query.setMaxResults(queryFetchLimit);

    while (startPosition < maxExportLimit) {
      if ((maxExportLimit - startPosition) < queryFetchLimit) {
        query.setMaxResults((maxExportLimit - startPosition));
      }
      query.setFirstResult(startPosition);
      dataList = query.getResultList();
      if (dataList.isEmpty()) break;

      generateBody(dataList);

      startPosition = startPosition + queryFetchLimit;
      reachLimit += dataList.size();
    }
    if (maxExportLimit == reachLimit) {
      isReachMaxExportLimit = true;
    }
    close();
    return getExportFile();
  }

  public boolean getIsReachMaxExportLimit() {
    return isReachMaxExportLimit;
  }

  public String getExportFileName() {
    return getFileName();
  }

  /**
   * Explicitly convert decimal value with it's scale.
   *
   * @param value
   * @return
   */
  public String convertDecimalValue(Object value) {
    BigDecimal decimalVal = (BigDecimal) value;
    return String.format("%." + decimalVal.scale() + "f", decimalVal);
  }
}
