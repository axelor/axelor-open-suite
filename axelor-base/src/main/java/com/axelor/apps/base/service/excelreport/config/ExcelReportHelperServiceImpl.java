package com.axelor.apps.base.service.excelreport.config;

import com.axelor.apps.base.db.PrintTemplate;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Splitter;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import org.apache.commons.lang3.tuple.ImmutablePair;

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

  @Override
  public Mapper getMapper(String modelFullName) throws ClassNotFoundException {
    Class<?> klass = Class.forName(modelFullName);
    return Mapper.of(klass);
  }

  @Override
  public ImmutablePair<Property, Object> findField(final Mapper mapper, Object value, String name) {
    final Iterator<String> iter = Splitter.on(".").split(name).iterator();
    Mapper current = mapper;
    Property property = current.getProperty(iter.next());

    if (property == null || (property.isJson() && iter.hasNext())) {
      return null;
    }

    while (property != null && property.getTarget() != null && iter.hasNext()) {
      if (ObjectUtils.notEmpty(value)) {
        value = property.get(value);
      }
      current = Mapper.of(property.getTarget());
      property = current.getProperty(iter.next());
    }

    return ImmutablePair.of(property, value);
  }
}
