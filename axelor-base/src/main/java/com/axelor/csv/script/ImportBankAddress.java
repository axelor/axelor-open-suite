package com.axelor.csv.script;

import com.axelor.apps.base.db.BankAddress;
import com.axelor.apps.base.db.repo.BankAddressRepository;
import com.axelor.apps.base.service.BankAddressService;
import com.axelor.exception.AxelorException;
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
