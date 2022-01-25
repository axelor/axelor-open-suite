/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.projectdms.db.repo;

import com.axelor.common.Inflector;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.dms.db.DMSFile;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.axelor.i18n.I18n;
import com.axelor.rpc.filter.Filter;
import com.axelor.rpc.filter.JPQLFilter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.List;

public class DMSFileManagementRepository extends DMSFileRepository {

  /**
   * Finds or creates parent folders.
   *
   * @param related model
   * @return home parent
   */
  @Override
  protected DMSFile findOrCreateHome(Model related) {
    final List<Filter> dmsRootFilters =
        Lists.newArrayList(
            new JPQLFilter(
                ""
                    + "COALESCE(self.isDirectory, FALSE) = TRUE "
                    + "AND self.relatedModel = :model "
                    + "AND COALESCE(self.relatedId, 0) = 0"));
    final DMSFile dmsRootParent = getRootParent(related);

    if (dmsRootParent != null) {
      dmsRootFilters.add(new JPQLFilter("self.parent = :rootParent"));
    }

    DMSFile dmsRoot =
        Filter.and(dmsRootFilters)
            .build(DMSFile.class)
            .bind("model", related.getClass().getName())
            .bind("rootParent", dmsRootParent)
            .fetchOne();

    if (dmsRoot == null) {
      final Inflector inflector = Inflector.getInstance();
      dmsRoot = new DMSFile();
      dmsRoot.setFileName(
          I18n.get(inflector.pluralize(inflector.humanize(related.getClass().getSimpleName()))));
      dmsRoot.setRelatedModel(related.getClass().getName());
      dmsRoot.setIsDirectory(true);
      dmsRoot.setParent(dmsRootParent);
      dmsRoot = save(dmsRoot); // Should get id before its child.
    } else {
      dmsRoot.setFileName(I18n.get(dmsRoot.getFileName()));
    }

    DMSFile dmsHome = findHomeByRelated(related);

    if (dmsHome == null) {
      String homeName = null;

      try {
        final Mapper mapper = Mapper.of(related.getClass());
        homeName = mapper.getNameField().get(related).toString();
      } catch (Exception e) {
        // Ignore
      }

      if (homeName == null) {
        homeName = Strings.padStart("" + related.getId(), 5, '0');
      }

      dmsHome = new DMSFile();
      dmsHome.setFileName(homeName);
      dmsHome.setRelatedId(related.getId());
      dmsHome.setRelatedModel(related.getClass().getName());
      dmsHome.setParent(dmsRoot);
      dmsHome.setIsDirectory(true);
      dmsHome = save(dmsHome); // Should get id before its child.
    }

    return dmsHome;
  }
}
