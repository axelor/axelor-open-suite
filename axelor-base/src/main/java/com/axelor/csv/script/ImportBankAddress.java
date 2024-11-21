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
package com.axelor.csv.script;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankAddress;
import com.axelor.apps.base.db.repo.BankAddressRepository;
import com.axelor.apps.base.service.BankAddressService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Map;

public class ImportBankAddress {

  private BankAddressRepository bankAddressRepository;
  private BankAddressService bankAddressService;

  @Inject
  public ImportBankAddress(
      BankAddressRepository bankAddressRepository, BankAddressService bankAddressService) {
    this.bankAddressRepository = bankAddressRepository;
    this.bankAddressService = bankAddressService;
  }

  @Transactional(rollbackOn = Exception.class)
  public Object setFullName(Object bean, Map<String, Object> values) throws AxelorException {
    assert bean instanceof BankAddress;
    BankAddress bankAddress = (BankAddress) bean;
    bankAddress.setFullAddress(bankAddressService.computeFullAddress(bankAddress));
    bankAddressRepository.save(bankAddress);
    return bankAddress;
  }
}
