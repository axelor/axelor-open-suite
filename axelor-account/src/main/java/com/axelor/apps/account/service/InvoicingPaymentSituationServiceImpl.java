/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.InvoicingPaymentSituation;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.utils.helpers.StringHelper;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InvoicingPaymentSituationServiceImpl implements InvoicingPaymentSituationService {

  @Inject
  public InvoicingPaymentSituationServiceImpl() {}

  @Override
  public String getCompanyDomain(
      InvoicingPaymentSituation invoicingPaymentSituation, Partner partner) {
    if (invoicingPaymentSituation == null || partner == null) {
      return "self.id = 0";
    }
    String domain = "(self.archived = false OR self.archived is null)";
    List<InvoicingPaymentSituation> partnerInvoicingPaymentSituationList =
        new ArrayList<>(partner.getInvoicingPaymentSituationList());
    partnerInvoicingPaymentSituationList.remove(invoicingPaymentSituation);
    if (ObjectUtils.isEmpty(partnerInvoicingPaymentSituationList)) {
      return domain;
    }

    // A company can hold one situation per available bank details of the partner. It must only be
    // excluded once every available (company, bankDetails) couple is already used.
    int availableBankDetailsCount =
        ObjectUtils.isEmpty(partner.getBankDetailsList()) ? 0 : partner.getBankDetailsList().size();

    Map<Company, Long> situationCountByCompany =
        partnerInvoicingPaymentSituationList.stream()
            .filter(situation -> situation.getCompany() != null)
            .collect(
                Collectors.groupingBy(
                    InvoicingPaymentSituation::getCompany, Collectors.counting()));

    List<Company> fullyUsedCompanyList =
        situationCountByCompany.entrySet().stream()
            .filter(entry -> entry.getValue() >= availableBankDetailsCount)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

    if (ObjectUtils.isEmpty(fullyUsedCompanyList)) {
      return domain;
    }

    domain =
        domain.concat(
            String.format(
                " AND self.id NOT IN (%s)", StringHelper.getIdListString(fullyUsedCompanyList)));

    return domain;
  }

  @Override
  public String getBankDetailsDomain(
      InvoicingPaymentSituation invoicingPaymentSituation, Partner partner) {
    if (partner == null) {
      return "self.id = 0";
    }
    if (invoicingPaymentSituation == null || invoicingPaymentSituation.getCompany() == null) {
      return "self.id IN (" + StringHelper.getIdListString(partner.getBankDetailsList()) + ")";
    }

    // Only bank details still available for the selected company are selectable, so each
    // (company, bankDetails) couple stays unique.
    return "self.id IN ("
        + StringHelper.getIdListString(
            getAvailableBankDetailsList(
                invoicingPaymentSituation, partner, invoicingPaymentSituation.getCompany()))
        + ")";
  }

  @Override
  public List<BankDetails> getAvailableBankDetailsList(
      InvoicingPaymentSituation invoicingPaymentSituation, Partner partner, Company company) {
    if (partner == null || company == null || ObjectUtils.isEmpty(partner.getBankDetailsList())) {
      return new ArrayList<>();
    }

    List<InvoicingPaymentSituation> partnerInvoicingPaymentSituationList =
        new ArrayList<>(partner.getInvoicingPaymentSituationList());
    partnerInvoicingPaymentSituationList.remove(invoicingPaymentSituation);

    List<BankDetails> usedBankDetailsList =
        partnerInvoicingPaymentSituationList.stream()
            .filter(
                situation ->
                    company.equals(situation.getCompany()) && situation.getBankDetails() != null)
            .map(InvoicingPaymentSituation::getBankDetails)
            .collect(Collectors.toList());

    return partner.getBankDetailsList().stream()
        .filter(bankDetails -> !usedBankDetailsList.contains(bankDetails))
        .collect(Collectors.toList());
  }

  @Override
  public InvoicingPaymentSituation initInvoicingPaymentSituation(
      InvoicingPaymentSituation invoicingPaymentSituation, Partner partner) {
    invoicingPaymentSituation.setPartner(partner);
    invoicingPaymentSituation.setUmrList(new ArrayList<>());

    Company defaultCompany =
        Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null);
    if (defaultCompany == null || partner == null) {
      return invoicingPaymentSituation;
    }

    List<InvoicingPaymentSituation> situationList =
        Optional.ofNullable(partner.getInvoicingPaymentSituationList()).orElse(new ArrayList<>());
    long companySituationCount =
        situationList.stream()
            .filter(situation -> defaultCompany.equals(situation.getCompany()))
            .count();
    int availableBankDetailsCount =
        ObjectUtils.isEmpty(partner.getBankDetailsList()) ? 0 : partner.getBankDetailsList().size();

    // The default company is pre-filled as long as it still has an available (company, bankDetails)
    // slot left.
    if (companySituationCount > 0 && companySituationCount >= availableBankDetailsCount) {
      return invoicingPaymentSituation;
    }
    invoicingPaymentSituation.setCompany(defaultCompany);

    // When a single bank details remains available for that company, pre-fill it as well.
    List<BankDetails> availableBankDetailsList =
        getAvailableBankDetailsList(invoicingPaymentSituation, partner, defaultCompany);
    if (availableBankDetailsList.size() == 1) {
      invoicingPaymentSituation.setBankDetails(availableBankDetailsList.get(0));
    }

    return invoicingPaymentSituation;
  }
}
