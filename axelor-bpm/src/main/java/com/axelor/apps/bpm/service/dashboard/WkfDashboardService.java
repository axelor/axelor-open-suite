/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bpm.service.dashboard;

import java.time.LocalDate;
import java.util.List;

public interface WkfDashboardService {

  /**
   * Get the record of meta model or meta json model for the particular month.
   *
   * @param tableName
   * @param status
   * @param month
   * @param jsonModel
   * @return
   */
  public List<Long> getStatusPerMonthRecord(
      String tableName, String status, String month, String jsonModel);

  /**
   * Get the record of meta model or meta json model for the particular day.
   *
   * @param tableName
   * @param status
   * @param day
   * @param jsonModel
   * @return
   */
  public List<Long> getStatusPerDayRecord(
      String tableName, String status, String day, String jsonModel);

  /**
   * Get the record of meta model or meta json model for time spent on the particular status.
   *
   * @param tableName
   * @param status
   * @param fromDate
   * @param toDate
   * @param jsonModel
   * @return
   */
  public List<Long> getTimespentPerStatusRecord(
      String tableName, String status, LocalDate fromDate, LocalDate toDate, String jsonModel);
}
