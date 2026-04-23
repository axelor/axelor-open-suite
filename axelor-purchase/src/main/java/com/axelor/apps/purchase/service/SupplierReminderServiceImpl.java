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
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.exception.PurchaseExceptionMessage;
import com.axelor.apps.purchase.service.config.PurchaseConfigService;
import com.axelor.i18n.I18n;
import com.axelor.message.db.Message;
import com.axelor.message.db.Template;
import com.axelor.message.service.MessageService;
import com.axelor.message.service.TemplateMessageService;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import jakarta.mail.MessagingException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SupplierReminderServiceImpl implements SupplierReminderService {

  protected final AppBaseService appBaseService;
  protected final PurchaseConfigService purchaseConfigService;
  protected final TemplateMessageService templateMessageService;
  protected final MessageService messageService;

  @Inject
  public SupplierReminderServiceImpl(
      AppBaseService appBaseService,
      PurchaseConfigService purchaseConfigService,
      TemplateMessageService templateMessageService,
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
    Map<Partner, List<PurchaseOrderLine>> linesBySupplier =
        purchaseOrderLineList.stream()
            .collect(
                Collectors.groupingBy(
                    purchaseOrderLine ->
                        purchaseOrderLine.getPurchaseOrder().getSupplierPartner()));

    for (Map.Entry<Partner, List<PurchaseOrderLine>> entry : linesBySupplier.entrySet()) {
      Partner supplier = entry.getKey();
      List<PurchaseOrderLine> supplierLines = entry.getValue();
      Company company = supplierLines.get(0).getPurchaseOrder().getCompany();
      sendReminderForSupplier(supplier, company, supplierLines);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void sendReminderForSupplier(
      Partner supplier, Company company, List<PurchaseOrderLine> purchaseOrderLineList)
      throws AxelorException, MessagingException, ClassNotFoundException {
    if (supplier == null || company == null) {
      return;
    }
    if (supplier.getEmailAddress() == null) {
      String purchaseOrderSeqs =
          purchaseOrderLineList.stream()
              .map(l -> l.getPurchaseOrder().getPurchaseOrderSeq())
              .distinct()
              .collect(Collectors.joining(", "));
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(PurchaseExceptionMessage.PURCHASE_SUPPLIER_REMINDER_MISSING_EMAIL),
          supplier.getName(),
          purchaseOrderSeqs);
    }
    Template template = purchaseConfigService.getSupplierReminderTemplate(company);
    Message message = templateMessageService.generateMessage(supplier, template);
    message.addToEmailAddressSetItem(supplier.getEmailAddress());
    messageService.sendByEmail(message);
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
      if (purchaseOrder.getStatusSelect() < PurchaseOrderRepository.STATUS_VALIDATED) {
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
