package com.axelor.apps.base.service.excelreport.config;

import com.axelor.apps.base.db.PrintTemplate;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import java.util.Locale;
import java.util.ResourceBundle;

public class ExcelReportHelperServiceImpl implements ExcelReportHelperService {

  @Override
  public ResourceBundle getResourceBundle(PrintTemplate printTemplate) {

    ResourceBundle resourceBundle;
    String language =
        ObjectUtils.notEmpty(printTemplate.getLanguage())
            ? printTemplate.getLanguage().getCode()
            : null;

    if (language == null) {
      resourceBundle = I18n.getBundle();
    } else if (language.equals("fr")) {
      resourceBundle = I18n.getBundle(Locale.FRANCE);
    } else {
      resourceBundle = I18n.getBundle(Locale.ENGLISH);
    }

    return resourceBundle;
  }

  @Override
  public int getBigDecimalScale() {
    int bigDecimalScale = Beans.get(AppBaseService.class).getAppBase().getBigdecimalScale();
    if (bigDecimalScale == 0) {
      bigDecimalScale = ExcelReportConstants.DEFAULT_BIGDECIMAL_SCALE;
    }
    return bigDecimalScale;
  }
}
