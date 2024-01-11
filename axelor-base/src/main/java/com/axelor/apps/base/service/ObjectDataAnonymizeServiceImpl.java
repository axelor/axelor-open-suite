/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.DataConfigLine;
import com.axelor.apps.base.db.ObjectDataConfig;
import com.axelor.apps.base.db.repo.DataConfigLineRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.AdminExceptionMessage;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.JPA;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.i18n.I18n;
import com.axelor.mail.db.repo.MailMessageRepository;
import com.axelor.meta.db.MetaField;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ObjectDataAnonymizeServiceImpl implements ObjectDataAnonymizeService {

  protected MailMessageRepository mailMessageRepo;

  @Inject
  public ObjectDataAnonymizeServiceImpl(MailMessageRepository mailMessageRepo) {
    this.mailMessageRepo = mailMessageRepo;
  }

  public void anonymize(ObjectDataConfig objectDataConfig, Long recordId) throws AxelorException {

    try {
      String rootModel = objectDataConfig.getModelSelect();

      for (DataConfigLine line : objectDataConfig.getDataConfigLineList()) {
        Class<? extends Model> modelClass =
            ObjectDataCommonService.findModelClass(line.getMetaModel());
        Query<? extends Model> query =
            ObjectDataCommonService.createQuery(recordId, line, modelClass);
        List<? extends Model> data = query.fetch();
        Mapper mapper = Mapper.of(modelClass);

        int reset = line.getResetPathSelect();
        if (reset != DataConfigLineRepository.RESET_NONE
            && line.getTypeSelect() == DataConfigLineRepository.TYPE_PATH) {

          String path = getPath(line);

          if (reset == DataConfigLineRepository.RESET_DELETE) {
            deleteLink(mapper, path, data);
          } else {
            replaceLink(mapper, path, data, rootModel, line.getRecordSelectId());
          }
        }

        deleteFields(line.getToDeleteMetaFieldSet(), mapper, data);
      }

    } catch (Exception e) {
      TraceBackService.trace(e);
      throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
    }
  }

  protected String getPath(DataConfigLine line) throws AxelorException {

    String metaModelName = line.getMetaModel().getName();

    switch (line.getTypeSelect()) {
      case DataConfigLineRepository.TYPE_PATH:
        MetaField metaFieldPath = line.getMetaFieldPath();
        if (metaFieldPath == null) {
          throw new AxelorException(
              line,
              TraceBackRepository.CATEGORY_NO_VALUE,
              I18n.get(AdminExceptionMessage.EMPTY_RELATIONAL_FIELD_IN_DATA_CONFIG_LINE),
              metaModelName);
        }
        return metaFieldPath.getName();

      case DataConfigLineRepository.TYPE_QUERY:
        String query = line.getPath();
        if (query == null) {
          throw new AxelorException(
              line,
              TraceBackRepository.CATEGORY_NO_VALUE,
              I18n.get(AdminExceptionMessage.EMPTY_QUERY_IN_DATA_CONFIG_LINE),
              metaModelName);
        }
        return query;

      default:
        throw new AxelorException(
            line, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, "Unknown case");
    }
  }

  @Transactional
  public void deleteLink(Mapper mapper, String path, List<? extends Model> data) {

    path = path.split("\\.")[0];
    Property property = mapper.getProperty(path);
    if (property == null || property.isRequired()) {
      return;
    }
    for (Model model : data) {
      mapper.set(model, path, null);
      JPA.save(model);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  public void replaceLink(
      Mapper mapper, String path, List<? extends Model> data, String rootModel, Long recordValue)
      throws ClassNotFoundException, AxelorException {

    path = path.split("\\.")[0];
    Property property = mapper.getProperty(path);
    if (property == null) {
      return;
    }
    Class<? extends Model> klass = (Class<? extends Model>) Class.forName(rootModel);

    JpaRepository<? extends Model> repo = JpaRepository.of(klass);
    Model object = repo.all().filter("self.id = ?1", recordValue).fetchOne();
    if (object == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(AdminExceptionMessage.OBJECT_DATA_REPLACE_MISSING),
          recordValue);
    }
    for (Model model : data) {
      mapper.set(model, path, object);
      JPA.save(model);
    }
  }

  @Transactional
  public void deleteFields(Set<MetaField> fields, Mapper mapper, List<? extends Model> data) {

    if (fields.isEmpty()) {
      return;
    }

    Map<String, Object> defaultValues = getDefaultValues(mapper, fields);

    for (Model model : data) {
      for (Entry<String, Object> fieldEntry : defaultValues.entrySet()) {
        mapper.set(model, fieldEntry.getKey(), fieldEntry.getValue());
      }
      JPA.save(model);

      mailMessageRepo
          .all()
          .filter(
              "self.relatedId = ?1 AND self.relatedModel = ?2",
              model.getId(),
              mapper.getBeanClass().getName())
          .delete();
    }
  }

  private Map<String, Object> getDefaultValues(Mapper mapper, Set<MetaField> fields) {

    Map<String, Object> defaultValues = new HashMap<>();

    LocalDate defaultDate = LocalDate.of(1900, 01, 01);

    for (MetaField field : fields) {

      String name = field.getName();
      Property property = mapper.getProperty(name);

      if (!property.isRequired()) {
        defaultValues.put(name, null);
        continue;
      }
      if (property.getTarget() != null) {
        continue;
      }

      switch (field.getTypeName()) {
        case "String":
          {
            defaultValues.put(name, "anonymous");
            break;
          }
        case "BigDecimal":
          {
            defaultValues.put(name, BigDecimal.ZERO);
            break;
          }
        case "Integer":
          {
            defaultValues.put(name, Integer.valueOf(0));
            break;
          }
        case "Boolean":
          {
            defaultValues.put(name, Boolean.FALSE);
            break;
          }
        case "Long":
          {
            defaultValues.put(name, Long.valueOf(0));
            break;
          }
        case "LocalTime":
          {
            defaultValues.put(name, LocalTime.MIDNIGHT);
            break;
          }
        case "LocalDateTime":
          {
            defaultValues.put(name, defaultDate.atStartOfDay());
            break;
          }
        case "ZonedDateTime":
          {
            defaultValues.put(name, defaultDate.atStartOfDay(ZoneId.systemDefault()));
            break;
          }
        default:
          {
            break;
          }
      }
    }

    return defaultValues;
  }
}
