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
package com.axelor.apps.base.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.ConfigurationTemplate;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.configuration.template.ConfigurationTemplateService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ConfigurationTemplateController {

  public void installConfig(ActionRequest request, ActionResponse response) throws AxelorException {
    Company company = request.getContext().asType(Company.class);

    if (company.getConfigurationTemplate() == null
        || company.getConfigurationTemplate().getBindingFile() == null
        || company.getConfigurationTemplate().getMetaFile() == null) {
      response.setError(I18n.get(BaseExceptionMessage.CONFIG_IMPORT_MISSING_METAFILE));
      return;
    }

    if (Beans.get(ConfigurationTemplateService.class).installConfig(company)) {
      response.setInfo(I18n.get(BaseExceptionMessage.CONFIG_IMPORT_SUCCESS));
    } else {
      response.setInfo(I18n.get(BaseExceptionMessage.CONFIG_IMPORT_ERROR));
    }
    response.setReload(true);
  }

  public void importConfigurationForCompanies(ActionRequest request, ActionResponse response)
      throws AxelorException {
    ConfigurationTemplate configurationTemplate =
        request.getContext().asType(ConfigurationTemplate.class);

    response.setNotify(
        Beans.get(ConfigurationTemplateService.class)
            .installConfigForAllCompanies(configurationTemplate));
  }

  @SuppressWarnings("unchecked")
  public void linkCompanies(ActionRequest request, ActionResponse response) throws AxelorException {
    ConfigurationTemplate configurationTemplate =
        findObject(
            ConfigurationTemplate.class,
            (LinkedHashMap<String, Object>) request.getContext().get("_configurationTemplate"));
    Object companiesToLink = request.getContext().get("_companiesToLink");
    List<Company> companyList =
        companiesToLink != null ? findItems(Company.class, companiesToLink) : null;

    Beans.get(ConfigurationTemplateService.class).linkCompanies(companyList, configurationTemplate);
    response.setCanClose(true);
  }

  @SuppressWarnings("unchecked")
  public static <T extends Model> List<T> findItems(Class<T> tClass, Object items) {
    Objects.requireNonNull(tClass);
    Objects.requireNonNull(items);
    if (ObjectUtils.isEmpty(items)) {
      return Lists.newArrayList();
    }

    Collection<Map<String, Object>> collection =
        items instanceof List<?>
            ? (List<Map<String, Object>>) items
            : (Set<Map<String, Object>>) items;

    return JPA.all(tClass)
        .filter(
            "self.id IN (?1)",
            collection.stream()
                .filter(item -> item.get("id") != null)
                .map(item -> Long.valueOf(item.get("id").toString()))
                .collect(Collectors.toSet()))
        .fetch();
  }

  @SuppressWarnings("unchecked")
  public static <T extends Model> T findObject(Class<T> tClass, Object object) {
    Objects.requireNonNull(tClass);
    Objects.requireNonNull(object);
    Object objectId = ((Map<String, Object>) object).get("id");
    if (objectId == null) {
      return null;
    }
    return JPA.find(tClass, Long.parseLong(objectId.toString()));
  }
}
