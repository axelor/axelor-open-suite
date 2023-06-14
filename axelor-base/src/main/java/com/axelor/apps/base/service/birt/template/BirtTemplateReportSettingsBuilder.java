/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.birt.template;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.db.BirtTemplateParameter;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.repo.TemplateRepository;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.text.GroovyTemplates;
import com.axelor.text.StringTemplates;
import com.axelor.text.Templates;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.birt.core.data.DataTypeUtil;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;

class BirtTemplateReportSettingsBuilder {

  private ReportSettings settings;
  private BirtTemplate template;
  private Model model;
  private Map<String, Object> context;

  public BirtTemplateReportSettingsBuilder(BirtTemplate template, String outputName) {
    this.template = template;
    this.context = new HashMap<>();
    this.settings = initReportSettings(outputName);
  }

  private ReportSettings initReportSettings(String outputName) {
    String designPath = this.template.getTemplateLink();
    MetaFile templateMetaFile = this.template.getTemplateMetaFile();
    if (templateMetaFile != null) {
      designPath = MetaFiles.getPath(templateMetaFile).toString();
    }
    return ReportFactory.createReport(designPath, outputName);
  }

  public BirtTemplateReportSettingsBuilder addParam(String key, String value, String type)
      throws BirtException {
    settings.addParam(key, convertValue(type, value));
    return this;
  }

  public BirtTemplateReportSettingsBuilder addInContext(Model model) {
    this.model = model;
    String klassName = model.getClass().getSimpleName();
    return addInContext(klassName, model);
  }

  public BirtTemplateReportSettingsBuilder addInContext(String tag, Object value) {
    this.context.put(tag, value);
    return this;
  }

  public BirtTemplateReportSettingsBuilder addInContext(Map<String, Object> context) {
    this.context.putAll(context);
    return this;
  }

  public BirtTemplateReportSettingsBuilder toAttach(Boolean toAttach) {
    if (Boolean.TRUE.equals(toAttach) && model != null) {
      settings.toAttach(model);
    }
    return this;
  }

  public BirtTemplateReportSettingsBuilder withFormat(String format) {
    settings.addFormat(format);
    return this;
  }

  public ReportSettings build() throws AxelorException {
    computeBirtParameters();
    settings.generate();
    return settings;
  }

  private void computeBirtParameters() throws AxelorException {
    List<BirtTemplateParameter> birtTemplateParameterList = template.getBirtTemplateParameterList();
    Templates templates = getTemplateEngine();

    for (BirtTemplateParameter birtTemplateParameter : birtTemplateParameterList) {

      try {
        String parseValue =
            templates.fromText(birtTemplateParameter.getValue()).make(context).render();
        settings.addParam(
            birtTemplateParameter.getName(),
            convertValue(birtTemplateParameter.getType(), parseValue));
      } catch (BirtException e) {
        throw new AxelorException(
            e,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BaseExceptionMessage.TEMPLATE_MESSAGE_BASE_2));
      }
    }
  }

  private Templates getTemplateEngine() {
    Templates templates = new StringTemplates('$', '$');
    if (template.getTemplateEngineSelect() == TemplateRepository.TEMPLATE_ENGINE_GROOVY_TEMPLATE) {
      templates = Beans.get(GroovyTemplates.class);
    }
    return templates;
  }

  private Object convertValue(String type, String value) throws BirtException {

    if (DesignChoiceConstants.PARAM_TYPE_BOOLEAN.equals(type)) {
      return DataTypeUtil.toBoolean(value);
    } else if (DesignChoiceConstants.PARAM_TYPE_DATETIME.equals(type)) {
      return DataTypeUtil.toDate(value);
    } else if (DesignChoiceConstants.PARAM_TYPE_DATE.equals(type)) {
      return DataTypeUtil.toSqlDate(value);
    } else if (DesignChoiceConstants.PARAM_TYPE_TIME.equals(type)) {
      return DataTypeUtil.toSqlTime(value);
    } else if (DesignChoiceConstants.PARAM_TYPE_DECIMAL.equals(type)) {
      return DataTypeUtil.toBigDecimal(value);
    } else if (DesignChoiceConstants.PARAM_TYPE_FLOAT.equals(type)) {
      return DataTypeUtil.toDouble(value);
    } else if (DesignChoiceConstants.PARAM_TYPE_STRING.equals(type)) {
      return DataTypeUtil.toLocaleNeutralString(value);
    } else if (DesignChoiceConstants.PARAM_TYPE_INTEGER.equals(type)) {
      return DataTypeUtil.toInteger(value);
    }
    return value;
  }
}
