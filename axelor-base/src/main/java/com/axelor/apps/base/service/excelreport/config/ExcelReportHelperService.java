package com.axelor.apps.base.service.excelreport.config;

import com.axelor.apps.base.db.PrintTemplate;
import java.util.ResourceBundle;

public interface ExcelReportHelperService {

  public ResourceBundle getResourceBundle(PrintTemplate printTemplate);

  public int getBigDecimalScale();
}
