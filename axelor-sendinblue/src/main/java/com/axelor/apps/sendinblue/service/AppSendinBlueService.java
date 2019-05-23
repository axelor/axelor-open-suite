/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sendinblue.service;

import com.axelor.apps.base.db.AppSendinblue;
import com.axelor.exception.AxelorException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import sendinblue.auth.ApiKeyAuth;

public interface AppSendinBlueService {

  ApiKeyAuth getApiKeyAuth() throws AxelorException;

  void exportContactFields() throws AxelorException;

  void exportContacts(AppSendinblue appSendinblue) throws AxelorException;

  void exportTemplate() throws AxelorException;

  void exportCampaign() throws AxelorException;

  void importCampaignReport() throws AxelorException;

  List<Map<String, Object>> getReport(LocalDate fromDate, LocalDate toDate);

  void importEvents() throws AxelorException;

  void importContactStat() throws AxelorException;

  void importCampaignStat() throws AxelorException;
}
