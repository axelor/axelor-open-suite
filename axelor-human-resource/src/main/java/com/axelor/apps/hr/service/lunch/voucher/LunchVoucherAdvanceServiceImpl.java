/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.lunch.voucher;

import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.LunchVoucherAdvance;
import com.axelor.apps.hr.db.repo.HRConfigRepository;
import com.axelor.apps.hr.db.repo.LunchVoucherAdvanceRepository;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class LunchVoucherAdvanceServiceImpl implements LunchVoucherAdvanceService {

  protected HRConfigService hrConfigService;

  @Inject
  public LunchVoucherAdvanceServiceImpl(HRConfigService hrConfigService) {
    this.hrConfigService = hrConfigService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void onNewAdvance(LunchVoucherAdvance lunchVoucherAdvance) throws AxelorException {

    HRConfig config =
        hrConfigService.getHRConfig(
            lunchVoucherAdvance.getEmployee().getMainEmploymentContract().getPayCompany());
    config.setAvailableStockLunchVoucher(
        config.getAvailableStockLunchVoucher() - lunchVoucherAdvance.getNbrLunchVouchers());

    Beans.get(LunchVoucherAdvanceRepository.class).save(lunchVoucherAdvance);
    Beans.get(HRConfigRepository.class).save(config);
  }

  @Override
  public int useAdvance(LunchVoucherAdvance lunchVoucherAdvance, int qty) throws AxelorException {
    int toUse =
        lunchVoucherAdvance.getNbrLunchVouchers() - lunchVoucherAdvance.getNbrLunchVouchersUsed();

    if (qty > toUse) {
      lunchVoucherAdvance.setNbrLunchVouchersUsed(lunchVoucherAdvance.getNbrLunchVouchers());
      return qty - toUse;
    }

    lunchVoucherAdvance.setNbrLunchVouchersUsed(
        lunchVoucherAdvance.getNbrLunchVouchersUsed() + qty);
    return 0;
  }
}
