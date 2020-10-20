package com.axelor.csv.script;

import com.axelor.apps.base.db.Bank;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.repo.BankRepository;
import com.axelor.apps.base.db.repo.CountryRepository;
import com.axelor.exception.AxelorException;
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
