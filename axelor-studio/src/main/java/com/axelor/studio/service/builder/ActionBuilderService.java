/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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

import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaAction;
import com.axelor.studio.db.ActionBuilder;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionBuilderService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject private ActionViewBuilderService actionViewBuilderService;

  @Inject private ActionScriptBuilderService actionScriptBuilderService;

  @Transactional
  public MetaAction build(ActionBuilder builder) {

    if (builder.getTypeSelect() < 2
        && (builder.getLines() == null || builder.getLines().isEmpty())) {
      return null;
    }

    MetaAction metaAction = null;
    if (builder.getTypeSelect() == 3) {
      metaAction = actionViewBuilderService.build(builder);
    } else {
      metaAction = actionScriptBuilderService.build(builder);
    }

    if (builder.getMetaModule() != null) {
      metaAction.setModule(builder.getMetaModule().getName());
    }

    log.debug("Processing action: {}, type: {}", builder.getName(), builder.getTypeSelect());

    MetaStore.clear();

    return metaAction;
  }
}
