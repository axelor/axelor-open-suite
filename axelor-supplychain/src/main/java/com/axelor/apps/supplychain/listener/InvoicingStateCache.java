/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.listener;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.supplychain.service.SaleInvoicingStateService;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Singleton
public class InvoicingStateCache {

  private final long CACHE_MAXIMUM_SIZE = 100000;
  private final long CACHE_EXPIRE_TIME = 7;

  private final LoadingCache<Long, Integer> cache =
      CacheBuilder.newBuilder()
          .maximumSize(CACHE_MAXIMUM_SIZE)
          .expireAfterAccess(CACHE_EXPIRE_TIME, TimeUnit.DAYS)
          .build(
              new CacheLoader<Long, Integer>() {
                @Override
                public Integer load(Long key) {
                  return getInvoicingState(key);
                }
              });

  protected SaleOrderRepository saleOrderRepository;
  protected SaleInvoicingStateService saleInvoicingStateService;
  protected SaleOrderInvoiceService saleOrderInvoiceService;

  @Inject
  public InvoicingStateCache(
      SaleOrderRepository saleOrderRepository,
      SaleInvoicingStateService saleInvoicingStateService,
      SaleOrderInvoiceService saleOrderInvoiceService) {
    this.saleOrderRepository = saleOrderRepository;
    this.saleInvoicingStateService = saleInvoicingStateService;
    this.saleOrderInvoiceService = saleOrderInvoiceService;
  }

  public int getInvoicingState(Long id) {
    SaleOrder saleOrder = saleOrderRepository.find(id);
    return this.saleInvoicingStateService.getInvoicingState(
        saleOrder.getAmountInvoiced(),
        saleOrder.getExTaxTotal(),
        saleOrderInvoiceService.atLeastOneInvoiceIsVentilated(saleOrder));
  }

  public Integer getInvoicingStateFromCache(Long id) throws ExecutionException {
    return cache.get(id);
  }

  public void invalidateSaleOrder(Long id) {
    cache.invalidate(id);
  }
}
