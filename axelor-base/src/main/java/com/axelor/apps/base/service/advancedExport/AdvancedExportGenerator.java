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
package com.axelor.apps.base.service.advancedExport;

import com.axelor.apps.base.db.AdvancedExport;
import com.axelor.exception.AxelorException;
import com.itextpdf.text.DocumentException;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public abstract class AdvancedExportGenerator {

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
   */
  @SuppressWarnings("rawtypes")
  public abstract void generateBody(List<List> dataList);

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
  @SuppressWarnings({"rawtypes"})
  public File generateFile(List<List> dataList) throws AxelorException {
    generateHeader();
    generateBody(dataList);
    close();
    return getExportFile();
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
