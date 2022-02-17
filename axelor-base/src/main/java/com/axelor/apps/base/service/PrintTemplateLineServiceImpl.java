/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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

import com.axelor.app.internal.AppFilter;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Language;
import com.axelor.apps.base.db.PrintTemplateLine;
import com.axelor.apps.base.db.PrintTemplateLineTest;
import com.axelor.apps.base.db.repo.PrintTemplateLineRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.message.service.TemplateContextService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaSelect;
import com.axelor.meta.db.MetaSelectItem;
import com.axelor.meta.db.repo.MetaSelectItemRepository;
import com.axelor.meta.db.repo.MetaSelectRepository;
import com.axelor.rpc.Context;
import com.axelor.tool.template.TemplateMaker;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class PrintTemplateLineServiceImpl implements PrintTemplateLineService {

  protected static final char TEMPLATE_DELIMITER = '$';

  protected TemplateContextService templateContextService;
  protected PrintTemplateLineRepository printTemplateLineRepo;

  @Inject
  public PrintTemplateLineServiceImpl(
      TemplateContextService templateContextService,
      PrintTemplateLineRepository printTemplateLineRepo) {
    this.templateContextService = templateContextService;
    this.printTemplateLineRepo = printTemplateLineRepo;
  }

  @SuppressWarnings("unchecked")
  @Transactional
  @Override
  public void checkExpression(
      Long objectId, MetaModel metaModel, PrintTemplateLine printTemplateLine)
      throws ClassNotFoundException, AxelorException {
    if (metaModel == null) {
      return;
    }
    String model = metaModel.getFullName();
    String simpleModel = metaModel.getName();
    PrintTemplateLineTest test = printTemplateLine.getPrintTemplateLineTest();

    Context scriptContext = null;
    if (StringUtils.notEmpty(model)) {
      Class<? extends Model> modelClass = (Class<? extends Model>) Class.forName(model);
      Model modelObject = JPA.find(modelClass, objectId);
      if (ObjectUtils.notEmpty(modelObject)) {
        scriptContext = new Context(Mapper.toMap(modelObject), modelClass);
      }
    }

    String resultOfTitle = null;
    String resultOfContent = null;

    Locale locale =
        Optional.ofNullable(printTemplateLine.getPrintTemplate().getLanguage())
            .map(Language::getCode)
            .map(Locale::new)
            .orElseGet(AppFilter::getLocale);
    TemplateMaker maker = initMaker(objectId, model, simpleModel, locale);

    try {
      if (StringUtils.notEmpty(printTemplateLine.getTitle())) {
        maker.setTemplate(printTemplateLine.getTitle());
        resultOfTitle = maker.make();
      }
    } catch (Exception e) {
      resultOfTitle = "Error in title";
    }

    try {
      if (StringUtils.notEmpty(printTemplateLine.getContent())) {
        maker.setTemplate(printTemplateLine.getContent());
        resultOfContent = maker.make();
      }
    } catch (Exception e) {
      resultOfContent = "Error in content";
    }

    test.setContentResult(resultOfTitle + "<br>" + resultOfContent);

    Boolean present = Boolean.TRUE;
    if (StringUtils.notEmpty(printTemplateLine.getConditions())) {
      Object evaluation = null;
      try {
        evaluation =
            templateContextService.computeTemplateContext(
                printTemplateLine.getConditions(), scriptContext);
      } catch (Exception e) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.PRINT_TEMPLATE_CONDITION_MUST_BE_BOOLEAN));
      }
      if (evaluation instanceof Boolean) {
        present = (Boolean) evaluation;
      }
    }
    test.setConditionsResult(present);

    printTemplateLineRepo.save(printTemplateLine);
  }

  @SuppressWarnings("unchecked")
  protected TemplateMaker initMaker(Long objectId, String model, String simpleModel, Locale locale)
      throws ClassNotFoundException {
    String timezone = null;
    Company activeCompany =
        Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null);
    if (activeCompany != null) {
      timezone = activeCompany.getTimezone();
    }
    TemplateMaker maker =
        new TemplateMaker(timezone, locale, TEMPLATE_DELIMITER, TEMPLATE_DELIMITER);

    Class<? extends Model> modelClass = (Class<? extends Model>) Class.forName(model);
    maker.setContext(JPA.find(modelClass, objectId), simpleModel);

    return maker;
  }

  @Override
  public void addItemToReferenceSelection(MetaModel model) {
    MetaSelect metaSelect =
        Beans.get(MetaSelectRepository.class)
            .findByName("print.template.line.test.reference.select");
    List<MetaSelectItem> items = metaSelect.getItems();
    if (items != null && !items.stream().anyMatch(x -> x.getValue().equals(model.getFullName()))) {
      MetaSelectItem metaSelectItem = new MetaSelectItem();
      metaSelectItem.setTitle(model.getName());
      metaSelectItem.setValue(model.getFullName());
      metaSelectItem.setSelect(metaSelect);
      saveMetaSelectItem(metaSelectItem);
    }
  }

  @Transactional
  public void saveMetaSelectItem(MetaSelectItem metaSelectItem) {
    Beans.get(MetaSelectItemRepository.class).save(metaSelectItem);
  }
}
