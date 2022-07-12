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
package com.axelor.apps.base.service.advancedExport;

import com.axelor.apps.base.db.AdvancedExport;
import com.axelor.apps.base.db.AdvancedExportLine;
import com.axelor.apps.base.db.repo.AdvancedExportLineRepository;
import com.axelor.apps.base.db.repo.AdvancedExportRepository;
import com.axelor.apps.tool.MetaTool;
import com.axelor.common.StringUtils;
import com.axelor.db.EntityHelper;
import com.axelor.db.JPA;
import com.axelor.db.JpaRepository;
import com.axelor.db.JpaSecurity;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.db.mapper.PropertyType;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.schema.views.Selection.Option;
import com.axelor.rpc.filter.Filter;
import com.google.common.base.Splitter;
import com.google.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.StringJoiner;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdvancedExportServiceImpl implements AdvancedExportService {

  private static final Logger log = LoggerFactory.getLogger(AdvancedExportServiceImpl.class);

  @Inject private MetaFieldRepository metaFieldRepository;

  @Inject private AdvancedExportGeneratorFactory exportGeneratorFactory;

  private String exportFileName;
  private boolean isReachMaxExportLimit = false;

  @SuppressWarnings("unchecked")
  @Override
  public Filter getJpaSecurityFilter(MetaModel metaModel) {
    JpaSecurity jpaSecurity = Beans.get(JpaSecurity.class);
    try {
      return jpaSecurity.getFilter(
          JpaSecurity.CAN_EXPORT,
          (Class<? extends Model>) Class.forName(metaModel.getFullName()),
          (Long) null);
    } catch (ClassNotFoundException e) {
      log.error(e.getMessage());
    }
    return null;
  }

  private List<AdvancedExportLine> sortAdvancedExportLineList(
      List<AdvancedExportLine> advancedExportLineList) {

    advancedExportLineList.sort(
        new Comparator<AdvancedExportLine>() {

          @Override
          public int compare(AdvancedExportLine line1, AdvancedExportLine line2) {
            if (line1.getSequence().equals(line2.getSequence())) {
              return line1.getId().compareTo(line2.getId());
            }
            return line1.getSequence() - line2.getSequence();
          }
        });
    return advancedExportLineList;
  }

  /**
   * Initialize the object of <i>AdvancedExportGenerator</i> based on file type and generate the
   * export file.
   *
   * @throws AxelorException
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public File export(AdvancedExport advancedExport, List<Long> recordIds, String fileType)
      throws AxelorException {

    AdvancedExportGenerator exportGenerator =
        exportGeneratorFactory.getAdvancedExportGenerator(advancedExport, fileType);

    sortAdvancedExportLineList(advancedExport.getAdvancedExportLineList());

    List<List> outputList = new ArrayList<>();
    List<? extends Model> modelList;
    int offset = 0;
    int maxExportLimit = advancedExport.getMaxExportLimit();
    List<AdvancedExportLine> advancedExportLineList = advancedExport.getAdvancedExportLineList();
    int queryFetchSize = advancedExport.getQueryFetchSize();

    try {
      Class<? extends Model> klass =
          (Class<? extends Model>) Class.forName(advancedExport.getMetaModel().getFullName());
      Mapper mapper = Mapper.of(klass);
      Query<? extends Model> query = getQuery(advancedExport, recordIds, klass);
      sortTargetField(query, advancedExportLineList);
      while (!(modelList = query.fetch(queryFetchSize, offset)).isEmpty()) {
        for (Model model : modelList) {
          if (++offset > maxExportLimit) {
            isReachMaxExportLimit = true;
            exportFileName = exportGenerator.getExportFileName();
            return exportGenerator.generateFile(outputList);
          }
          List<String> recordList = new ArrayList<>();
          for (AdvancedExportLine advancedExportLine : advancedExportLineList) {
            Object data =
                findField(
                    mapper,
                    Splitter.on(".")
                        .splitToList(advancedExportLine.getTargetField())
                        .listIterator(),
                    model);
            if (data != null) {
              recordList.add(data.toString());
            }
          }
          outputList.add(recordList);
        }
        JPA.clear();
      }
    } catch (ClassNotFoundException e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }

    exportFileName = exportGenerator.getExportFileName();
    return exportGenerator.generateFile(outputList);
  }

  protected Object findField(Mapper mapper, ListIterator<String> iter, Object model)
      throws AxelorException {
    String prev = iter.next();
    Property property = mapper.getProperty(prev);
    if (property == null || property.get(model) == null) {
      return "";
    }

    Object lastElement = property.get(model);
    String selection = property.getSelection();
    if (StringUtils.notBlank(selection)) {
      Option selectionItem = MetaStore.getSelectionItem(selection, lastElement.toString());
      return selectionItem == null ? "" : I18n.get(selectionItem.getTitle());
    }

    if (property.getTarget() == null) {
      return lastElement;
    }

    String lastFieldName = "";
    while (property.getTarget() != null && iter.hasNext()) {
      if (property.getType() == PropertyType.MANY_TO_MANY
          || property.getType() == PropertyType.ONE_TO_MANY) {
        Integer prevIndex = iter.previousIndex();
        String relationalField = fetchRelationalFields((Model) model, prev, iter);
        while (!prevIndex.equals(iter.previousIndex())) {
          iter.previous();
        }
        return relationalField;
      }
      lastFieldName = iter.next();
      model = property.get(model);
      mapper = Mapper.of(property.getTarget());
      property = mapper.getProperty(lastFieldName);
      prev = lastFieldName;
    }

    selection = property.getSelection();
    if (StringUtils.notBlank(selection)) {
      Option selectionItem =
          MetaStore.getSelectionItem(selection, mapper.get(model, lastFieldName).toString());
      return selectionItem == null ? "" : I18n.get(selectionItem.getTitle());
    }

    return mapper.get(model, lastFieldName);
  }

  @SuppressWarnings("unchecked")
  protected String fetchRelationalFields(Model model, String fieldName, ListIterator<String> iter)
      throws AxelorException {
    MetaField relationalField =
        metaFieldRepository
            .all()
            .filter("self.name = :name AND self.metaModel.name = :modelName")
            .bind("modelName", EntityHelper.getEntityClass(model).getSimpleName())
            .bind("name", fieldName)
            .fetchOne();

    Mapper mapper = Mapper.of(model.getClass());
    StringJoiner joiner = new StringJoiner("|");
    Collection<? extends Model> colValue =
        (Collection<? extends Model>) mapper.get(model, relationalField.getName());
    if (CollectionUtils.isNotEmpty(colValue)) {
      String next = iter.next();
      Collection<Model> col = fetchRelationalFields(colValue, relationalField);
      for (Model m : col) {
        mapper = Mapper.of(m.getClass());
        Object value = mapper.get(m, next);
        if (value instanceof Collection && ((Collection<String>) value).isEmpty()) {
          continue;
        }
        iter.previous();
        value = findField(mapper, iter, m);
        if (value != null && !value.toString().isEmpty()) {
          joiner.add(value.toString());
        }
      }
      iter.previous();
    }
    return joiner.toString();
  }

  protected void sortTargetField(
      Query<? extends Model> query, List<AdvancedExportLine> advancedExportLineList) {
    for (AdvancedExportLine advancedExportLine : advancedExportLineList) {
      if (advancedExportLine.getOrderBy()) {
        if (AdvancedExportLineRepository.ORDER_BY_TYPE_ASC.equals(
            advancedExportLine.getOrderByType())) {
          query.order(advancedExportLine.getTargetField());
        } else {
          query.order("-" + advancedExportLine.getTargetField());
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  protected Collection<Model> fetchRelationalFields(
      Collection<? extends Model> values, MetaField metaField) throws AxelorException {
    Collection<Model> dbValues = new HashSet<>();
    String className = MetaTool.computeFullClassName(metaField);
    for (Model model : values) {
      try {
        dbValues.add(JPA.find((Class<Model>) Class.forName(className), model.getId()));
      } catch (ClassNotFoundException e) {
        throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
      }
    }
    return dbValues;
  }

  protected Query<? extends Model> getQuery(
      AdvancedExport advancedExport, List<Long> recordIds, Class<? extends Model> klass) {
    Query<? extends Model> query = JpaRepository.of(klass).all();
    StringBuilder filter = new StringBuilder();

    if (!advancedExport.getIncludeArchivedRecords()) {
      filter.append("self.archived = 'f' OR self.archived IS NULL");
    }

    if (recordIds != null) {
      log.trace("criteria : {}", recordIds.toString());
      if (filter.length() != 0) {
        filter.append(" AND ");
      }
      filter.append(
          String.format(
              "self.id IN (%s)",
              recordIds.toString().substring(1, recordIds.toString().length() - 1)));
    }

    Filter securityFilter = getJpaSecurityFilter(advancedExport.getMetaModel());
    if (securityFilter != null) {
      if (filter.length() != 0) {
        filter.append(" AND ");
      }
      filter.append(securityFilter.getQuery());
      return query.filter(filter.toString(), securityFilter.getParams());
    }

    return filter.length() == 0 ? query : query.filter(filter.toString());
  }

  @Override
  public boolean getIsReachMaxExportLimit() {
    return isReachMaxExportLimit;
  }

  @Override
  public String getExportFileName() {
    return exportFileName;
  }

  @Override
  public boolean checkAdvancedExportExist(String metaModelName) {

    long total =
        Beans.get(AdvancedExportRepository.class)
            .all()
            .filter("self.metaModel.fullName = :metaModelName")
            .bind("metaModelName", metaModelName)
            .count();

    if (total == 0) {
      return false;
    }

    return true;
  }
}
