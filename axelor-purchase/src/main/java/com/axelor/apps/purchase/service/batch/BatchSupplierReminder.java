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
package com.axelor.apps.purchase.service.batch;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.ExceptionOriginRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.purchase.db.PurchaseBatch;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.exception.PurchaseExceptionMessage;
import com.axelor.apps.purchase.service.SupplierReminderService;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import jakarta.inject.Inject;
import jakarta.persistence.TypedQuery;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BatchSupplierReminder extends AbstractBatch {

  protected final SupplierReminderService supplierReminderService;
  protected final PurchaseOrderLineRepository purchaseOrderLineRepository;
  protected final CompanyRepository companyRepository;

  @Inject
  public BatchSupplierReminder(
      SupplierReminderService supplierReminderService,
      PurchaseOrderLineRepository purchaseOrderLineRepository,
      CompanyRepository companyRepository) {
    super();
    this.supplierReminderService = supplierReminderService;
    this.purchaseOrderLineRepository = purchaseOrderLineRepository;
    this.companyRepository = companyRepository;
  }

  @Override
  protected void setBatchTypeSelect() {
    this.batch.setBatchTypeSelect(BatchRepository.BATCH_TYPE_PURCHASE_BATCH);
  }

  @Override
  protected void process() throws SQLException {
    PurchaseBatch purchaseBatch = (PurchaseBatch) model;
    Company company = purchaseBatch.getCompany();
    LocalDate todayDate = appBaseService.getTodayDate(company);

    int offset = 0;
    List<Partner> supplierList;
    while (!(supplierList = fetchOverdueSuppliers(company, todayDate, offset)).isEmpty()) {
      for (Partner supplier : supplierList) {
        ++offset;
        try {
          List<PurchaseOrderLine> overduePurchaseOrderLineList =
              fetchOverdueLines(supplier, company, todayDate);
          supplierReminderService.sendReminder(
              supplier, company, overduePurchaseOrderLineList, batch);
          incrementDone();
        } catch (Exception e) {
          TraceBackService.trace(e, ExceptionOriginRepository.SUPPLIER_REMINDER, batch.getId());
          incrementAnomaly();
        }
      }
      JPA.clear();
      findBatch();
      company = companyRepository.find(company.getId());
    }
  }

  protected List<Partner> fetchOverdueSuppliers(Company company, LocalDate todayDate, int offset) {
    TypedQuery<Partner> query =
        JPA.em()
            .createQuery(
                "SELECT DISTINCT supplier FROM PurchaseOrderLine self"
                    + " JOIN self.purchaseOrder.supplierPartner supplier"
                    + " WHERE "
                    + getOverdueLineFilter()
                    + " ORDER BY supplier.id",
                Partner.class);
    getOverdueLineFilterBindings(todayDate, company).forEach(query::setParameter);
    return query.setFirstResult(offset).setMaxResults(getFetchLimit()).getResultList();
  }

  protected List<PurchaseOrderLine> fetchOverdueLines(
      Partner supplier, Company company, LocalDate todayDate) {
    return purchaseOrderLineRepository
        .all()
        .filter("self.purchaseOrder.supplierPartner = :supplier AND " + getOverdueLineFilter())
        .bind("supplier", supplier)
        .bind(getOverdueLineFilterBindings(todayDate, company))
        .order("purchaseOrder.purchaseOrderSeq")
        .order("sequence")
        .order("id")
        .fetch();
  }

  protected String getOverdueLineFilter() {
    return "self.isTitleLine = false"
        + " AND self.purchaseOrder.statusSelect = :status"
        + " AND self.estimatedReceiptDate < :today"
        + " AND self.receivedQty < self.qty"
        + " AND self.purchaseOrder.supplierPartner.emailAddress IS NOT NULL"
        + " AND self.purchaseOrder.company = :company";
  }

  protected Map<String, Object> getOverdueLineFilterBindings(LocalDate todayDate, Company company) {
    Map<String, Object> bindings = new HashMap<>();
    bindings.put("status", PurchaseOrderRepository.STATUS_VALIDATED);
    bindings.put("today", todayDate);
    bindings.put("company", company);
    return bindings;
  }

  @Override
  protected Integer getFetchLimit() {
    return Optional.ofNullable(batch)
        .map(Batch::getPurchaseBatch)
        .map(PurchaseBatch::getFetchLimit)
        .filter(limit -> limit > 0)
        .orElseGet(super::getFetchLimit);
  }

  @Override
  protected void stop() {
    super.stop();
    addComment(
        String.format(
            I18n.get(PurchaseExceptionMessage.PURCHASE_SUPPLIER_REMINDER_BATCH_REPORT),
            batch.getDone(),
            batch.getAnomaly()));
  }
}
