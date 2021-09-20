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
package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.App;
import com.axelor.apps.base.module.AdminModule;
import com.axelor.db.JPA;
import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.persistence.EntityGraph;

@Alternative
@Priority(AdminModule.PRIORITY)
public class AppAdminRepository extends AppRepository {

  @Override
  public App find(Long id) {
    final EntityGraph<App> entityGraph = JPA.em().createEntityGraph(App.class);
    entityGraph.addAttributeNodes("dependsOnSet");
    return JPA.em()
        .createQuery("select self from App self where self.id = :id", App.class)
        .setHint("javax.persistence.fetchgraph", entityGraph)
        .setParameter("id", id)
        .getSingleResult();
  }
}
