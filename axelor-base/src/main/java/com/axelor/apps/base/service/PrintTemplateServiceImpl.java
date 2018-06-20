package com.axelor.apps.base.service;

import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.db.Print;
import com.axelor.apps.base.db.PrintLine;
import com.axelor.apps.base.db.PrintTemplate;
import com.axelor.apps.base.db.PrintTemplateLine;
import com.axelor.apps.base.db.repo.PrintRepository;
import com.axelor.apps.base.service.message.TemplateMessageServiceBaseImpl;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.tool.template.TemplateMaker;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrintTemplateServiceImpl implements PrintTemplateService {

  private final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final char TEMPLATE_DELIMITER = '$';

  protected PrintRepository printRepo;
  protected PrintService printService;
  protected TemplateMessageServiceBaseImpl templateMessageService;

  @Inject
  public PrintTemplateServiceImpl(
      PrintRepository printRepo,
      PrintService printService,
      TemplateMessageServiceBaseImpl templateMessageService) {
    this.printRepo = printRepo;
    this.printService = printService;
    this.templateMessageService = templateMessageService;
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

    for (PrintTemplateLine printTemplateLine : printTemplate.getPrintTemplateLineList()) {
      String title = null;
      String content = null;

      if (!Strings.isNullOrEmpty(printTemplateLine.getTitle())) {
        maker.setTemplate(printTemplateLine.getTitle());
        title = maker.make();
      }

      if (!Strings.isNullOrEmpty(printTemplateLine.getContent())) {
        maker.setTemplate(printTemplateLine.getContent());
        content = maker.make();
      }

      PrintLine printLine = new PrintLine();
      printLine.setTitle(title);
      printLine.setContent(content);
      print.addPrintLineListItem(printLine);
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
