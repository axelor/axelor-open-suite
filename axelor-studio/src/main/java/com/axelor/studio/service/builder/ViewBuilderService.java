/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.studio.service.builder;

import com.axelor.common.Inflector;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.ObjectViews;
import com.axelor.meta.schema.views.AbstractView;
import com.axelor.meta.schema.views.AbstractWidget;
import com.axelor.meta.schema.views.Field;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.ViewItem;
import com.axelor.studio.db.repo.MetaJsonFieldRepo;
import com.axelor.studio.exception.IExceptionMessage;
import com.axelor.studio.service.StudioMetaService;
import com.google.inject.Inject;
import java.io.StringWriter;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViewBuilderService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject private MetaFieldRepository metaFieldRepo;

  @Inject private MetaJsonFieldRepo metaJsonFieldRepo;

  @Inject private MetaModelRepository metaModelRepo;

  @Inject private MetaViewRepository metaViewRepo;

  @Inject private ModelBuilderService modelBuilderService;

  private static final List<String> DATE_TYPES =
      Arrays.asList(
          new String[] {
            "date", "datetime", "LocalDate", "LocalDateTime", "ZonnedDateTime",
          });

  private static final List<String> NUMBER_TYPES =
      Arrays.asList(new String[] {"integer", "Integer", "Long", "decimal", "BigDecimal"});

  public AbstractView processCommon(AbstractView view, ViewBuilder viewBuilder, String module)
      throws AxelorException {

    view.setName(viewBuilder.getName());
    view.setTitle(viewBuilder.getTitle());
    view.setModel(getModelName(viewBuilder, module));
    if (module != null) {
      view.setXmlId(module + "-" + viewBuilder.getName());
    }

    return view;
  }

  public String getModelName(ViewBuilder viewBuilder, String moduleName) throws AxelorException {

    String modelName = viewBuilder.getModel();

    if (modelName == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.MODEL_NOT_FOUND),
          "");
    }

    if (viewBuilder.getIsJson()) {
      if (moduleName != null) {
        modelName = modelBuilderService.getModelFullName(moduleName, modelName);
      }
    } else {
      MetaModel metaModel = metaModelRepo.findByName(modelName);
      if (metaModel == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(IExceptionMessage.MODEL_NOT_FOUND),
            modelName);
      }
      modelName = metaModel.getFullName();
    }

    return modelName;
  }

  public String createXml(ObjectViews objectViews) throws JAXBException {

    StringWriter writer = new StringWriter();
    XMLViews.marshal(objectViews, writer);

    return writer.toString();
  }

  public String[] getJsonField(String model, String name) throws AxelorException {

    MetaJsonField jsonField =
        metaJsonFieldRepo
            .all()
            .filter("self.jsonModel.name = ?1 and self.name = ?2", model, name)
            .fetchOne();

    if (jsonField == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.FIELD_NOT_FOUND),
          name,
          model);
    }

    String fieldName = jsonField.getName();
    String type = jsonField.getType();
    Boolean isImage =
        jsonField.getType().equals("many-to-one")
            && jsonField.getTargetModel().equals(MetaFiles.class.getName());
    if (isImage) {
      type = "image";
    }
    if (!isImage && jsonField.getType().contains("-to-")) {
      fieldName += "." + getJsonNameColumn(jsonField);
    }

    return new String[] {fieldName, jsonField.getTitle(), type};
  }

  public String[] getMetaField(String model, String name) throws AxelorException {

    MetaField metaField =
        metaFieldRepo
            .all()
            .filter("self.metaModel.name = ?1 and self.name = ?2", model, name)
            .fetchOne();
    if (metaField == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.FIELD_NOT_FOUND),
          name,
          model);
    }

    String fieldName = metaField.getName();

    String type = metaField.getTypeName();
    Boolean isImage = type.equals(MetaFile.class.getSimpleName());

    if (isImage) {
      type = "image";
      log.debug("Image field: {}, model: {}", name, model);
    }

    if (!isImage && metaField.getRelationship() != null) {
      fieldName += "." + getNameColumn(metaField.getTypeName());
    }

    return new String[] {fieldName, metaField.getLabel(), type};
  }

  public List<AbstractWidget> getItems(ViewBuilder viewBuilder) throws AxelorException {

    List<AbstractWidget> fields = new ArrayList<>();

    for (ViewItem viewItem : viewBuilder.getViewItemList()) {
      Field field = new Field();
      String[] fieldData = null;
      if (viewBuilder.getIsJson()) {
        fieldData = getJsonField(viewBuilder.getModel(), viewItem.getName());
      } else {
        fieldData = getMetaField(viewBuilder.getModel(), viewItem.getName());
      }

      if (NUMBER_TYPES.contains(fieldData[2])) {
        Map<QName, String> otherAttributes = new HashMap<>();
        otherAttributes.put(new QName("x-scale"), "2");
        field.setOtherAttributes(otherAttributes);
      }
      field.setName(viewItem.getName());
      fields.add(field);
    }

    return fields;
  }

  public void sortBySequence(List<ViewItem> viewItems) {

    if (viewItems == null) {
      return;
    }

    viewItems.sort(
        new Comparator<ViewItem>() {

          @Override
          public int compare(ViewItem item1, ViewItem item2) {
            return item1.getSequence().compareTo(item2.getSequence());
          }
        });
  }

  public String getNameColumn(String modelName) {

    try {
      if (modelName == null) {
        return "id";
      }

      if (!modelName.contains(".")) {
        MetaModel metaModel = metaModelRepo.findByName(modelName);
        if (metaModel == null) {
          return "id";
        }
        modelName = metaModel.getFullName();
      }

      Mapper mapper = Mapper.of(Class.forName(modelName));
      boolean nameField = false;
      boolean codeField = false;
      for (Property property : Arrays.asList(mapper.getProperties())) {
        if (property.isNameColumn()) {
          return property.getName();
        } else if (property.getName().equals("name")) {
          nameField = true;
        } else if (property.getName().equals("code")) {
          codeField = true;
        }
      }
      if (nameField) {
        return "name";
      }
      if (codeField) {
        return "code";
      }
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    return "id";
  }

  public String getJsonNameColumn(MetaJsonField metaJsonField) {

    log.debug("Finding nameColumn for json field: {}", metaJsonField);

    if (metaJsonField.getType().startsWith("json-")) {
      MetaJsonModel metaJsonModel = metaJsonField.getTargetJsonModel();
      String nameField = metaJsonModel.getNameField();
      if (nameField != null) {
        return nameField;
      }
    } else {
      return getNameColumn(metaJsonField.getTargetModel());
    }

    return "id";
  }

  public Object getFormatted(String name, String type) {

    if (DATE_TYPES.contains(type) || NUMBER_TYPES.contains(type)) {
      return "$fmt('" + name + "')";
    }

    return name;
  }

  public void genereateMetaView(ViewBuilder viewBuilder, String module) throws AxelorException {

    if (viewBuilder == null || viewBuilder.getIsJson()) {
      return;
    }

    AbstractView view = null;
    switch (viewBuilder.getViewType()) {
      case "kanban":
        view = Beans.get(KanbanBuilderService.class).build(viewBuilder, module);
        break;
      case "calendar":
        view = Beans.get(CalendarBuilderService.class).build(viewBuilder, module);
        break;
      case "cards":
        view = Beans.get(CardsBuilderService.class).build(viewBuilder, module);
        break;
    }

    if (view != null) {
      MetaView metaView = Beans.get(StudioMetaService.class).generateMetaView(view, module);
      viewBuilder.setMetaViewGenerated(metaView);
    }
  }

  public String getViewTitle(String viewName) {

    MetaView metaView = metaViewRepo.findByName(viewName);

    if (metaView != null) {
      return metaView.getTitle();
    }

    return viewName;
  }

  public String getDefaultViewName(String type, String simpleModelName) {

    return Inflector.getInstance().dasherize(simpleModelName) + "-" + type;
  }
}
