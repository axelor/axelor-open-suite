/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.PayrollPreparation;
import java.io.IOException;
import java.util.List;

public interface PayrollPreparationExportService {

  String exportPayrollPreparation(PayrollPreparation payrollPreparation)
      throws AxelorException, IOException;

  void exportNibelis(PayrollPreparation payrollPreparation, List<String[]> list)
      throws AxelorException;

  public String getPayrollPreparationExportName();

  String[] getPayrollPreparationExportHeader();

  String[] getPayrollPreparationMeilleurGestionExportHeader();

  List<String[]> exportSilae(PayrollPreparation payrollPrep, List<String[]> exportLineList)
      throws AxelorException;

  String[] getPayrollPreparationSilaeExportHeader();
}
