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
package com.axelor.apps.tool;

import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaSelectItem;
import com.axelor.meta.db.repo.MetaSelectItemRepository;
import com.google.inject.Inject;
import java.util.Optional;

public class MetaSelectTool {
  protected MetaSelectItemRepository metaSelectItemRepo;

  @Inject
  public MetaSelectTool(MetaSelectItemRepository metaSelectItemRepo) {
    this.metaSelectItemRepo = metaSelectItemRepo;
  }

  public String getSelectTitle(String selection, int value) {
    return Optional.of(
            metaSelectItemRepo
                .all()
                .filter("self.select.name = :selection AND self.value = :value")
                .bind("selection", selection)
                .bind("value", value)
                .fetchOne())
        .map(MetaSelectItem::getTitle)
        .map(I18n::get)
        .orElse("");
  }
}
