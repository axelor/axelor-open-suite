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
import com.axelor.apps.base.db.Bank;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.repo.BankRepository;
import com.axelor.apps.base.db.repo.CountryRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Map;

public class ImportBank {

  private CountryRepository countryRepository;
  private BankRepository bankRepository;

  @Inject
  public ImportBank(CountryRepository countryRepository, BankRepository bankRepository) {
    this.countryRepository = countryRepository;
    this.bankRepository = bankRepository;
  }

  @Transactional(rollbackOn = Exception.class)
  public Object setCountry(Object bean, Map<String, Object> values) throws AxelorException {
    assert bean instanceof Bank;
    Bank bank = (Bank) bean;
    String code = (String) values.get("code");
    String codeCountry = code.substring(4, 6);
    Country country =
        countryRepository
            .all()
            .filter("self.alpha2Code = :codeCountry")
            .bind("codeCountry", codeCountry)
            .fetchOne();
    if (country != null) {
      bank.setCountry(country);
    }

    bankRepository.save(bank);
    return bank;
  }
}
