package com.axelor.apps.base.service.excelreport.config;

import com.axelor.apps.base.db.PrintTemplate;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import java.util.ResourceBundle;
import org.apache.commons.lang3.tuple.ImmutablePair;

public interface ExcelReportHelperService {

  public ResourceBundle getResourceBundle(PrintTemplate printTemplate);

  public int getBigDecimalScale();

  public Mapper getMapper(String modelFullName) throws ClassNotFoundException;

  public ImmutablePair<Property, Object> findField(final Mapper mapper, Object value, String name);
}
