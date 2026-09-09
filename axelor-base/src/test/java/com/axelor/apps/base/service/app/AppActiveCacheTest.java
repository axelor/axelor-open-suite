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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AppActiveCacheTest {

  protected static final long TTL_IN_MILLIS = 60_000L;

  private final Map<String, Boolean> databaseValues = new HashMap<>();

  private final AtomicInteger loadCount = new AtomicInteger();

  private final AtomicLong currentTime = new AtomicLong();

  private final AtomicReference<String> currentTenant = new AtomicReference<>();

  private AppActiveCache cache;

  @BeforeEach
  void setUp() {
    databaseValues.clear();
    loadCount.set(0);
    currentTime.set(1_000L);
    currentTenant.set(null);
    cache = new AppActiveCache(this::load, currentTenant::get, TTL_IN_MILLIS, currentTime::get);
  }

  protected Boolean load(String code) {
    loadCount.incrementAndGet();
    return databaseValues.get(code);
  }

  @Test
  void isActive_readsTheDatabaseOnceThenServesFromMemory() {
    databaseValues.put("budget", true);

    assertTrue(cache.isActive("budget"));
    assertTrue(cache.isActive("budget"));
    assertTrue(cache.isActive("budget"));

    assertEquals(1, loadCount.get());
  }

  @Test
  void isActive_isFalseWhenTheAppDoesNotExist() {
    assertFalse(cache.isActive("budget"));
    assertEquals(1, loadCount.get());
  }

  @Test
  void isActive_isFalseWhenTheAppIsNotActive() {
    databaseValues.put("budget", false);

    assertFalse(cache.isActive("budget"));
  }

  @Test
  void isActive_cachesEachAppCodeSeparately() {
    databaseValues.put("budget", true);
    databaseValues.put("supplychain", false);

    assertTrue(cache.isActive("budget"));
    assertFalse(cache.isActive("supplychain"));
    assertTrue(cache.isActive("budget"));

    assertEquals(2, loadCount.get());
  }

  @Test
  void isActive_doesNotServeTheValueOfATenantToAnotherOne() {
    databaseValues.put("budget", true);
    currentTenant.set("tenant-one");
    assertTrue(cache.isActive("budget"));

    databaseValues.put("budget", false);
    currentTenant.set("tenant-two");
    assertFalse(cache.isActive("budget"));

    currentTenant.set("tenant-one");
    assertTrue(cache.isActive("budget"));

    assertEquals(2, loadCount.get());
  }

  @Test
  void clear_makesTheNextCheckReadTheDatabaseAgain() {
    databaseValues.put("budget", false);
    assertFalse(cache.isActive("budget"));

    databaseValues.put("budget", true);
    assertFalse(cache.isActive("budget"));

    cache.clear();

    assertTrue(cache.isActive("budget"));
    assertEquals(2, loadCount.get());
  }

  @Test
  void isActive_readsTheDatabaseAgainOnceTheTimeToLiveHasExpired() {
    databaseValues.put("budget", false);
    assertFalse(cache.isActive("budget"));

    databaseValues.put("budget", true);

    currentTime.addAndGet(TTL_IN_MILLIS - 1_000L);
    assertFalse(cache.isActive("budget"));

    currentTime.addAndGet(2_000L);
    assertTrue(cache.isActive("budget"));
  }
}
