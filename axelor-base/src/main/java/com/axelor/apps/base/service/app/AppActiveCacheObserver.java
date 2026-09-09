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
package com.axelor.apps.base.service.app;

import com.axelor.db.Model;
import com.axelor.event.Observes;
import com.axelor.events.internal.BeforeTransactionComplete;
import com.axelor.studio.db.App;
import jakarta.inject.Singleton;
import java.util.Set;

/**
 * Clears {@link AppActiveCache} when an app record is created, updated or removed, so installing or
 * uninstalling an app is taken into account by the app checks of the service overrides.
 */
@Singleton
public class AppActiveCacheObserver {

  void onBeforeTransactionComplete(@Observes BeforeTransactionComplete event) {
    if (containsApp(event.getUpdated()) || containsApp(event.getDeleted())) {
      getCache().clear();
    }
  }

  protected AppActiveCache getCache() {
    return AppActiveCache.get();
  }

  protected boolean containsApp(Set<? extends Model> models) {
    return models != null && models.stream().anyMatch(App.class::isInstance);
  }
}
