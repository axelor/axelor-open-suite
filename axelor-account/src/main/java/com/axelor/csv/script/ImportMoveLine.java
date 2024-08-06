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

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AccountAccountRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.TaxLineRepository;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.common.StringUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.Map;

public class ImportMoveLine {
  private MoveLineRepository moveLineRepository;
  private AccountAccountRepository accountRepository;
  private TaxLineRepository taxLineRepository;
  private PartnerRepository partnerRepository;

  @Inject
  public ImportMoveLine(
      MoveLineRepository moveLineRepository,
      AccountAccountRepository accountAccountRepository,
      TaxLineRepository taxLineRepository,
      PartnerRepository partnerRepository) {
    this.moveLineRepository = moveLineRepository;
    this.accountRepository = accountAccountRepository;
    this.taxLineRepository = taxLineRepository;
    this.partnerRepository = partnerRepository;
  }

  @Transactional
  public Object importMoveLine(Object bean, Map<String, Object> values) {
    assert bean instanceof MoveLine;
    MoveLine moveLine = (MoveLine) bean;

    String accountId = (String) values.get("account_importId");
    Account account = getAccount(accountId);
    if (account != null) {
      moveLine.setAccountCode(account.getCode());
      moveLine.setAccountName(account.getName());
    } else {
      moveLine.setAccountCode((String) values.get("accountCode"));
      moveLine.setAccountName((String) values.get("accountName"));
    }

    String taxLineId = (String) values.get("taxLine_importId");
    TaxLine taxLine = getTaxLine(taxLineId);
    if (taxLine != null) {
      moveLine.setTaxCode(taxLine.getTax().getCode());
      moveLine.setTaxRate(taxLine.getValue());
    } else {
      moveLine.setTaxCode((String) values.get("taxCode"));
      moveLine.setTaxRate(new BigDecimal((String) values.get("taxRate")));
    }

    String partnerId = (String) values.get("partner_importId");
    Partner partner = getPartner(partnerId);
    if (partner != null) {
      moveLine.setPartnerSeq(partner.getPartnerSeq());
      moveLine.setPartnerFullName(partner.getSimpleFullName());
    } else {
      moveLine.setPartnerSeq((String) values.get("partnerSeq"));
      moveLine.setPartnerFullName((String) values.get("partnerSimpleFullName"));
    }

    moveLineRepository.save(moveLine);
    return moveLine;
  }

  protected Account getAccount(String accountId) {
    if (StringUtils.notBlank(accountId)) {
      Account account =
          accountRepository
              .all()
              .filter("self.id = :account")
              .bind("account", accountId)
              .fetchOne();
      return account;
    }
    return null;
  }

  protected TaxLine getTaxLine(String taxLineId) {
    if (StringUtils.notBlank(taxLineId)) {
      TaxLine taxLine =
          taxLineRepository
              .all()
              .filter("self.id = :taxLine")
              .bind("taxLine", taxLineId)
              .fetchOne();
      return taxLine;
    }
    return null;
  }

  protected Partner getPartner(String partnerId) {
    if (StringUtils.notBlank(partnerId)) {
      Partner partner =
          partnerRepository
              .all()
              .filter("self.id = :partner")
              .bind("partner", partnerId)
              .fetchOne();
      return partner;
    }
    return null;
  }
}
