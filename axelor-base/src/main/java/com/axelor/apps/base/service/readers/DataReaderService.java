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
package com.axelor.apps.base.service.readers;

import com.axelor.meta.db.MetaFile;

public interface DataReaderService {

  /**
   * Initialize the input file.
   *
   * @param input
   * @return
   */
  public boolean initialize(MetaFile input, String separator);

  /**
   * Returns record/row of a particular line.
   *
   * @param key
   * @param index
   * @param headerSize
   * @return
   */
  public String[] read(String sheetName, int index, int headerSize);

  /**
   * Returns total number of lines.
   *
   * @param key
   * @return
   */
  public int getTotalLines(String sheetName);

  /**
   * Returns name of sheets.
   *
   * @return
   */
  public String[] getSheetNames();
}
