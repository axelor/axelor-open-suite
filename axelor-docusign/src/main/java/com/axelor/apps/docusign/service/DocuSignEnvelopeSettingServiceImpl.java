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
package com.axelor.apps.docusign.service;

import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaSelect;
import com.axelor.meta.db.MetaSelectItem;
import com.axelor.meta.db.repo.MetaSelectItemRepository;
import com.axelor.meta.db.repo.MetaSelectRepository;
import com.google.inject.persist.Transactional;
import java.util.List;

public class DocuSignEnvelopeSettingServiceImpl implements DocuSignEnvelopeSettingService {

  @Override
  @Transactional
  public void addItemToReferenceSelection(MetaModel model) {
    MetaSelect metaSelect =
        Beans.get(MetaSelectRepository.class).findByName("docusign.envelope.related.to.select");
    List<MetaSelectItem> items = metaSelect.getItems();
    if (items != null && !items.stream().anyMatch(x -> x.getValue().equals(model.getFullName()))) {
      MetaSelectItem metaSelectItem = new MetaSelectItem();
      metaSelectItem.setTitle(model.getName());
      metaSelectItem.setValue(model.getFullName());
      metaSelectItem.setSelect(metaSelect);
      Beans.get(MetaSelectItemRepository.class).save(metaSelectItem);
    }
  }
}
