/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.mobilesettings.service;

import com.axelor.apps.mobilesettings.db.MobileDashboard;
import com.axelor.apps.mobilesettings.db.MobileDashboardLine;
import com.axelor.apps.mobilesettings.db.repo.MobileDashboardLineRepository;
import com.google.inject.Inject;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class MobileDashboardLineRemoveServiceImpl implements MobileDashboardLineRemoveService {

  protected MobileDashboardLineRepository mobileDashboardLineRepository;

  @Inject
  public MobileDashboardLineRemoveServiceImpl(
      MobileDashboardLineRepository mobileDashboardLineRepository) {
    this.mobileDashboardLineRepository = mobileDashboardLineRepository;
  }

  @Override
  public void deletesLines(MobileDashboard mobileDashboard) {
    List<MobileDashboardLine> mobileDashboardLineList =
        mobileDashboard.getMobileDashboardLineList();
    if (CollectionUtils.isEmpty(mobileDashboardLineList)) {
      return;
    }
    mobileDashboard
        .getMobileDashboardLineList()
        .forEach(line -> mobileDashboardLineRepository.remove(line));
  }
}
