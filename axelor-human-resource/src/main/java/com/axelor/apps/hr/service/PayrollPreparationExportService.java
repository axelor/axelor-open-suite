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
