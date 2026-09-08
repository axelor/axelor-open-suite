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
package com.axelor.apps.crm.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.DuplicateObjectsService;
import com.axelor.db.JPA;
import com.axelor.db.mapper.Mapper;
import com.google.inject.persist.Transactional;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

@Singleton
public class DuplicateObjectsCrmService extends DuplicateObjectsService {

  @Transactional
  public void removeDuplicate(List<Long> selectedIds, String modelName) {
    List<Object> duplicateObjects = getDuplicateObject(selectedIds, modelName);
    Object originalObjct = getOriginalObject(selectedIds, modelName);
    managePartnerEvents(duplicateObjects, originalObjct);

    super.removeDuplicate(selectedIds, modelName);
  }

  protected void managePartnerEvents(List<Object> duplicateObjects, Object originalObject) {
    if (!(originalObject instanceof Partner) || CollectionUtils.isEmpty(duplicateObjects)) {
      return;
    }
    Partner originalPartner = (Partner) originalObject;
    List<Long> duplicateIds =
        duplicateObjects.stream()
            .map(o -> (Long) Mapper.of(o.getClass()).get(o, "id"))
            .collect(Collectors.toList());

    JPA.em()
        .createQuery(
            "UPDATE com.axelor.apps.crm.db.Event self SET self.partner = :original WHERE self.partner IN (:duplicates)")
        .setParameter("original", originalPartner)
        .setParameter("duplicates", duplicateObjects)
        .executeUpdate();
    JPA.em()
        .createQuery(
            "UPDATE com.axelor.apps.crm.db.Event self SET self.contactPartner = :original WHERE self.contactPartner IN (:duplicates)")
        .setParameter("original", originalPartner)
        .setParameter("duplicates", duplicateObjects)
        .executeUpdate();
    JPA.em()
        .createQuery(
            "UPDATE com.axelor.apps.crm.db.Event self SET self.relatedToSelectId = :originalId WHERE self.relatedToSelect = 'com.axelor.apps.base.db.Partner' AND self.relatedToSelectId IN (:duplicateIds)")
        .setParameter("originalId", originalPartner.getId())
        .setParameter("duplicateIds", duplicateIds)
        .executeUpdate();
  }
}
