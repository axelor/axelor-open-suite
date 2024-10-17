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
package com.axelor.apps.base.service.meta;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.common.Inflector;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.google.inject.Inject;
import java.util.Map;

public class MetaViewServiceImpl implements MetaViewService {

  protected MetaViewRepository metaViewRepository;

  @Inject
  public MetaViewServiceImpl(MetaViewRepository metaViewRepository) {
    this.metaViewRepository = metaViewRepository;
  }

  @Override
  public Map<String, Object> getActionView(Class<?> modelClass, Long recordId)
      throws AxelorException {
    final Inflector inflector = Inflector.getInstance();
    String simpleName = modelClass.getSimpleName();
    String viewName = inflector.underscore(simpleName) + "-form";
    String viewTitle = simpleName;

    viewName = inflector.dasherize(viewName);
    viewTitle = inflector.humanize(viewTitle);

    if (metaViewRepository.findByName(viewName) == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(I18n.get(BaseExceptionMessage.VIEW_NOT_FOUND), simpleName));
    }
    return ActionView.define(I18n.get(viewTitle))
        .add("form", viewName)
        .model(modelClass.getName())
        .context("_showRecord", recordId)
        .map();
  }
}
