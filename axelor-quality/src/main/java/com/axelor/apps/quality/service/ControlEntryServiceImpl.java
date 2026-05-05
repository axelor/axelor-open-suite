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
package com.axelor.apps.quality.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.quality.db.ControlEntry;
import com.axelor.apps.quality.db.ControlPlan;
import com.axelor.apps.quality.db.repo.ControlEntryRepository;
import com.axelor.apps.quality.db.repo.ControlPlanRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

public class ControlEntryServiceImpl implements ControlEntryService {

  protected ControlEntrySampleService controlEntrySampleService;
  protected ControlPlanRepository controlPlanRepository;
  protected ControlEntryRepository controlEntryRepository;

  @Inject
  public ControlEntryServiceImpl(
      ControlEntrySampleService controlEntrySampleService,
      ControlPlanRepository controlPlanRepository,
      ControlEntryRepository controlEntryRepository) {
    this.controlEntrySampleService = controlEntrySampleService;
    this.controlPlanRepository = controlPlanRepository;
    this.controlEntryRepository = controlEntryRepository;
  }

  private static final String DEFAULT_SAMPLE_NAME = "-";

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void createSamples(ControlEntry controlEntry) {
    Objects.requireNonNull(controlEntry);

    IntStream.range(0, controlEntry.getSampleCount())
        .mapToObj(i -> controlEntrySampleService.createSample(i, DEFAULT_SAMPLE_NAME, controlEntry))
        .forEach(controlEntry::addControlEntrySamplesListItem);

    controlEntry.setStatusSelect(ControlEntryRepository.IN_PROGRESS_STATUS);
  }

  @Override
  public Map<String, Object> addControlEntry(Context context) {
    Objects.requireNonNull(context);

    Map<String, Object> values = new HashMap<>();
    Class<?> contextClass = context.getContextClass();
    String relatedToSelect = getRelatedToSelect(contextClass);
    Long relatedToSelectId = getRelatedToSelectId(context);
    values.put("relatedTo", relatedToSelect);
    values.put("relatedToId", relatedToSelectId);

    ControlPlan controlPlan =
        getControlPlan(
            getControlPlanRelatedToSelect(contextClass),
            getControlPlanRelatedToSelectId(contextClass, context, relatedToSelectId));

    if (controlPlan != null) {
      values.put("controlPlan", controlPlan);
      getTemplateValues(values, controlPlan, relatedToSelect);
    }
    return ActionView.define(I18n.get("Control entry"))
        .model(ControlEntry.class.getName())
        .add("form", "control-entry-form")
        .add("grid", "control-entry-grid")
        .param("popup", "reload")
        .context("_relatedTo", values.get("relatedTo"))
        .context("_relatedToId", values.get("relatedToId"))
        .context("_controlPlan", values.get("controlPlan"))
        .context("_name", values.get("name"))
        .context("_sampleCount", values.get("sampleCount"))
        .context("_inspector", values.get("inspector"))
        .map();
  }

  @Override
  public Map<String, Object> onControlPlanChange(ControlEntry controlEntry) {
    Objects.requireNonNull(controlEntry);

    Map<String, Object> values = new HashMap<>();
    values.put("relatedToSelect", controlEntry.getRelatedToSelect());
    values.put("relatedToSelectId", controlEntry.getRelatedToSelectId());

    ControlPlan controlPlan = controlEntry.getControlPlan();
    if (controlPlan == null) {
      return values;
    }
    String relatedToSelect = getRelatedToSelectOnControlPlanChange(controlPlan);
    Long relatedToSelectId =
        getRelatedToSelectIdOnControlPlanChange(controlPlan, controlEntry.getRelatedToSelect());

    values.put("relatedToSelect", relatedToSelect);
    values.put("relatedToSelectId", relatedToSelectId);

    getTemplateValues(values, controlPlan, relatedToSelect);
    return values;
  }

  protected String getRelatedToSelect(Class<?> contextClass) {
    if (contextClass == null) {
      return null;
    }
    return contextClass.getName();
  }

  protected Long getRelatedToSelectId(Context context) {
    if (context.get("id") == null) {
      return null;
    }
    return Long.valueOf(context.get("id").toString());
  }

  protected String getControlPlanRelatedToSelect(Class<?> contextClass) {
    if (OperationOrder.class.equals(contextClass)) {
      return ProdProcessLine.class.getName();
    }
    return Product.class.getName();
  }

  protected Long getControlPlanRelatedToSelectId(
      Class<?> contextClass, Context context, Long relatedToSelectId) {
    if (OperationOrder.class.equals(contextClass)) {
      if (context.get("prodProcessLine") != null) {
        ProdProcessLine prodProcessLine = (ProdProcessLine) context.get("prodProcessLine");
        return prodProcessLine.getId();
      }
    }

    if (context.get("product") != null) {
      Product product = (Product) context.get("product");
      return product.getId();
    }
    return relatedToSelectId;
  }

  protected String getRelatedToSelectOnControlPlanChange(ControlPlan controlPlan) {
    if (ProdProcessLine.class.getName().equals(controlPlan.getRelatedToSelect())) {
      return OperationOrder.class.getName();
    }
    return controlPlan.getRelatedToSelect();
  }

  protected Long getRelatedToSelectIdOnControlPlanChange(
      ControlPlan controlPlan, String relatedToSelect) {
    if (ProdProcessLine.class.getName().equals(controlPlan.getRelatedToSelect())
        && !OperationOrder.class.getName().equals(relatedToSelect)) {
      return null;
    }
    return controlPlan.getRelatedToSelectId();
  }

  protected ControlPlan getControlPlan(String relatedToSelect, Long relatedToSelectId) {
    if (relatedToSelect == null || relatedToSelectId == null) {
      return null;
    }
    List<ControlPlan> controlPlans =
        controlPlanRepository
            .all()
            .filter(
                "self.relatedToSelect = ?1 AND self.relatedToSelectId = ?2 AND self.statusSelect = ?3",
                relatedToSelect,
                relatedToSelectId,
                ControlPlanRepository.APPLICABLE_STATUS)
            .fetch();
    return controlPlans.size() == 1 ? controlPlans.get(0) : null;
  }

  protected ControlEntry getTemplateControlEntry(ControlPlan controlPlan, String relatedToSelect) {
    if (controlPlan == null || relatedToSelect == null) {
      return null;
    }
    List<ControlEntry> controlEntries =
        controlEntryRepository
            .all()
            .filter(
                "self.controlPlan = ?1 AND self.relatedToSelect = ?2 AND self.relatedToSelectId is null",
                controlPlan,
                relatedToSelect)
            .fetch();
    return controlEntries.size() == 1 ? controlEntries.get(0) : null;
  }

  protected void getTemplateValues(
      Map<String, Object> values, ControlPlan controlPlan, String relatedToSelect) {
    ControlEntry templateControlEntry = getTemplateControlEntry(controlPlan, relatedToSelect);
    if (templateControlEntry == null) {
      return;
    }
    values.put("name", templateControlEntry.getName());
    values.put("sampleCount", templateControlEntry.getSampleCount());
    values.put("inspector", templateControlEntry.getInspector());
  }
}
