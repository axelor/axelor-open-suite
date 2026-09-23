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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.purchase.db.PurchaseConfig;
import com.axelor.apps.purchase.service.config.PurchaseConfigService;
import com.axelor.apps.supplychain.db.SupplierDispute;
import com.axelor.apps.supplychain.db.repo.SupplierDisputeRepository;
import com.axelor.message.db.EmailAddress;
import com.axelor.message.db.Message;
import com.axelor.message.db.Template;
import com.axelor.message.db.repo.MessageRepository;
import com.axelor.message.service.TemplateMessageService;
import com.axelor.meta.db.MetaModel;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TestSupplierDisputeMessageService {

  protected SupplierDisputeMessageService supplierDisputeMessageService;
  protected PurchaseConfig purchaseConfig;
  protected Template openingTemplate;
  protected Template awaitingTemplate;
  protected Message generatedMessage;

  @BeforeEach
  void setUp() throws Exception {
    openingTemplate = createTemplate("Opening", SupplierDispute.class.getName());
    awaitingTemplate = createTemplate("Awaiting", SupplierDispute.class.getName());
    purchaseConfig = new PurchaseConfig();
    purchaseConfig.setSupplierDisputeOpeningMessageTemplate(openingTemplate);
    purchaseConfig.setSupplierDisputeAwaitingResponseMessageTemplate(awaitingTemplate);

    PurchaseConfigService purchaseConfigService = mock(PurchaseConfigService.class);
    when(purchaseConfigService.getPurchaseConfig(any())).thenReturn(purchaseConfig);

    generatedMessage = new Message();
    TemplateMessageService templateMessageService = mock(TemplateMessageService.class);
    when(templateMessageService.generateMessage(any(SupplierDispute.class), any(Template.class)))
        .thenReturn(generatedMessage);

    MessageRepository messageRepository = mock(MessageRepository.class);
    when(messageRepository.save(any(Message.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    supplierDisputeMessageService =
        new SupplierDisputeMessageServiceImpl(
            purchaseConfigService, templateMessageService, messageRepository);
  }

  @Test
  void testGetTemplateOpenReturnsOpeningTemplate() throws AxelorException {
    Optional<Template> template =
        supplierDisputeMessageService.getTemplate(
            createSupplierDispute(SupplierDisputeRepository.STATUS_OPEN));

    assertSame(openingTemplate, template.orElse(null));
  }

  @Test
  void testGetTemplateAwaitingReturnsAwaitingTemplate() throws AxelorException {
    Optional<Template> template =
        supplierDisputeMessageService.getTemplate(
            createSupplierDispute(SupplierDisputeRepository.STATUS_AWAITING_SUPPLIER_RESPONSE));

    assertSame(awaitingTemplate, template.orElse(null));
  }

  @ParameterizedTest
  @ValueSource(
      ints = {
        SupplierDisputeRepository.STATUS_UNDER_INVESTIGATION,
        SupplierDisputeRepository.STATUS_RESOLVED,
        SupplierDisputeRepository.STATUS_CLOSED,
        SupplierDisputeRepository.STATUS_CANCELLED
      })
  void testGetTemplateOtherStatusesReturnEmpty(int statusSelect) throws AxelorException {
    Optional<Template> template =
        supplierDisputeMessageService.getTemplate(createSupplierDispute(statusSelect));

    assertTrue(template.isEmpty());
  }

  @Test
  void testGenerateMessageWithoutTemplateThrowsConfigurationError() {
    purchaseConfig.setSupplierDisputeOpeningMessageTemplate(null);

    AxelorException exception =
        assertThrows(
            AxelorException.class,
            () ->
                supplierDisputeMessageService.generateMessage(
                    createSupplierDispute(SupplierDisputeRepository.STATUS_OPEN)));

    assertEquals(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, exception.getCategory());
  }

  @Test
  void testGenerateMessageWithTemplateOnWrongModelThrowsConfigurationError() {
    purchaseConfig.setSupplierDisputeOpeningMessageTemplate(
        createTemplate("Wrong", Partner.class.getName()));

    AxelorException exception =
        assertThrows(
            AxelorException.class,
            () ->
                supplierDisputeMessageService.generateMessage(
                    createSupplierDispute(SupplierDisputeRepository.STATUS_OPEN)));

    assertEquals(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, exception.getCategory());
  }

  @Test
  void testGenerateMessageUsesContactEmailWhenTemplateGivesNoRecipient() throws AxelorException {
    SupplierDispute supplierDispute = createSupplierDispute(SupplierDisputeRepository.STATUS_OPEN);
    EmailAddress contactEmail = new EmailAddress("contact@supplier.com");
    supplierDispute.setContactPartner(createPartner(contactEmail));

    Message message = supplierDisputeMessageService.generateMessage(supplierDispute);

    assertEquals(1, message.getToEmailAddressSet().size());
    assertTrue(message.getToEmailAddressSet().contains(contactEmail));
  }

  @Test
  void testGenerateMessageFallsBackToSupplierEmailWithoutContact() throws AxelorException {
    SupplierDispute supplierDispute = createSupplierDispute(SupplierDisputeRepository.STATUS_OPEN);
    EmailAddress supplierEmail = new EmailAddress("sales@supplier.com");
    supplierDispute.setSupplierPartner(createPartner(supplierEmail));

    Message message = supplierDisputeMessageService.generateMessage(supplierDispute);

    assertEquals(1, message.getToEmailAddressSet().size());
    assertTrue(message.getToEmailAddressSet().contains(supplierEmail));
  }

  @Test
  void testGenerateMessageKeepsTemplateRecipients() throws AxelorException {
    SupplierDispute supplierDispute = createSupplierDispute(SupplierDisputeRepository.STATUS_OPEN);
    supplierDispute.setContactPartner(createPartner(new EmailAddress("contact@supplier.com")));
    EmailAddress templateEmail = new EmailAddress("template@supplier.com");
    generatedMessage.addToEmailAddressSetItem(templateEmail);

    Message message = supplierDisputeMessageService.generateMessage(supplierDispute);

    assertEquals(1, message.getToEmailAddressSet().size());
    assertTrue(message.getToEmailAddressSet().contains(templateEmail));
  }

  protected SupplierDispute createSupplierDispute(int statusSelect) {
    SupplierDispute supplierDispute = new SupplierDispute();
    supplierDispute.setDisputeSeq("SD260001");
    supplierDispute.setStatusSelect(statusSelect);
    supplierDispute.setCompany(new Company());
    supplierDispute.setSupplierPartner(new Partner());
    return supplierDispute;
  }

  protected Partner createPartner(EmailAddress emailAddress) {
    Partner partner = new Partner();
    partner.setEmailAddress(emailAddress);
    return partner;
  }

  protected Template createTemplate(String name, String modelFullName) {
    MetaModel metaModel = new MetaModel();
    metaModel.setFullName(modelFullName);
    Template template = new Template();
    template.setName(name);
    template.setMetaModel(metaModel);
    return template;
  }
}
