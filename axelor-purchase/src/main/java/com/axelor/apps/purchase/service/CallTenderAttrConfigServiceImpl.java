/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.purchase.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.purchase.db.CallTenderAttrConfig;
import com.axelor.apps.purchase.exception.PurchaseExceptionMessage;
import com.axelor.common.Inflector;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class CallTenderAttrConfigServiceImpl implements CallTenderAttrConfigService {

  protected final MetaJsonFieldRepository metaJsonFieldRepository;

  @Inject
  public CallTenderAttrConfigServiceImpl(MetaJsonFieldRepository metaJsonFieldRepository) {
    this.metaJsonFieldRepository = metaJsonFieldRepository;
  }

  @Override
  @Transactional
  public void syncMirrorFields(CallTenderAttrConfig config) {
    if (config == null || config.getId() == null) {
      return;
    }

    Map<String, MetaJsonField> sourceByName = getSourceByName(config);

    for (String target : getAllTargetModels()) {
      boolean enabled = isDisplayEnabled(config, target);
      Map<String, MetaJsonField> existingMirrors = getMirrorsByName(config, target);

      if (!enabled) {
        for (MetaJsonField m : existingMirrors.values()) {
          metaJsonFieldRepository.remove(m);
        }
        continue;
      }

      for (Map.Entry<String, MetaJsonField> entry : sourceByName.entrySet()) {
        MetaJsonField source = entry.getValue();
        MetaJsonField mirror = existingMirrors.remove(entry.getKey());
        if (mirror == null) {
          mirror = createMirror(source, config, target);
        }
        copyDefinition(source, mirror, config);
        metaJsonFieldRepository.save(mirror);
      }

      for (MetaJsonField stale : existingMirrors.values()) {
        metaJsonFieldRepository.remove(stale);
      }
    }
  }

  protected List<String> getAllTargetModels() {
    return Arrays.asList(MODEL_PRODUCT, MODEL_SO_LINE, MODEL_NEED, MODEL_OFFER);
  }

  protected boolean isDisplayEnabled(CallTenderAttrConfig config, String targetModel) {
    switch (targetModel) {
      case MODEL_PRODUCT:
        return Boolean.TRUE.equals(config.getDisplayInProduct());
      case MODEL_SO_LINE:
        return Boolean.TRUE.equals(config.getDisplayInSoLine());
      case MODEL_NEED:
        return Boolean.TRUE.equals(config.getDisplayInTenderNeed());
      case MODEL_OFFER:
        return Boolean.TRUE.equals(config.getDisplayInTenderOffer());
      default:
        return false;
    }
  }

  protected Map<String, MetaJsonField> getSourceByName(CallTenderAttrConfig config) {
    Map<String, MetaJsonField> map = new HashMap<>();
    if (config.getCustomFieldList() == null) {
      return map;
    }
    for (MetaJsonField f : config.getCustomFieldList()) {
      if (StringUtils.isBlank(f.getName())) {
        continue;
      }
      map.put(f.getName(), f);
    }
    return map;
  }

  protected Map<String, MetaJsonField> getMirrorsByName(
      CallTenderAttrConfig config, String targetModel) {

    List<MetaJsonField> mirrors =
        metaJsonFieldRepository
            .all()
            .filter("self.callTenderAttrConfigMirror = :config AND self.model = :model")
            .bind("config", config)
            .bind("model", targetModel)
            .fetch();

    Map<String, MetaJsonField> map = new HashMap<>();
    List<MetaJsonField> duplicates = new ArrayList<>();
    for (MetaJsonField f : mirrors) {
      if (StringUtils.isBlank(f.getName())) {
        continue;
      }
      if (map.containsKey(f.getName())) {
        duplicates.add(f);
      } else {
        map.put(f.getName(), f);
      }
    }
    for (MetaJsonField dup : duplicates) {
      metaJsonFieldRepository.remove(dup);
    }
    return map;
  }

  protected MetaJsonField createMirror(
      MetaJsonField source, CallTenderAttrConfig config, String targetModel) {
    MetaJsonField mirror = new MetaJsonField();
    mirror.setModel(targetModel);
    mirror.setModelField(MODEL_FIELD);
    mirror.setName(source.getName());
    mirror.setCallTenderAttrConfigMirror(config);
    return mirror;
  }

  protected void copyDefinition(
      MetaJsonField source, MetaJsonField target, CallTenderAttrConfig config) {

    target.setTitle(source.getTitle());
    target.setType(source.getType());
    target.setWidget(source.getWidget());
    target.setDefaultValue(source.getDefaultValue());
    target.setRequired(source.getRequired());
    target.setSelection(source.getSelection());
    target.setScale(source.getScale());
    target.setPrecision(source.getPrecision());
    target.setMinSize(source.getMinSize());
    target.setMaxSize(source.getMaxSize());
    target.setRegex(source.getRegex());
    target.setSequence(source.getSequence());
    target.setVisibleInGrid(source.getVisibleInGrid());

    String targetModel = target.getModel();
    boolean needsContext = !Objects.equals(targetModel, MODEL_SOURCE);
    if (needsContext && config != null && config.getId() != null) {
      target.setContextField("callTenderAttrConfig");
      target.setContextFieldTarget("com.axelor.apps.purchase.db.CallTenderAttrConfig");
      target.setContextFieldTargetName("name");
      target.setContextFieldValue(String.valueOf(config.getId()));
      target.setContextFieldTitle(config.getName());
    }
  }

  @Override
  @Transactional
  public void deleteFields(CallTenderAttrConfig config) {
    if (config == null || config.getId() == null) {
      return;
    }
    JPA.em()
        .createQuery(
            "DELETE FROM MetaJsonField self "
                + "WHERE self.callTenderAttrConfig = :config "
                + "OR self.callTenderAttrConfigMirror = :config")
        .setParameter("config", config)
        .executeUpdate();
  }

  @Override
  public String buildDefaultAttrs(CallTenderAttrConfig config, String existingAttrs) {
    if (config == null || config.getCustomFieldList() == null) {
      return existingAttrs;
    }
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode root;
    try {
      if (StringUtils.notEmpty(existingAttrs)) {
        JsonNode parsed = mapper.readTree(existingAttrs);
        root = parsed.isObject() ? (ObjectNode) parsed : mapper.createObjectNode();
      } else {
        root = mapper.createObjectNode();
      }
    } catch (Exception e) {
      root = mapper.createObjectNode();
    }

    for (MetaJsonField f : config.getCustomFieldList()) {
      String name = f.getName();
      if (StringUtils.isBlank(name) || root.has(name)) {
        continue;
      }
      String def = f.getDefaultValue();
      if (StringUtils.isBlank(def)) {
        continue;
      }
      String type = f.getType();
      if (type == null) {
        root.put(name, def);
        continue;
      }
      switch (type) {
        case "boolean":
          root.put(name, Boolean.parseBoolean(def));
          break;
        case "integer":
        case "long":
          try {
            root.put(name, Long.parseLong(def.trim()));
          } catch (NumberFormatException ex) {
            root.put(name, def);
          }
          break;
        case "decimal":
          try {
            root.put(name, new BigDecimal(def.trim()));
          } catch (NumberFormatException ex) {
            root.put(name, def);
          }
          break;
        default:
          root.put(name, def);
      }
    }
    return root.toString();
  }

  @Override
  public void regenerateFieldNames(CallTenderAttrConfig config) {
    if (config == null || config.getCustomFieldList() == null) {
      return;
    }
    for (MetaJsonField field : config.getCustomFieldList()) {
      String title = field.getTitle();
      if (StringUtils.isBlank(title)) {
        continue;
      }
      field.setName(Inflector.getInstance().camelize(title, true));
    }
  }

  @Override
  public void validateFieldNameUniqueness(CallTenderAttrConfig config) throws AxelorException {
    if (config == null || config.getCustomFieldList() == null) {
      return;
    }
    Set<String> seen = new HashSet<>();
    Long configId = config.getId();
    for (MetaJsonField field : config.getCustomFieldList()) {
      String name = field.getName();
      if (StringUtils.isBlank(name)) {
        continue;
      }
      if (!seen.add(name)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get("Field name '%s' is duplicated within this configuration."),
            name);
      }
      TypedQuery<MetaJsonField> query =
          JPA.em()
              .createQuery(
                  "SELECT self FROM MetaJsonField self "
                      + "WHERE self.name = :name AND self.model = :model "
                      + "AND self.callTenderAttrConfig IS NOT NULL "
                      + "AND (:configId IS NULL OR self.callTenderAttrConfig.id != :configId)",
                  MetaJsonField.class)
              .setFlushMode(FlushModeType.COMMIT)
              .setParameter("name", name)
              .setParameter("model", CallTenderAttrConfigService.MODEL_SOURCE)
              .setParameter("configId", configId);
      List<MetaJsonField> conflicts = query.getResultList();
      if (!conflicts.isEmpty()) {
        MetaJsonField conflict = conflicts.get(0);
        String otherConfig =
            conflict.getCallTenderAttrConfig() == null
                ? ""
                : conflict.getCallTenderAttrConfig().getName();
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(PurchaseExceptionMessage.CALL_TENDER_ATTR_CONFIG_DUPLICATE_FIELD),
            name,
            otherConfig);
      }
    }
  }
}
