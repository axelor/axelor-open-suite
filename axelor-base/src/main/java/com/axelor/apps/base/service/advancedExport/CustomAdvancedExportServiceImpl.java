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
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaSelect;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.db.repo.MetaSelectRepository;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public class CustomAdvancedExportServiceImpl implements CustomAdvancedExportService {

  private StringBuilder fromFields = new StringBuilder("MetaJsonRecord AS self ");
  private StringBuilder selectFields = new StringBuilder();

  private int count = 0;
  private int msi = 0, mt = 0;

  @Inject private MetaJsonFieldRepository jsonFieldRepo;
  @Inject private MetaModelRepository metaModelRepo;
  @Inject private MetaSelectRepository metaSelectRepo;

  @Override
  public void createCustomQueryParts(String[] splitField, MetaJsonModel jsonModel)
      throws ClassNotFoundException {
    String fieldName = splitField[0];
    MetaJsonField jsonField =
        jsonFieldRepo
            .all()
            .filter("self.name=?1 AND self.jsonModel = ?2", fieldName, jsonModel)
            .fetchOne();

    if (jsonField.getTargetJsonModel() != null) {
      if ("id".equals(splitField[1])) {
        selectFields.append("json_extract_integer(self.attrs,'" + fieldName + "','id'),");
        return;
      }

      count++;
      fromFields.append(
          "LEFT JOIN MetaJsonRecord AS col_"
              + count
              + " ON (col_"
              + count
              + ".id=json_extract_integer(self.attrs,'"
              + fieldName
              + "','id')) ");

      relationalCustomField(
          splitField,
          1,
          AdvancedExportService.META_JSON_RECORD_FULL_NAME
              + " "
              + jsonField.getTargetJsonModel().getName());

    } else if (jsonField.getTargetModel() != null) {
      MetaModel realModel =
          metaModelRepo.findByName(StringUtils.substringAfterLast(jsonField.getTargetModel(), "."));
      count++;
      fromFields.append(
          "LEFT JOIN "
              + realModel.getName()
              + " AS col_"
              + count
              + " ON (col_"
              + count
              + ".id=json_extract_integer(self.attrs,'"
              + fieldName
              + "','id')) ");

      relationalRealField(splitField, 1, realModel);
    } else {
      if (Strings.isNullOrEmpty(jsonField.getSelection())) {
        selectFields.append("json_extract_text(self.attrs,'" + fieldName + "'),");
      } else {
        addSelectionField(jsonField.getSelection(), fieldName, splitField.length, true);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void relationalRealField(String[] splitField, int parentIndex, MetaModel metaModel)
      throws ClassNotFoundException {
    for (int index = parentIndex; index < splitField.length; index++) {
      Class<? extends Model> klass =
          (Class<? extends Model>) Class.forName(metaModel.getFullName());
      Mapper mapper = Mapper.of(klass);
      String fieldName = splitField[index];
      Property prop = mapper.getProperty(fieldName);

      if (AdvancedExportService.FIELD_ATTRS.equals(prop.getName())) {
        relationalCustomField(splitField, index + 1, metaModel.getFullName());
        break;
      }

      if (prop.getTarget() == null) {
        if (Strings.isNullOrEmpty(prop.getSelection())) {
          selectFields.append("col_" + count + "." + fieldName + ",");
        } else {
          addSelectionField(prop.getSelection(), fieldName, splitField.length, false);
        }
      } else {
        count++;
        fromFields.append(
            "LEFT JOIN col_" + (count - 1) + "." + fieldName + " AS col_" + count + " ");
        metaModel =
            metaModelRepo.all().filter("self.fullName=?1", prop.getTarget().getName()).fetchOne();
      }
    }
  }

  public void relationalCustomField(String[] splitField, int parentIndex, String uniqueModel)
      throws ClassNotFoundException {
    for (int index = parentIndex; index < splitField.length; index++) {
      String fieldName = splitField[index];
      MetaJsonField jsonField =
          jsonFieldRepo
              .all()
              .filter("self.name=?1 AND self.uniqueModel = ?2", fieldName, uniqueModel)
              .fetchOne();

      if (jsonField.getTargetJsonModel() != null) {
        count++;
        fromFields.append(
            "LEFT JOIN MetaJsonRecord AS col_"
                + count
                + " ON (col_"
                + count
                + ".id=json_extract_integer(col_"
                + (count - 1)
                + ".attrs,'"
                + fieldName
                + "','id')) ");

        uniqueModel =
            AdvancedExportService.META_JSON_RECORD_FULL_NAME
                + " "
                + jsonField.getTargetJsonModel().getName();
      } else if (jsonField.getTargetModel() != null) {
        MetaModel realModel =
            metaModelRepo.findByName(
                StringUtils.substringAfterLast(jsonField.getTargetModel(), "."));

        count++;
        fromFields.append(
            "LEFT JOIN "
                + realModel.getName()
                + " AS col_"
                + count
                + " ON (col_"
                + count
                + ".id=json_extract_integer(col_"
                + (count - 1)
                + ".attrs,'"
                + fieldName
                + "','id')) ");

        relationalRealField(splitField, index + 1, realModel);
        break;
      } else {
        if (Strings.isNullOrEmpty(jsonField.getSelection())) {
          selectFields.append("json_extract_text(col_" + count + ".attrs,'" + fieldName + "'),");
        } else {
          addSelectionField(jsonField.getSelection(), fieldName, splitField.length, true);
        }
      }
    }
  }

  public void addSelectionField(
      String selectionName, String fieldName, int splitFieldsLength, boolean isJson) {
    String language = Optional.ofNullable(AuthUtils.getUser()).map(User::getLanguage).orElse(null);
    MetaSelect select = metaSelectRepo.findByName(selectionName);
    String selectionValue = null;

    if (splitFieldsLength == 1) {
      selectionValue = "json_extract_text(self.attrs,'" + fieldName + "')";
    } else {
      if (isJson) {
        selectionValue = "json_extract_text(col_" + count + ".attrs,'" + fieldName + "')";
      } else {
        selectionValue = "CAST(col_" + count + "." + fieldName + " AS string)";
      }
    }

    fromFields.append(
        "LEFT JOIN MetaSelectItem AS msi_"
            + msi
            + " ON (msi_"
            + msi
            + ".select = "
            + select.getId()
            + " AND msi_"
            + msi
            + ".value = "
            + selectionValue
            + ") ");

    if (language.equals(AdvancedExportService.LANGUAGE_FR)) {

      selectFields.append(
          "COALESCE ("
              + "NULLIF"
              + "("
              + ("mt_" + (mt))
              + ".message, '') , "
              + ("msi_" + (msi))
              + ".title),");

      fromFields.append(
          "LEFT JOIN "
              + "MetaTranslation AS "
              + ("mt_" + (mt))
              + " ON "
              + ("msi_" + (msi))
              + ".title = "
              + ("mt_" + (mt))
              + ".key AND "
              + ("mt_" + (mt))
              + ".language = '"
              + language
              + "' ");
      mt++;
    } else {
      selectFields.append("msi_" + msi + ".title,");
    }
    msi++;
  }

  public StringBuilder createQueryBuilder(
      AdvancedExport advancedExport,
      StringBuilder selectFieldBuilder,
      List<Long> recordIds,
      StringBuilder orderByFieldBuilder) {

    MetaJsonModel metaJsonModel = advancedExport.getJsonModel();

    StringBuilder queryBuilder = new StringBuilder();
    queryBuilder.append("SELECT NEW List(");
    selectFields.deleteCharAt(selectFields.length() - 1);
    queryBuilder.append(selectFields);
    queryBuilder.append(") FROM ");
    queryBuilder.append(fromFields);
    queryBuilder.append(" WHERE self.jsonModel='" + metaJsonModel.getName() + "' ");

    return queryBuilder;
  }
}
