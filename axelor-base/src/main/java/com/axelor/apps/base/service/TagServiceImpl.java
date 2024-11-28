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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Tag;
import com.axelor.common.StringUtils;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class TagServiceImpl implements TagService {

  protected MetaModelRepository metaModelRepository;

  @Inject
  public TagServiceImpl(MetaModelRepository metaModelRepository) {
    this.metaModelRepository = metaModelRepository;
  }

  @Override
  public void addMetaModelToTag(Tag tag, String fullName) {
    if (!StringUtils.isEmpty(fullName)) {
      MetaModel metaModel =
          metaModelRepository.all().filter("self.fullName = ?", fullName).fetchOne();

      if (metaModel != null) {
        tag.addConcernedModelSetItem(metaModel);
      }
    }
  }

  protected void setDefaultColor(Tag tag) {
    String primaryColor = MetaStore.getSelectionItem("color.name.selection", "blue").getColor();
    tag.setColor(primaryColor);
  }

  @Override
  public Map<String, Object> getOnNewValuesMap(Tag tag, String fullNameModel, String fieldModel) {
    Map<String, Object> valuesMap = new HashMap<>();

    this.addMetaModelToTag(tag, fullNameModel);
    this.addMetaModelToTag(tag, fieldModel);
    this.setDefaultColor(tag);

    valuesMap.put("concernedModelSet", tag.getConcernedModelSet());
    valuesMap.put("color", tag.getColor());
    return valuesMap;
  }
}
