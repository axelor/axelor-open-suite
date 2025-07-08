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
package com.axelor.apps.stock.service;

import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.db.JPA;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class StockLocationFetchServiceImpl implements StockLocationFetchService {

  private final ThreadLocal<Map<String, List<Long>>> threadCache =
      ThreadLocal.withInitial(HashMap::new);

  @Override
  public List<Long> getAllContentLocationAndSubLocation(Long stockLocationId) {
    return getAllLocationAndSubLocation(stockLocationId, false);
  }

  @Override
  public List<Long> getAllLocationAndSubLocation(Long stockLocationId, boolean isVirtualInclude) {
    return getAllLocationAndSubLocation(stockLocationId, isVirtualInclude, null);
  }

  @Override
  public List<Long> getAllLocationAndSubLocation(
      Long stockLocationId, boolean isVirtualInclude, List<Predicate> extraFilters) {
    if (stockLocationId == null) {
      return Collections.emptyList();
    }

    return CollectionUtils.isEmpty(extraFilters)
        ? getCachedOrFetch(stockLocationId, isVirtualInclude)
        : fetchAllLocationAndSubLocation(stockLocationId, isVirtualInclude, extraFilters);
  }

  protected List<Long> getCachedOrFetch(Long stockLocationId, boolean isVirtualInclude) {
    final Map<String, List<Long>> cache = threadCache.get();
    final String key = buildCacheKey(stockLocationId, isVirtualInclude);

    List<Long> cached = cache.get(key);
    if (cached != null) {
      return cached;
    }
    List<Long> result = fetchAllLocationAndSubLocation(stockLocationId, isVirtualInclude, null);
    cache.put(key, result);
    return result;
  }

  protected String buildCacheKey(Long stockLocationId, boolean isVirtualInclude) {
    return stockLocationId + "::" + isVirtualInclude;
  }

  protected List<Long> fetchAllLocationAndSubLocation(
      Long stockLocationId, boolean isVirtualInclude, List<Predicate> extraFilters) {
    if (stockLocationId == null) {
      return Collections.emptyList();
    }

    Queue<Long> queue = new LinkedList<>();
    Set<Long> visited = new LinkedHashSet<>();

    queue.add(stockLocationId);

    while (!queue.isEmpty()) {
      Long currentId = queue.poll();
      if (currentId == null || !visited.add(currentId)) {
        continue;
      }

      List<Long> childIds = fetchChildrenIds(currentId, isVirtualInclude, extraFilters);
      for (Long childId : childIds) {
        if (childId != null && !visited.contains(childId)) {
          queue.add(childId);
        }
      }
    }
    return new ArrayList<>(visited);
  }

  protected List<Long> fetchChildrenIds(
      Long parentId, boolean isVirtualInclude, List<Predicate> extraFilters) {
    EntityManager em = JPA.em();
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
    Root<StockLocation> root = cq.from(StockLocation.class);

    cq.select(root.get("id"));

    List<Predicate> predicates = new ArrayList<>();
    predicates.add(cb.equal(root.get("parentStockLocation").get("id"), parentId));

    if (!isVirtualInclude) {
      predicates.add(cb.notEqual(root.get("typeSelect"), StockLocationRepository.TYPE_VIRTUAL));
    }

    if (CollectionUtils.isNotEmpty(extraFilters)) {
      predicates.addAll(extraFilters);
    }

    cq.where(predicates.toArray(new Predicate[0]));

    return em.createQuery(cq).getResultList();
  }

  @Override
  public Set<Long> getLocationAndAllParentLocationIds(StockLocation stockLocation) {
    Set<Long> locationIds = new LinkedHashSet<>();
    if (stockLocation == null) {
      return locationIds;
    }

    while (stockLocation != null && locationIds.add(stockLocation.getId())) {
      stockLocation = stockLocation.getParentStockLocation();
    }

    return locationIds;
  }
}
