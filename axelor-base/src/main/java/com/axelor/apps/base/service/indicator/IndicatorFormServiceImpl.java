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
package com.axelor.apps.base.service.indicator;

import com.axelor.apps.base.db.IndicatorConfig;
import com.axelor.apps.base.db.repo.IndicatorConfigRepository;
import com.axelor.common.Inflector;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.views.FormView;
import com.axelor.meta.schema.views.Panel;
import com.axelor.meta.schema.views.Spacer;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;

public class IndicatorFormServiceImpl implements IndicatorFormService {

  private static final String FORM_NAME_PATTERN = "indicator-result-viewer-%s-form";

  protected final MetaViewRepository metaViewRepository;

  @Inject
  public IndicatorFormServiceImpl(MetaViewRepository metaViewRepository) {
    this.metaViewRepository = metaViewRepository;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void createOrUpdateForm(IndicatorConfig config) {
    if (config.getDisplayInRecordViewTypeSelect()
        != IndicatorConfigRepository.DISPLAY_TYPE_BUTTON) {
      return;
    }
    String dasherizedModelName =
        Inflector.getInstance().dasherize(config.getTargetModel().getName());

    final String formName = String.format(FORM_NAME_PATTERN, dasherizedModelName);
    final FormView form = buildFormView(formName, config);

    final String xml = XMLViews.toXml(form, true);
    MetaView metaView =
        Optional.ofNullable(metaViewRepository.findByName(formName)).orElseGet(MetaView::new);
    metaView.setName(formName);
    metaView.setType(form.getType());
    metaView.setTitle(form.getTitle());
    metaView.setXml(xml);

    metaViewRepository.save(metaView);
  }

  protected FormView buildFormView(String name, IndicatorConfig config) {
    final FormView form = new FormView();
    form.setName(name);
    form.setTitle(I18n.get("Indicators"));
    form.setModel(config.getTargetModel().getFullName());

    String falseStr = Boolean.FALSE.toString();
    form.setCanNew(falseStr);
    form.setCanEdit(falseStr);
    form.setCanSave(falseStr);
    form.setCanDelete(falseStr);
    form.setCanArchive(falseStr);

    Panel panel = new Panel();
    panel.setHidden(true);
    panel.setItems(List.of(new Spacer()));
    form.setItems(List.of(panel));

    return form;
  }
}
