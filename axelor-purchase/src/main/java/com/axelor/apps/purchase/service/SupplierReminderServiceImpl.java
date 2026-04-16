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
package com.axelor.apps.purchase.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.message.TemplateMessageServiceBaseImpl;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.exception.PurchaseExceptionMessage;
import com.axelor.apps.purchase.service.config.PurchaseConfigService;
import com.axelor.i18n.I18n;
import com.axelor.message.db.Message;
import com.axelor.message.db.Template;
import com.axelor.message.service.MessageService;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import jakarta.mail.MessagingException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SupplierReminderServiceImpl implements SupplierReminderService {

  protected static final String CONTEXT_COMPANY = "Company";
  protected static final String CONTEXT_OVERDUE_LINES = "overdueLines";

  protected static final String LINE_KEY_LINE = "line";
  protected static final String LINE_KEY_REMAINING_QTY = "remainingQty";
  protected static final String LINE_KEY_DAYS_OVERDUE = "daysOverdue";

  protected final AppBaseService appBaseService;
  protected final PurchaseConfigService purchaseConfigService;
  protected final TemplateMessageServiceBaseImpl templateMessageService;
  protected final MessageService messageService;

  @Inject
  public SupplierReminderServiceImpl(
      AppBaseService appBaseService,
      PurchaseConfigService purchaseConfigService,
      TemplateMessageServiceBaseImpl templateMessageService,
      MessageService messageService) {
    this.appBaseService = appBaseService;
    this.purchaseConfigService = purchaseConfigService;
    this.templateMessageService = templateMessageService;
    this.messageService = messageService;
  }

  @Override
  public void sendReminders(List<PurchaseOrderLine> purchaseOrderLineList)
      throws AxelorException, MessagingException, ClassNotFoundException {
    if (CollectionUtils.isEmpty(purchaseOrderLineList)) {
      return;
    }
    for (Map.Entry<Partner, Map<Company, List<PurchaseOrderLine>>> supplierEntry :
        groupBySupplierAndCompany(purchaseOrderLineList).entrySet()) {
      for (Map.Entry<Company, List<PurchaseOrderLine>> companyEntry :
          supplierEntry.getValue().entrySet()) {
        sendReminder(supplierEntry.getKey(), companyEntry.getKey(), companyEntry.getValue(), null);
      }
    }
  }

  @Override
  public Map<Partner, Map<Company, List<PurchaseOrderLine>>> groupBySupplierAndCompany(
      List<PurchaseOrderLine> purchaseOrderLineList) {
    return purchaseOrderLineList.stream()
        .filter(line -> line.getPurchaseOrder().getSupplierPartner() != null)
        .filter(line -> line.getPurchaseOrder().getCompany() != null)
        .collect(
            Collectors.groupingBy(
                line -> line.getPurchaseOrder().getSupplierPartner(),
                LinkedHashMap::new,
                Collectors.groupingBy(
                    line -> line.getPurchaseOrder().getCompany(),
                    LinkedHashMap::new,
                    Collectors.toList())));
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void sendReminder(
      Partner supplier, Company company, List<PurchaseOrderLine> purchaseOrderLineList, Batch batch)
      throws AxelorException, MessagingException, ClassNotFoundException {
    if (supplier.getEmailAddress() == null) {
      String purchaseOrderSeqs =
          purchaseOrderLineList.stream()
              .map(line -> line.getPurchaseOrder().getPurchaseOrderSeq())
              .distinct()
              .collect(Collectors.joining(", "));
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(PurchaseExceptionMessage.PURCHASE_SUPPLIER_REMINDER_MISSING_EMAIL),
          supplier.getName(),
          purchaseOrderSeqs);
    }
    Template template = purchaseConfigService.getSupplierReminderTemplate(company);
    Message message =
        templateMessageService.generateMessage(
            supplier, template, getExtraTemplatesContext(company, purchaseOrderLineList));
    message.addToEmailAddressSetItem(supplier.getEmailAddress());
    purchaseOrderLineList.stream()
        .map(PurchaseOrderLine::getPurchaseOrder)
        .distinct()
        .forEach(
            purchaseOrder ->
                messageService.addMessageRelatedTo(
                    message, PurchaseOrder.class.getName(), purchaseOrder.getId()));
    if (batch != null) {
      messageService.addMessageRelatedTo(message, Batch.class.getName(), batch.getId());
    }
    messageService.sendByEmail(message);
  }

  protected Map<String, Object> getExtraTemplatesContext(
      Company company, List<PurchaseOrderLine> purchaseOrderLineList) {
    LocalDate todayDate = appBaseService.getTodayDate(company);
    Map<String, Object> extraTemplatesContext = new HashMap<>();
    extraTemplatesContext.put(CONTEXT_COMPANY, company);
    extraTemplatesContext.put(
        CONTEXT_OVERDUE_LINES,
        purchaseOrderLineList.stream()
            .map(line -> getLineContext(line, todayDate))
            .collect(Collectors.toList()));
    return extraTemplatesContext;
  }

  protected Map<String, Object> getLineContext(PurchaseOrderLine line, LocalDate todayDate) {
    Map<String, Object> lineContext = new HashMap<>();
    lineContext.put(LINE_KEY_LINE, line);
    lineContext.put(LINE_KEY_REMAINING_QTY, line.getQty().subtract(line.getReceivedQty()));
    lineContext.put(
        LINE_KEY_DAYS_OVERDUE, ChronoUnit.DAYS.between(line.getEstimatedReceiptDate(), todayDate));
    return lineContext;
  }

  @Override
  public List<PurchaseOrderLine> getOverdueLines(List<PurchaseOrderLine> purchaseOrderLineList) {
    if (CollectionUtils.isEmpty(purchaseOrderLineList)) {
      return List.of();
    }
    return purchaseOrderLineList.stream()
        .filter(getEligibilityPredicate())
        .collect(Collectors.toList());
  }

  protected Predicate<PurchaseOrderLine> getEligibilityPredicate() {
    return purchaseOrderLine -> {
      if (purchaseOrderLine.getIsTitleLine()) {
        return false;
      }
      PurchaseOrder purchaseOrder = purchaseOrderLine.getPurchaseOrder();
      int statusSelect = purchaseOrder.getStatusSelect();
      if (statusSelect < PurchaseOrderRepository.STATUS_VALIDATED
          || statusSelect == PurchaseOrderRepository.STATUS_CANCELED) {
        return false;
      }
      if (purchaseOrderLine.getEstimatedReceiptDate() == null) {
        return false;
      }
      LocalDate todayDate = appBaseService.getTodayDate(purchaseOrder.getCompany());
      return purchaseOrderLine.getEstimatedReceiptDate().isBefore(todayDate)
          && purchaseOrderLine.getReceivedQty().compareTo(purchaseOrderLine.getQty()) < 0;
    };
  }
}
