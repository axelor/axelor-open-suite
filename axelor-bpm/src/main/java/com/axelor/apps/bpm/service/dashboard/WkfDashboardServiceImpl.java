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

import com.axelor.db.JPA;
import java.time.LocalDate;
import java.util.List;
import javax.persistence.Query;
import org.apache.commons.lang3.StringUtils;

public class WkfDashboardServiceImpl implements WkfDashboardService {

  @SuppressWarnings("unchecked")
  @Override
  public List<Long> getStatusPerMonthRecord(
      String tableName, String status, String month, String jsonModel) {

    String condition = "TO_CHAR(activity.start_time_,'yyyy-MM') = :month";

    if (jsonModel != null) {
      condition += " AND record.json_model = '" + jsonModel + "'";
    }

    Query query = createCommonRecordQuery(tableName, condition, status);

    query.setParameter("month", month);

    return query.getResultList();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Long> getStatusPerDayRecord(
      String tableName, String status, String day, String jsonModel) {

    String condition = "TO_CHAR(activity.start_time_,'MM/dd/yyyy') = :day";

    if (jsonModel != null) {
      condition += " AND record.json_model = '" + jsonModel + "'";
    }

    Query query = createCommonRecordQuery(tableName, condition, status);

    query.setParameter("day", day.toString());

    return query.getResultList();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Long> getTimespentPerStatusRecord(
      String tableName, String status, LocalDate fromDate, LocalDate toDate, String jsonModel) {

    String condition = "DATE(activity.start_time_) BETWEEN :fromDate AND :toDate";

    if (jsonModel != null) {
      condition += " AND record.json_model = '" + jsonModel + "'";
    }

    Query query = createCommonRecordQuery(tableName, condition, status);

    query.setParameter("fromDate", fromDate);
    query.setParameter("toDate", toDate);

    return query.getResultList();
  }

  private Query createCommonRecordQuery(String tableName, String condition, String status) {

    StringBuilder queryBuilder = new StringBuilder();

    queryBuilder.append("SELECT record.id FROM " + tableName + " record ");
    queryBuilder.append(
        "LEFT JOIN act_hi_actinst activity ON record.process_instance_id = activity.proc_inst_id_ ");
    queryBuilder.append("WHERE (activity.act_name_ = :status OR activity.act_id_ = :status)");

    if (!StringUtils.isEmpty(condition)) {
      queryBuilder.append(" AND " + condition);
    }

    Query query = JPA.em().createNativeQuery(queryBuilder.toString());
    query.setParameter("status", status);
    return query;
  }
}
