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
package com.axelor.apps.base.service.printing.template;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PrintingTemplate;
import com.axelor.apps.base.db.PrintingTemplateLine;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.printing.template.model.PrintingGenFactoryContext;
import com.axelor.apps.base.service.printing.template.model.TemplatePrint;
import com.axelor.common.StringUtils;
import com.axelor.db.EntityHelper;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.utils.ThrowConsumer;
import com.axelor.utils.helpers.ModelHelper;
import com.axelor.utils.service.translation.TranslationBaseService;
import com.google.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class PrintingTemplatePrintServiceImpl implements PrintingTemplatePrintService {

  protected AppBaseService appBaseService;
  protected MetaFiles metaFiles;
  protected TranslationBaseService translationBaseService;
  protected PrintingTemplateComputeNameService templateComputeNameService;

  @Inject
  public PrintingTemplatePrintServiceImpl(
      AppBaseService appBaseService,
      MetaFiles metaFiles,
      TranslationBaseService translationBaseService,
      PrintingTemplateComputeNameService templateComputeNameService) {
    this.appBaseService = appBaseService;
    this.metaFiles = metaFiles;
    this.translationBaseService = translationBaseService;
    this.templateComputeNameService = templateComputeNameService;
  }

  @Override
  public String getPrintLink(PrintingTemplate template, PrintingGenFactoryContext context)
      throws AxelorException {
    return getPrintLink(template, context, getTemplateName(template));
  }

  @Override
  public String getPrintLink(
      PrintingTemplate template, PrintingGenFactoryContext context, String outputFileName)
      throws AxelorException {
    return getPrintLink(template, context, outputFileName, template.getToAttach());
  }

  @Override
  public String getPrintLink(
      PrintingTemplate template, PrintingGenFactoryContext context, boolean toAttach)
      throws AxelorException {
    return getPrintLink(template, context, getTemplateName(template), toAttach);
  }

  @Override
  public String getPrintLink(
      PrintingTemplate template,
      PrintingGenFactoryContext context,
      String outputFileName,
      boolean toAttach)
      throws AxelorException {
    File file = getPrintFile(template, context, outputFileName, toAttach);
    return PrintingTemplateHelper.getFileLink(file);
  }

  @Override
  public File getPrintFile(PrintingTemplate template, PrintingGenFactoryContext context)
      throws AxelorException {
    return getPrintFile(template, context, getTemplateName(template));
  }

  @Override
  public File getPrintFile(
      PrintingTemplate template, PrintingGenFactoryContext context, String outputFileName)
      throws AxelorException {
    return getPrintFile(template, context, outputFileName, template.getToAttach());
  }

  @Override
  public File getPrintFile(
      PrintingTemplate template, PrintingGenFactoryContext context, Boolean toAttach)
      throws AxelorException {
    return getPrintFile(template, context, getTemplateName(template), toAttach);
  }

  @Override
  public File getPrintFile(
      PrintingTemplate template,
      PrintingGenFactoryContext context,
      String outputFileName,
      Boolean toAttach)
      throws AxelorException {
    List<TemplatePrint> prints = getPrintList(template, context);
    return getPrintFile(template, prints, outputFileName, context, toAttach);
  }

  @Override
  public <T extends Model> String getPrintLinkForList(
      List<Integer> idList, Class<T> contextClass, PrintingTemplate template)
      throws IOException, AxelorException {
    List<File> printedRecords = new ArrayList<>();
    int errorCount =
        ModelHelper.apply(
            contextClass,
            idList,
            new ThrowConsumer<T, Exception>() {
              @Override
              public void accept(T item) throws Exception {
                try {
                  String name = translationBaseService.getValueTranslation(template.getName());
                  File printFile =
                      getPrintFile(
                          template,
                          new PrintingGenFactoryContext(EntityHelper.getEntity(item)),
                          name + "-" + item.getId());
                  printedRecords.add(printFile);
                } catch (Exception e) {
                  TraceBackService.trace(e);
                  throw e;
                }
              }
            });
    if (errorCount > 0 || CollectionUtils.isEmpty(printedRecords)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.FILE_COULD_NOT_BE_GENERATED));
    }
    File file = PrintingTemplateHelper.mergeToFile(printedRecords, template.getName());
    return PrintingTemplateHelper.getFileLink(file);
  }

  protected List<TemplatePrint> getPrintList(
      PrintingTemplate printingTemplate, PrintingGenFactoryContext context) throws AxelorException {
    List<TemplatePrint> prints = new ArrayList<>();
    List<PrintingTemplateLine> templateLines = printingTemplate.getPrintingTemplateLineList();
    templateLines.sort(Comparator.comparing(PrintingTemplateLine::getSequence));
    for (PrintingTemplateLine templateLine : templateLines) {
      PrintingGeneratorFactory factory = PrintingGeneratorFactory.getFactory(templateLine);
      TemplatePrint print = factory.generate(templateLine, context);
      prints.add(print);
    }
    return prints;
  }

  protected File getPrintFile(
      PrintingTemplate template,
      List<TemplatePrint> prints,
      String outputFileName,
      PrintingGenFactoryContext context,
      boolean toAttach)
      throws AxelorException {

    outputFileName = getPrintFileName(template, outputFileName, context);
    List<File> printFiles = getPrintFilesList(prints);
    File file = PrintingTemplateHelper.mergeToFile(printFiles, outputFileName);
    if (toAttach && context != null && context.getModel() != null) {
      try {
        metaFiles.attach(new FileInputStream(file), file.getName(), context.getModel());
      } catch (IOException e) {
        throw new AxelorException(
            e,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BaseExceptionMessage.FILE_COULD_NOT_BE_GENERATED));
      }
    }
    return file;
  }

  protected List<File> getPrintFilesList(List<TemplatePrint> prints) throws AxelorException {
    if (CollectionUtils.isEmpty(prints)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.FILE_COULD_NOT_BE_GENERATED));
    }

    return prints.stream()
        .map(TemplatePrint::getPrint)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  protected String getTemplateName(PrintingTemplate template) {
    if (StringUtils.notEmpty(template.getScriptFieldName())) {
      return template.getScriptFieldName();
    }
    return template.getName();
  }

  @Override
  public String getPrintFileName(PrintingTemplate template, PrintingGenFactoryContext context)
      throws AxelorException {
    return getPrintFileName(template, getTemplateName(template), context);
  }

  protected String getPrintFileName(
      PrintingTemplate template, String outputFileName, PrintingGenFactoryContext context)
      throws AxelorException {
    try {
      return templateComputeNameService.computeFileName(outputFileName, context);
    } catch (Exception e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.PRINTING_TEMPLATE_SCRIPT_ERROR),
          template.getName(),
          e.getMessage());
    }
  }
}
