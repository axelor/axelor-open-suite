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
package com.axelor.apps.supplychain.service.supplierdispute;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.purchase.db.PurchaseConfig;
import com.axelor.apps.purchase.service.config.PurchaseConfigService;
import com.axelor.apps.supplychain.db.SupplierDispute;
import com.axelor.apps.supplychain.db.repo.SupplierDisputeRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.message.db.EmailAddress;
import com.axelor.message.db.Message;
import com.axelor.message.db.Template;
import com.axelor.message.db.repo.MessageRepository;
import com.axelor.message.service.TemplateMessageService;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;

public class SupplierDisputeMessageServiceImpl implements SupplierDisputeMessageService {

  protected PurchaseConfigService purchaseConfigService;
  protected TemplateMessageService templateMessageService;
  protected MessageRepository messageRepository;

  @Inject
  public SupplierDisputeMessageServiceImpl(
      PurchaseConfigService purchaseConfigService,
      TemplateMessageService templateMessageService,
      MessageRepository messageRepository) {
    this.purchaseConfigService = purchaseConfigService;
    this.templateMessageService = templateMessageService;
    this.messageRepository = messageRepository;
  }

  @Override
  public Optional<Template> getTemplate(SupplierDispute supplierDispute) throws AxelorException {
    PurchaseConfig purchaseConfig =
        purchaseConfigService.getPurchaseConfig(supplierDispute.getCompany());
    Integer statusSelect = supplierDispute.getStatusSelect();

    if (statusSelect == SupplierDisputeRepository.STATUS_OPEN) {
      return Optional.ofNullable(purchaseConfig.getSupplierDisputeOpeningMessageTemplate());
    }
    if (statusSelect == SupplierDisputeRepository.STATUS_AWAITING_SUPPLIER_RESPONSE) {
      return Optional.ofNullable(
          purchaseConfig.getSupplierDisputeAwaitingResponseMessageTemplate());
    }
    return Optional.empty();
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Message generateMessage(SupplierDispute supplierDispute) throws AxelorException {
    Template template =
        getTemplate(supplierDispute)
            .orElseThrow(
                () ->
                    new AxelorException(
                        supplierDispute,
                        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                        I18n.get(
                            SupplychainExceptionMessage
                                .SUPPLYCHAIN_SUPPLIER_DISPUTE_NO_MESSAGE_TEMPLATE),
                        supplierDispute.getDisputeSeq()));

    if (template.getMetaModel() == null
        || !SupplierDispute.class.getName().equals(template.getMetaModel().getFullName())) {
      throw new AxelorException(
          supplierDispute,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(SupplychainExceptionMessage.SUPPLYCHAIN_SUPPLIER_DISPUTE_WRONG_TEMPLATE_MODEL),
          template.getName());
    }

    Message message;
    try {
      message = templateMessageService.generateMessage(supplierDispute, template);
    } catch (ClassNotFoundException e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_INCONSISTENCY, e.getMessage());
    }

    if (CollectionUtils.isEmpty(message.getToEmailAddressSet())) {
      getDefaultRecipient(supplierDispute).ifPresent(message::addToEmailAddressSetItem);
    }

    return messageRepository.save(message);
  }

  protected Optional<EmailAddress> getDefaultRecipient(SupplierDispute supplierDispute) {
    return Optional.ofNullable(supplierDispute.getContactPartner())
        .map(Partner::getEmailAddress)
        .or(
            () ->
                Optional.ofNullable(supplierDispute.getSupplierPartner())
                    .map(Partner::getEmailAddress));
  }
}
