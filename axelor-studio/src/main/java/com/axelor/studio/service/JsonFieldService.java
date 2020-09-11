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
package com.axelor.studio.service;

import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaSelect;
import com.axelor.meta.db.MetaSelectItem;
import com.axelor.meta.db.repo.MetaSelectRepository;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.HashMap;
import java.util.Map;

public class JsonFieldService {

  public static final String SELECT_PREFIX = "custom-json-select-";

  @Inject private MetaSelectRepository metaSelectRepository;

  @Transactional
  public void updateSelection(MetaJsonField metaJsonField) {

    String selectionText = metaJsonField.getSelectionText();

    if (Strings.isNullOrEmpty(selectionText)) {
      removeSelection(metaJsonField);

      if (metaJsonField.getSelection() != null
          && metaJsonField.getSelection().equals(SELECT_PREFIX + metaJsonField.getId())) {
        metaJsonField.setSelection(null);
      }

      return;
    }

    processSelectionText(metaJsonField, selectionText);
  }

  private void processSelectionText(MetaJsonField metaJsonField, String selectionText) {

    String[] selection = selectionText.split("\n");

    MetaSelect metaSelect = findMetaSelect(metaJsonField.getId());
    Map<String, MetaSelectItem> itemMap = new HashMap<String, MetaSelectItem>();
    if (metaSelect == null) {
      metaSelect = new MetaSelect(SELECT_PREFIX + metaJsonField.getId());
    } else {
      for (MetaSelectItem item : metaSelect.getItems()) {
        itemMap.put(item.getValue(), item);
      }
    }
    metaSelect.clearItems();

    int order = 1;

    for (String option : selection) {
      option = option.trim();
      String title = option;
      String value = option;

      if (option.contains(":") && option.indexOf(":") != option.length() - 1) {
        title = option.substring(option.indexOf(":") + 1);
        value = option.substring(0, option.indexOf(":"));
      }

      MetaSelectItem metaSelectItem = itemMap.get(value);
      if (metaSelectItem == null) {
        metaSelectItem = new MetaSelectItem();
      }

      metaSelectItem.setTitle(title);
      metaSelectItem.setValue(value);
      metaSelectItem.setOrder(order);
      order++;

      metaSelect.addItem(metaSelectItem);
    }

    metaSelect.setIsCustom(true);
    metaSelectRepository.save(metaSelect);
    metaJsonField.setSelection(metaSelect.getName());
  }

  private MetaSelect findMetaSelect(Long jsonFieldId) {

    return metaSelectRepository
        .all()
        .filter("self.name = ?1 and self.isCustom = true", SELECT_PREFIX + jsonFieldId)
        .fetchOne();
  }

  @Transactional
  public void removeSelection(MetaJsonField metaJsonField) {

    MetaSelect metaSelect = findMetaSelect(metaJsonField.getId());

    if (metaSelect != null) {
      metaSelectRepository.remove(metaSelect);
    }
  }
}
