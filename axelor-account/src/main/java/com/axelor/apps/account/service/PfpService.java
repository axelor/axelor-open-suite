package com.axelor.apps.account.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;

public interface PfpService {

  boolean isManagePassedForPayment(Company company) throws AxelorException;

  boolean isManagePFPInRefund(Company company) throws AxelorException;

  boolean isManageDaybookInPFP(Company company) throws AxelorException;
}
