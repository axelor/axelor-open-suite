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
package com.axelor.apps.bpm.service.deployment;

import com.axelor.apps.bpm.db.WkfTaskConfig;
import com.axelor.apps.bpm.service.execution.WkfInstanceService;
import com.axelor.auth.db.Role;
import com.axelor.auth.db.repo.RoleRepository;
import com.axelor.db.Query;
import com.axelor.meta.db.MetaAttrs;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaAttrsRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetaAttrsServiceImpl implements MetaAttrsService {

  protected final Logger log = LoggerFactory.getLogger(MetaAttrsServiceImpl.class);

  public static final String META_ATTRS_CONDITION =
      "com.axelor.inject.Beans.get("
          + WkfInstanceService.class.getName()
          + ").isActiveTask(processInstanceId, '%s')";

  public static final String META_ATTRS_CONDITION_PERMANENT =
      "com.axelor.inject.Beans.get("
          + WkfInstanceService.class.getName()
          + ").isActivatedTask(processInstanceId, '%s')";

  @Inject protected MetaModelRepository metaModelRepository;

  @Inject protected MetaAttrsRepository metaAttrsRepository;

  @Inject protected RoleRepository roleRepository;

  @Override
  public List<MetaAttrs> createMetaAttrs(
      String taskName,
      ModelElementInstance modelElementInstance,
      WkfTaskConfig config,
      String wkfModelId) {

    List<MetaAttrs> metaAttrsList = new ArrayList<MetaAttrs>();

    Collection<CamundaProperty> properties =
        modelElementInstance.getChildElementsByType(CamundaProperty.class);

    log.debug("Extension elements: {}", properties.size());

    String model = null;
    String view = null;
    String item = null;
    String roles = null;
    String permanent = null;

    for (CamundaProperty property : properties) {

      String name = property.getCamundaName();
      String value = property.getCamundaValue();

      if (name == null) {
        continue;
      }
      log.debug("Processing property: {}, value: {}", name, value);

      switch (name) {
        case "model":
          model = getModel(value);
          view = null;
          item = null;
          roles = null;
          break;
        case "view":
          view = value;
          break;
        case "item":
          item = value;
          break;
        case "roles":
          roles = value;
          break;
        case "modelName":
          break;
        case "modelType":
          break;
        case "itemLabel":
          break;
        case "permanent":
          permanent = value;
          break;
        default:
          if (model != null && item != null) {
            MetaAttrs metaAttrs = new MetaAttrs();
            metaAttrs.setModel(model);
            metaAttrs.setView(view);
            metaAttrs.setField(item);
            metaAttrs.setRoles(findRoles(roles));
            if (permanent != null && permanent.equals("true")) {
              metaAttrs.setCondition(String.format(META_ATTRS_CONDITION_PERMANENT, taskName));
            } else {
              metaAttrs.setCondition(String.format(META_ATTRS_CONDITION, taskName));
            }
            metaAttrs.setValue(value);
            metaAttrs.setName(name);
            metaAttrs.setWkfModelId(wkfModelId);
            metaAttrsList.add(metaAttrs);
          }
          break;
      }
    }

    return metaAttrsList;
  }

  private String getModel(String value) {

    MetaModel metaModel = metaModelRepository.findByName(value);

    if (metaModel != null) {
      return metaModel.getFullName();
    }

    return value;
  }

  private MetaAttrs findMetaAttrs(MetaAttrs metaAttrs, Long wkfModelId) {

    MetaAttrs savedAttrs =
        metaAttrsRepository
            .all()
            .filter(
                "self.wkfModelId = ?1 "
                    + "and self.model = ?2 "
                    + "and self.view = ?3 "
                    + "and self.field = ?4 "
                    + "and self.condition = ?5 "
                    + "and self.name = ?6",
                wkfModelId,
                metaAttrs.getModel(),
                metaAttrs.getView(),
                metaAttrs.getField(),
                metaAttrs.getName(),
                metaAttrs.getCondition())
            .fetchOne();

    if (savedAttrs != null) {
      return savedAttrs;
    }

    return metaAttrs;
  }

  private Set<Role> findRoles(String roles) {

    Set<Role> roleSet = new HashSet<Role>();

    if (roles != null) {
      roleSet.addAll(
          roleRepository.all().filter("self.name in ?1", Arrays.asList(roles.split(","))).fetch());
    }

    return roleSet;
  }

  @Override
  @Transactional
  public void saveMetaAttrs(List<MetaAttrs> metaAttrsList, Long wkfModelId) {

    List<Long> metaAttrsIds = new ArrayList<Long>();
    metaAttrsIds.add(0L);

    for (MetaAttrs metaAttrs : metaAttrsList) {
      MetaAttrs saved = findMetaAttrs(metaAttrs, wkfModelId);
      log.debug("Creating meta attrs: {}", saved);
      saved.setValue(metaAttrs.getValue());
      saved.setRoles(metaAttrs.getRoles());
      metaAttrsRepository.save(saved);
      metaAttrsIds.add(saved.getId());
    }

    long attrsRemoved =
        Query.of(MetaAttrs.class)
            .filter(
                "self.id not in ?1 and self.wkfModelId = ?2", metaAttrsIds, wkfModelId.toString())
            .remove();

    log.debug("Total meta attrs removed: {}", attrsRemoved);
  }
}
