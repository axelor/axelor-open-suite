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
package com.axelor.apps.base.service.message;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.service.birt.template.BirtTemplateService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.message.db.Template;
import com.axelor.message.service.MessageService;
import com.axelor.message.service.TemplateContextService;
import com.axelor.message.service.TemplateMessageServiceImpl;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.text.Templates;
import com.google.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplateMessageServiceBaseImpl extends TemplateMessageServiceImpl {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected BirtTemplateService birtTemplateService;

  @Inject
  public TemplateMessageServiceBaseImpl(
      MessageService messageService,
      TemplateContextService templateContextService,
      BirtTemplateService birtTemplateService) {
    super(messageService, templateContextService);
    this.birtTemplateService = birtTemplateService;
  }

  @Override
  public Set<MetaFile> getMetaFiles(
      Template template, Templates templates, Map<String, Object> templatesContext) {

    Set<MetaFile> metaFiles = super.getMetaFiles(template, templates, templatesContext);
    Set<BirtTemplate> birtTemplates = template.getBirtTemplateSet();
    if (CollectionUtils.isEmpty(birtTemplates)) {
      return metaFiles;
    }

    for (BirtTemplate birtTemplate : birtTemplates) {
      try {
        metaFiles.add(createMetaFileUsingBirtTemplate(birtTemplate, templatesContext));
      } catch (Exception e) {
        TraceBackService.traceExceptionFromSaveMethod(e);
        throw new IllegalStateException(e.getMessage(), e);
      }
    }

    logger.debug("Metafile to attach: {}", metaFiles);

    return metaFiles;
  }

  public MetaFile createMetaFileUsingBirtTemplate(
      BirtTemplate birtTemplate, Map<String, Object> templatesContext)
      throws AxelorException, IOException {

    logger.debug("Generate birt metafile: {}", birtTemplate.getName());

    String fileName =
        birtTemplate.getName()
            + "-"
            + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

    File file =
        birtTemplateService.generateBirtTemplateFile(
            birtTemplate, templatesContext, fileName, false, birtTemplate.getFormat());

    try (InputStream is = new FileInputStream(file)) {
      return Beans.get(MetaFiles.class).upload(is, fileName + "." + birtTemplate.getFormat());
    }
  }
}
