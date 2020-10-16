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
package com.axelor.studio.db.repo;

import com.axelor.studio.db.Wkf;
import com.axelor.studio.db.WkfNode;
import com.axelor.studio.service.wkf.WkfService;
import com.google.inject.Inject;

public class StudioWkfRepository extends WkfRepository {

  @Inject private WkfService wkfService;

  /**
   * Overridden to remove changes related with workflow. Like to remove buttons and status field
   * from view and model.
   */
  @Override
  public void remove(Wkf wkf) {

    wkfService.clearWkf(wkf);

    super.remove(wkf);
  }

  @Override
  public Wkf copy(Wkf wkf, boolean deep) {

    wkf = super.copy(wkf, deep);
    for (WkfNode node : wkf.getNodes()) {
      node.setIsGenerateMenu(false);
      node.setMenuBuilder(null);
    }

    return wkf;
  }
}
