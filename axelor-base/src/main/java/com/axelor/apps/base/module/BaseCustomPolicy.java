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
    allowPackages.add("com.axelor.utils.helpers.*");
    allowClasses.add(PrintingTemplateComputeNameServiceImpl.class);
    allowClasses.add(PrintingTemplateComputeNameServiceImpl.TranslationHelper.class);
  }
}
