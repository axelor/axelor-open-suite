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
package com.axelor.apps.purchase.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.purchase.db.CallTenderNeed;
import com.axelor.apps.purchase.db.repo.CallTenderNeedRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class CallTenderNeedServiceImpl implements CallTenderNeedService {

  protected final CallTenderNeedRepository callTenderNeedRepository;

  @Inject
  public CallTenderNeedServiceImpl(CallTenderNeedRepository callTenderNeedRepository) {
    this.callTenderNeedRepository = callTenderNeedRepository;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public CallTenderNeed createCallTenderNeed(
      Product product, BigDecimal quantity, Unit unit, LocalDate date, int typeSelect) {
    Objects.requireNonNull(product);

    var callTenderNeed = new CallTenderNeed();

    callTenderNeed.setProduct(product);
    callTenderNeed.setUnit(unit);
    callTenderNeed.setRequestedQty(quantity);
    callTenderNeed.setRequestedDate(date);
    callTenderNeed.setTypeSelect(typeSelect);

    return callTenderNeedRepository.save(callTenderNeed);
  }
}
