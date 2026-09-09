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

import com.axelor.db.JPA;
import com.axelor.db.tenants.TenantResolver;
import jakarta.persistence.FlushModeType;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * In memory cache of the active flag of the apps, keyed by tenant and app code.
 *
 * <p>The app check is called on every cross-module service override, so it must not hit the
 * database nor trigger a flush of the persistence context. The value is loaded once per app code
 * through a scalar projection, then served from memory.
 *
 * <p>Values are keyed by tenant, as each tenant has its own database and installs its own apps.
 *
 * <p>The cache is cleared by {@link AppActiveCacheObserver} as soon as an app is saved or removed.
 * A time to live also bounds how long a stale value can survive when the app is installed on
 * another node of a cluster.
 */
public class AppActiveCache {

  protected static final long DEFAULT_TTL_IN_MILLIS = 60_000L;

  protected static final String ACTIVE_FLAG_QUERY =
      "select self.active from App self where self.code = :code";

  private static final AppActiveCache INSTANCE =
      new AppActiveCache(
          AppActiveCache::fetchActiveFlag,
          TenantResolver::currentTenantIdentifier,
          DEFAULT_TTL_IN_MILLIS,
          System::currentTimeMillis);

  protected final Map<String, Boolean> activeByKey = new ConcurrentHashMap<>();

  protected final Function<String, Boolean> loader;

  protected final Supplier<String> tenantSupplier;

  protected final long ttlInMillis;

  protected final LongSupplier clock;

  protected final AtomicLong lastClearTime;

  protected AppActiveCache(
      Function<String, Boolean> loader,
      Supplier<String> tenantSupplier,
      long ttlInMillis,
      LongSupplier clock) {
    this.loader = loader;
    this.tenantSupplier = tenantSupplier;
    this.ttlInMillis = ttlInMillis;
    this.clock = clock;
    this.lastClearTime = new AtomicLong(clock.getAsLong());
  }

  /** Returns the cache used by the application. */
  public static AppActiveCache get() {
    return INSTANCE;
  }

  /**
   * Whether the app with the given code is installed and active.
   *
   * @param code the app code, as stored on the app record
   * @return true when the app exists and is active, false otherwise
   */
  public boolean isActive(String code) {
    clearIfExpired();

    String key = cacheKey(code);
    Boolean active = activeByKey.get(key);
    if (active == null) {
      active = Boolean.TRUE.equals(loader.apply(code));
      activeByKey.put(key, active);
    }

    return active;
  }

  /** Drops every cached value, so the next check reloads it from the database. */
  public void clear() {
    activeByKey.clear();
    lastClearTime.set(clock.getAsLong());
  }

  protected void clearIfExpired() {
    long lastClear = lastClearTime.get();
    long now = clock.getAsLong();

    if (now - lastClear >= ttlInMillis && lastClearTime.compareAndSet(lastClear, now)) {
      activeByKey.clear();
    }
  }

  /** Values of a tenant must never be served to another one. */
  protected String cacheKey(String code) {
    String tenantId = tenantSupplier.get();
    return tenantId == null ? code : tenantId + '|' + code;
  }

  /**
   * Reads the active flag from the database, without hydrating the app entity and without flushing
   * the persistence context.
   */
  protected static Boolean fetchActiveFlag(String code) {
    List<Boolean> results =
        JPA.em()
            .createQuery(ACTIVE_FLAG_QUERY, Boolean.class)
            .setParameter("code", code)
            .setFlushMode(FlushModeType.COMMIT)
            .setMaxResults(1)
            .getResultList();

    return !results.isEmpty() && Boolean.TRUE.equals(results.get(0));
  }
}
