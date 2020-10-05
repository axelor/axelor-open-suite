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
import com.axelor.meta.db.MetaSelect;
import com.axelor.meta.db.MetaSelectItem;
import com.axelor.meta.db.repo.MetaSelectRepository;
import com.axelor.meta.schema.views.Selection.Option;
import com.axelor.studio.db.SelectionBuilder;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectionBuilderService {

  @Inject private MetaSelectRepository metaSelectRepository;

  public static final String SELECTION_PREFIX = "custom-selection-builder-";

  public String createSelectionText(String selectionName) {

    List<Option> options = MetaStore.getSelectionList(selectionName);

    boolean allSame = true;
    List<String> values = new ArrayList<>();
    List<String> valuesWithTitles = new ArrayList<String>();

    for (Option option : options) {
      if (option.getHidden() != null && option.getHidden()) {
        continue;
      }
      valuesWithTitles.add(option.getValue() + ":" + option.getTitle());
      if (allSame && !option.getValue().equals(option.getTitle())) {
        allSame = false;
      } else {
        values.add(option.getValue());
      }
    }

    return Joiner.on("\n").join(allSame ? values : valuesWithTitles);
  }

  @Transactional
  public void removeSelection(String name, String xmlId) {

    if (name == null && xmlId == null) {
      return;
    }

    MetaSelect metaSelect = xmlId != null ? findMetaSelectById(xmlId) : findMetaSelectByName(name);

    if (metaSelect != null) {
      metaSelectRepository.remove(metaSelect);
    }
  }

  public void build(SelectionBuilder selectionBuilder) {

    String xmlId = SELECTION_PREFIX + selectionBuilder.getId();

    updateMetaSelectFromText(
        selectionBuilder.getSelectionText(), selectionBuilder.getName(), xmlId);
  }

  @Transactional
  public String updateMetaSelectFromText(String selectionText, String name, String xmlId) {

    if (name == null && xmlId == null) {
      return null;
    }

    MetaSelect metaSelect = updateSelectItems(selectionText, name, xmlId);

    metaSelect.setIsCustom(true);
    metaSelect.setName(name);

    if (xmlId != null) {
      Integer priority = getPriority(name, xmlId);
      if (priority != null) {
        metaSelect.setPriority(priority);
      }
      metaSelect.setXmlId(xmlId);
    }

    metaSelectRepository.save(metaSelect);

    return name;
  }

  private MetaSelect updateSelectItems(String selectionText, String name, String xmlId) {

    String[] selection = selectionText.split("\n");

    MetaSelect metaSelect = xmlId != null ? findMetaSelectById(xmlId) : findMetaSelectByName(name);

    Map<String, MetaSelectItem> itemMap = new HashMap<String, MetaSelectItem>();

    if (metaSelect == null) {
      metaSelect = new MetaSelect(name);
    } else {
      for (MetaSelectItem item : metaSelect.getItems()) {
        itemMap.put(item.getValue(), item);
      }
    }
    metaSelect.clearItems();

    int order = 1;

    for (String option : selection) {
      option = option.trim();
      final String title;
      final String value;

      if (option.contains(":") && option.indexOf(":") != option.length() - 1) {
        title = option.substring(option.indexOf(":") + 1);
        value = option.substring(0, option.indexOf(":"));
      } else {
        title = option;
        value = option;
      }

      MetaSelectItem metaSelectItem = updateItem(itemMap, order, title, value);

      order++;

      metaSelect.addItem(metaSelectItem);
    }

    return metaSelect;
  }

  private MetaSelectItem updateItem(
      Map<String, MetaSelectItem> itemMap, int order, final String title, final String value) {

    MetaSelectItem metaSelectItem = itemMap.get(value);
    if (metaSelectItem == null) {
      metaSelectItem = new MetaSelectItem();
    }

    metaSelectItem.setTitle(title);
    metaSelectItem.setValue(value);
    metaSelectItem.setOrder(order);

    return metaSelectItem;
  }

  private MetaSelect findMetaSelectByName(String name) {

    return metaSelectRepository
        .all()
        .filter("self.name = ?1 and self.isCustom is true", name)
        .fetchOne();
  }

  private MetaSelect findMetaSelectById(String xmlId) {

    return metaSelectRepository
        .all()
        .filter("self.xmlId = ?1 and self.isCustom is true", xmlId)
        .fetchOne();
  }

  private Integer getPriority(String name, String xmlId) {

    MetaSelect metaSelect =
        metaSelectRepository
            .all()
            .filter("(self.xmlId is null OR self.xmlId != ?1) and self.name = ?2", xmlId, name)
            .fetchOne();

    if (metaSelect != null) {
      return metaSelect.getPriority() + 1;
    }

    return null;
  }
}
