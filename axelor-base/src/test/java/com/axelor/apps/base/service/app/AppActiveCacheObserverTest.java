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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.axelor.apps.base.db.Company;
import com.axelor.db.Model;
import com.axelor.events.internal.BeforeTransactionComplete;
import com.axelor.studio.db.App;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AppActiveCacheObserverTest {

  private final AtomicInteger loadCount = new AtomicInteger();

  private AppActiveCache cache;

  private AppActiveCacheObserver observer;

  @BeforeEach
  void setUp() {
    loadCount.set(0);
    cache =
        new AppActiveCache(
            code -> {
              loadCount.incrementAndGet();
              return true;
            },
            () -> null,
            60_000L,
            () -> 0L);
    observer =
        new AppActiveCacheObserver() {
          @Override
          protected AppActiveCache getCache() {
            return cache;
          }
        };
  }

  @Test
  void onBeforeTransactionComplete_clearsTheCacheWhenAnAppIsUpdated() {
    warmUpCache();

    observer.onBeforeTransactionComplete(event(Set.of(new App()), Set.of()));

    assertEquals(2, checkTwice());
  }

  @Test
  void onBeforeTransactionComplete_clearsTheCacheWhenAnAppIsDeleted() {
    warmUpCache();

    observer.onBeforeTransactionComplete(event(Set.of(), Set.of(new App())));

    assertEquals(2, checkTwice());
  }

  @Test
  void onBeforeTransactionComplete_keepsTheCacheWhenNoAppIsChanged() {
    warmUpCache();

    observer.onBeforeTransactionComplete(event(Set.of(new Company()), Set.of(new Company())));

    assertEquals(1, checkTwice());
  }

  protected void warmUpCache() {
    cache.isActive("budget");
    assertEquals(1, loadCount.get());
  }

  protected int checkTwice() {
    cache.isActive("budget");
    return loadCount.get();
  }

  protected BeforeTransactionComplete event(
      Set<? extends Model> updated, Set<? extends Model> deleted) {
    return new BeforeTransactionComplete(updated, deleted);
  }
}
