/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.message.service;

import com.axelor.app.internal.AppFilter;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.exception.IExceptionMessage;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.event.Observes;
import com.axelor.events.StartupEvent;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaSelect;
import com.axelor.meta.db.MetaSelectItem;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.db.repo.MetaSelectItemRepository;
import com.axelor.meta.db.repo.MetaSelectRepository;
import com.axelor.tool.template.TemplateMaker;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Singleton
public class TemplateService {

  public static final String ALL_MODEL_SELECT = "all.model.reference.select";

  public void checkTargetReceptor(Template template) throws AxelorException {
    String target = template.getTarget();
    MetaModel metaModel = template.getMetaModel();

    if (Strings.isNullOrEmpty(target)) {
      return;
    }
    if (metaModel == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.TEMPLATE_SERVICE_1));
    }

    try {
      this.validTarget(target, metaModel);
    } catch (Exception e) {
      throw new AxelorException(
          e.getCause(),
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.TEMPLATE_SERVICE_2));
    }
  }

  private void validTarget(String target, MetaModel metaModel) throws ClassNotFoundException {
    Iterator<String> iter = Splitter.on(".").split(target).iterator();
    Property p = Mapper.of(Class.forName(metaModel.getFullName())).getProperty(iter.next());
    while (iter.hasNext() && p != null) {
      p = Mapper.of(p.getTarget()).getProperty(iter.next());
    }

    if (p == null) {
      throw new IllegalArgumentException();
    }
  }

  public String processSubject(
      String timeZone,
      Template template,
      Model bean,
      String beanName,
      Map<String, Object> context) {
    TemplateMaker maker = new TemplateMaker(timeZone, AppFilter.getLocale(), '$', '$');

    maker.setTemplate(template.getSubject());
    maker.setContext(bean, context, beanName);
    return maker.make();
  }

  public String processContent(
      String timeZone,
      Template template,
      Model bean,
      String beanName,
      Map<String, Object> context) {
    TemplateMaker maker = new TemplateMaker(timeZone, AppFilter.getLocale(), '$', '$');

    maker.setTemplate(template.getContent());
    maker.setContext(bean, context, beanName);
    return maker.make();
  }

  @Transactional
  public void createSelectionOfAllModels(@Observes StartupEvent event) {

    MetaSelectRepository metaSelectRepo = Beans.get(MetaSelectRepository.class);
    MetaModelRepository metaModelRepo = Beans.get(MetaModelRepository.class);
    MetaSelectItemRepository metaSelectItemRepo = Beans.get(MetaSelectItemRepository.class);

    MetaSelect allModelSelect = metaSelectRepo.findByName(ALL_MODEL_SELECT);

    if (allModelSelect != null) {
      List<MetaModel> addDifferences =
          metaModelRepo
              .all()
              .filter(
                  "self.fullName NOT IN (SELECT item.value FROM MetaSelectItem item WHERE item.select = :selectId)")
              .bind("selectId", allModelSelect.getId())
              .fetch();
      if (!addDifferences.isEmpty()) {
        addMetaSelectItems(addDifferences, allModelSelect);
      }
      List<MetaSelectItem> removeDifferences =
          metaSelectItemRepo
              .all()
              .filter(
                  "self.select = :selectId AND self.value NOT IN (SELECT model.fullName FROM MetaModel model)")
              .bind("selectId", allModelSelect.getId())
              .fetch();
      if (!removeDifferences.isEmpty()) {
        allModelSelect.getItems().removeAll(removeDifferences);
      }
    } else {
      allModelSelect = new MetaSelect();
      allModelSelect.setName(ALL_MODEL_SELECT);
      List<MetaModel> metaModelList = metaModelRepo.all().order("name").fetch();
      addMetaSelectItems(metaModelList, allModelSelect);
    }

    metaSelectRepo.save(allModelSelect);
  }

  public void addMetaSelectItems(List<MetaModel> metaModelList, MetaSelect metaSelect) {
    for (MetaModel model : metaModelList) {
      MetaSelectItem item = new MetaSelectItem();
      item.setTitle(model.getName());
      item.setValue(model.getFullName());
      item.setSelect(metaSelect);
      metaSelect.addItem(item);
    }
  }

  @SuppressWarnings("unchecked")
  public Message generateDraftMessage(Template template, MetaModel metaModel, String referenceId)
      throws ClassNotFoundException, AxelorException {

    if (metaModel == null) {
      return null;
    }
    String model = metaModel.getFullName();
    Model modelObject = null;

    if (StringUtils.notEmpty(model)) {
      Class<? extends Model> modelClass = (Class<? extends Model>) Class.forName(model);
      modelObject = JPA.find(modelClass, Long.valueOf(referenceId));
    }
    try {
      return Beans.get(TemplateMessageService.class).generateMessage(modelObject, template, true);
    } catch (InstantiationException | IllegalAccessException | IOException e) {
      TraceBackService.trace(e);
    }
    return null;
  }
}
