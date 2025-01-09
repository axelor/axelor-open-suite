/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.wizard;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.wizard.BaseConvertLeadWizardService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import java.util.Optional;

public class AccountConvertLeadWizardServiceImpl implements BaseConvertLeadWizardService {

  @Override
  public void setPartnerFields(Partner partner) {
    Company activeCompany =
        Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null);
    if (activeCompany != null) {
      AccountConfig accountConfig =
          Beans.get(AccountConfigRepository.class).findByCompany(activeCompany);
      PaymentMode inPaymentMode = accountConfig.getInPaymentMode();
      PaymentMode outPaymentMode = accountConfig.getOutPaymentMode();
      PaymentCondition paymentCondition = accountConfig.getDefPaymentCondition();

      partner.setInPaymentMode(inPaymentMode);
      partner.setOutPaymentMode(outPaymentMode);
      partner.setPaymentCondition(paymentCondition);
      partner.setPayNoticeSendingMethodSelect(PartnerRepository.PAYMENT_NOTICE_EMAIL);

      if (!partner.getIsContact()) {
        partner.setInvoiceSendingFormatSelect("emailpaper");
      }
    }
  }
}
