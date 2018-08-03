/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.db;

public interface IDataBackup {

  /** Static DataBackup Message */
  static final String IMPORT_COMPLETE = "Import Completed";

  static final String EXPORT_COMPLETE = "export Completed";
  static final String EXPORT_MODULE_NAME = "Exporting Model Named : ";

  static final String TEMP_FOLDER_NAME = "/temp";
  static final String CONFIG_PREFIX = "config";
  static final String LOG_FILE_NAME = "_backUpLog.log";
  static final String TOTAL_IMPORT = "Total Records : ";
  static final String SUCCESS_IMPORT = "Success Records : ";

  static final String CONGIF_FILE_TABLE_LINE_SEPERATOR =
      "------------------------------------------------------";
}
