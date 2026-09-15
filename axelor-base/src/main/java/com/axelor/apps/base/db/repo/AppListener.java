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
package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.studio.db.App;
import com.axelor.studio.helper.TransactionHelper;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;

/**
 * Drops the cached app active flags when an app is installed, uninstalled or removed, so the app
 * checks guarding the service overrides take the change into account.
 *
 * <p>The cache is cleared after the commit only: clearing it on flush would let a concurrent check
 * cache the value read before the commit.
 */
public class AppListener {

  @PostPersist
  @PostUpdate
  @PostRemove
  protected void onAppChange(App app) {
    TransactionHelper.runAfterCommit(AppBaseServiceImpl::invalidateAppActiveCache);
  }
}
