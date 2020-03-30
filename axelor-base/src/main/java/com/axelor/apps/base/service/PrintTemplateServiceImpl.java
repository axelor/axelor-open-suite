/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.db.Print;
import com.axelor.apps.base.db.PrintLine;
import com.axelor.apps.base.db.PrintTemplate;
import com.axelor.apps.base.db.PrintTemplateLine;
import com.axelor.apps.base.db.repo.PrintRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.message.TemplateMessageServiceBaseImpl;
import com.axelor.apps.message.service.TemplateContextService;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.Context;
import com.axelor.tool.template.TemplateMaker;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrintTemplateServiceImpl implements PrintTemplateService {

  private final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final char TEMPLATE_DELIMITER = '$';
  private static final String TEMPLATE_CONTEXT_SEQUENCE_KEY_PREFIX = "Sequence_";

  protected PrintRepository printRepo;
  protected PrintService printService;
  protected TemplateMessageServiceBaseImpl templateMessageService;
  protected TemplateContextService templateContextService;
  protected int rank;

  @Inject
  public PrintTemplateServiceImpl(
      PrintRepository printRepo,
      PrintService printService,
      TemplateMessageServiceBaseImpl templateMessageService,
      TemplateContextService templateContextService) {
    this.printRepo = printRepo;
    this.printService = printService;
    this.templateMessageService = templateMessageService;
    this.templateContextService = templateContextService;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Map<String, Object> generatePrint(
      Long objectId, String model, String simpleModel, PrintTemplate printTemplate)
      throws AxelorException, IOException, ClassNotFoundException {
    MetaModel metaModel = printTemplate.getMetaModel();
    if (metaModel == null) {
      return null;
    }

    // debug
    LOG.debug("");
    LOG.debug("model : {}", model);
    LOG.debug("simpleModel : {}", simpleModel);
    LOG.debug("object id : {}", objectId);
    LOG.debug("printTemplate : {}", printTemplate);
    LOG.debug("");

    TemplateMaker maker = initMaker(objectId, model, simpleModel);

    Print print = new Print();
    print.setMetaModel(metaModel);
    print.setCompany(printTemplate.getCompany());
    print.setPrintSettings(printTemplate.getPrintSettings());
    print.setLanguage(printTemplate.getLanguage());
    print.setHidePrintSettings(printTemplate.getHidePrintSettings());
    print.setFormatSelect(printTemplate.getFormatSelect());
    print.setDisplayTypeSelect(printTemplate.getDisplayTypeSelect());

    Context scriptContext = new Context(Mapper.toMap(metaModel), metaModel.getClass());

    rank = 0;
    int level = 1;

    if (CollectionUtils.isNotEmpty(printTemplate.getPrintTemplateLineList())) {
      processPrintTemplateLineList(
          printTemplate.getPrintTemplateLineList(), print, null, scriptContext, maker, level);
    }

    printRepo.save(print);

    printService.attachMetaFiles(print, getMetaFiles(maker, printTemplate));

    return ActionView.define("Create print")
        .model(Print.class.getName())
        .add("form", "print-form")
        .param("forceEdit", "true")
        .context("_showRecord", print.getId().toString())
        .map();
  }

  private void processPrintTemplateLineList(
      List<PrintTemplateLine> templateLineList,
      Print print,
      PrintLine parent,
      Context scriptContext,
      TemplateMaker maker,
      int level)
      throws AxelorException {
    int seq = 0;
    for (PrintTemplateLine printTemplateLine : templateLineList) {
      boolean present = true;
      if (StringUtils.notEmpty(printTemplateLine.getConditions())) {
        Object evaluation =
            templateContextService.computeTemplateContext(
                printTemplateLine.getConditions(), scriptContext);
        if (evaluation instanceof Boolean) {
          present = (Boolean) evaluation;
        } else {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              IExceptionMessage.PRINT_TEMPLATE_CONDITIONS_MUST_BE_BOOLEAN);
        }
      }

      if (present) {
        ++seq;
        ++rank;
        String title = null;
        String content = null;

        if (!Strings.isNullOrEmpty(printTemplateLine.getTitle())) {
          maker.setTemplate(printTemplateLine.getTitle());
          addSequencesInContext(maker, level, seq, parent);
          title = maker.make();
        }

        if (!Strings.isNullOrEmpty(printTemplateLine.getContent())) {
          maker.setTemplate(printTemplateLine.getContent());
          addSequencesInContext(maker, level, seq, parent);
          content = maker.make();
        }

        PrintLine printLine = new PrintLine();
        printLine.setRank(rank);
        printLine.setSequence(seq);
        printLine.setTitle(title);
        printLine.setContent(content);
        printLine.setIsEditable(printTemplateLine.getIsEditable());
        printLine.setParent(parent);

        if (CollectionUtils.isNotEmpty(printTemplateLine.getPrintTemplateLineList())) {
          processPrintTemplateLineList(
              printTemplateLine.getPrintTemplateLineList(),
              print,
              printLine,
              scriptContext,
              maker,
              level + 1);
        }

        print.addPrintLineListItem(printLine);
      }
    }
  }

  private void addSequencesInContext(TemplateMaker maker, int level, int seq, PrintLine parent) {
    maker.addInContext(TEMPLATE_CONTEXT_SEQUENCE_KEY_PREFIX + level, seq);
    if (ObjectUtils.notEmpty(parent)) {
      addSequencesInContext(maker, level - 1, parent.getSequence(), parent.getParent());
    }
  }

  @SuppressWarnings("unchecked")
  private TemplateMaker initMaker(Long objectId, String model, String simpleModel)
      throws ClassNotFoundException {
    TemplateMaker maker = new TemplateMaker(Locale.FRENCH, TEMPLATE_DELIMITER, TEMPLATE_DELIMITER);

    Class<? extends Model> myClass = (Class<? extends Model>) Class.forName(model);
    maker.setContext(JPA.find(myClass, objectId), simpleModel);

    return maker;
  }

  public Set<MetaFile> getMetaFiles(TemplateMaker maker, PrintTemplate printTemplate)
      throws AxelorException, IOException {
    Set<MetaFile> metaFiles = new HashSet<>();
    if (printTemplate.getBirtTemplateSet() == null) {
      return metaFiles;
    }

    for (BirtTemplate birtTemplate : printTemplate.getBirtTemplateSet()) {
      metaFiles.add(templateMessageService.createMetaFileUsingBirtTemplate(maker, birtTemplate));
    }

    LOG.debug("MetaFile to attach: {}", metaFiles);

    return metaFiles;
  }
}
