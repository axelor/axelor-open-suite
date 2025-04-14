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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.TaxNumber;
import com.axelor.apps.account.db.repo.FiscalPositionRepository;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class InvoiceDomainServiceImpl implements InvoiceDomainService {

  protected InvoiceService invoiceService;
  protected FiscalPositionRepository fiscalPositionRepository;

  @Inject
  public InvoiceDomainServiceImpl(
      InvoiceService invoiceService, FiscalPositionRepository fiscalPositionRepository) {
    this.invoiceService = invoiceService;
    this.fiscalPositionRepository = fiscalPositionRepository;
  }

  @Override
  public String getPartnerBaseDomain(Company company, Invoice invoice, int invoiceTypeSelect) {
    long companyId = company.getPartner() == null ? 0 : company.getPartner().getId();
    String domain =
        String.format(
            "self.id != %d "
                + "AND self.isContact = false "
                + "AND :company member of self.companySet",
            companyId);

    if (invoiceTypeSelect == PriceListRepository.TYPE_SALE) {
      domain += " AND self.isCustomer = true ";
    } else {
      domain += " AND self.isSupplier = true ";
    }
    return domain;
  }

  @Override
  public String getCompanyTaxNumberDomain(Company company) {
    String companyTaxNumbersIds =
        CollectionUtils.isEmpty(company.getTaxNumberList())
            ? "0"
            : company.getTaxNumberList().stream()
                .map(TaxNumber::getId)
                .map(Objects::toString)
                .collect(Collectors.joining(","));

    return String.format("self.id IN (%s)", companyTaxNumbersIds);
  }

  @Override
  public String getFiscalPositionDomain(Invoice invoice) {
    TaxNumber companyTaxNumber = invoice.getCompanyTaxNumber();
    if (companyTaxNumber != null) {
      List<FiscalPosition> fiscalPositionList =
          fiscalPositionRepository
              .all()
              .filter("(:companyTaxNumber) MEMBER OF self.taxNumberSet")
              .bind("companyTaxNumber", companyTaxNumber)
              .fetch();
      String companyTaxNumbersIds =
          CollectionUtils.isEmpty(fiscalPositionList)
              ? "0"
              : fiscalPositionList.stream()
                  .map(FiscalPosition::getId)
                  .map(Objects::toString)
                  .collect(Collectors.joining(","));
      return String.format("self.id IN (%s)", companyTaxNumbersIds);
    }
    return null;
  }
}
