package com.axelor.apps.base.module;

import com.axelor.app.AppSettings;
import com.axelor.apps.base.service.DateService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.printing.template.PrintingTemplateComputeNameServiceImpl;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.common.VersionUtils;
import com.axelor.meta.MetaFiles;
import com.axelor.script.ScriptPolicyConfigurator;
import com.axelor.utils.helpers.date.DurationHelper;
import java.util.List;

public class BaseCustomPolicy implements ScriptPolicyConfigurator {
  @Override
  public void configure(
      List<String> allowPackages,
      List<Class<?>> allowClasses,
      List<String> denyPackages,
      List<Class<?>> denyClasses) {
    allowClasses.add(AppBaseService.class);
    allowClasses.add(DurationHelper.class);
    allowClasses.add(AppSettings.class);
    allowClasses.add(MetaFiles.class);
    allowClasses.add(VersionUtils.class);
    allowClasses.add(DateService.class);
    allowClasses.add(ProductCompanyService.class);
    allowClasses.add(ReportSettings.class);
    allowPackages.add("groovy.tmp.templates.*");
    allowPackages.add("com.axelor.apps.base.service.printing.template");
    allowClasses.add(PrintingTemplateComputeNameServiceImpl.class);
    allowClasses.add(PrintingTemplateComputeNameServiceImpl.TranslationHelper.class);
  }
}
