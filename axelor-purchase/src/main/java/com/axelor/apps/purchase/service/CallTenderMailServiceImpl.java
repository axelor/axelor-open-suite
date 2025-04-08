package com.axelor.apps.purchase.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.purchase.db.CallTender;
import com.axelor.apps.purchase.db.CallTenderMail;
import com.axelor.apps.purchase.db.CallTenderOffer;
import com.axelor.apps.purchase.db.CallTenderSupplier;
import com.axelor.apps.purchase.db.repo.CallTenderOfferRepository;
import com.axelor.apps.purchase.exception.PurchaseExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.Template;
import com.axelor.message.service.MessageService;
import com.axelor.message.service.TemplateMessageService;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.utils.helpers.StringHelper;
import com.axelor.utils.helpers.file.CsvHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.mail.MessagingException;

public class CallTenderMailServiceImpl implements CallTenderMailService {

  protected final MessageService messageService;
  protected final TemplateMessageService templateMessageService;

  @Inject
  public CallTenderMailServiceImpl(
      MessageService messageService, TemplateMessageService templateMessageService) {
    this.messageService = messageService;
    this.templateMessageService = templateMessageService;
  }

  @Override
  public void sendMails(CallTender callTender) throws ClassNotFoundException, MessagingException {

    for (CallTenderOffer offer : callTender.getCallTenderOfferList()) {
      if (offer.getStatusSelect().equals(CallTenderOfferRepository.STATUS_DRAFT)) {
        CallTenderMail offerMail = offer.getOfferMail();
        var messageToSend =
            templateMessageService.generateMessage(offer, offerMail.getMailTemplate());
        // Add all contacts to email address
        var contacts =
            getContactPartnerList(
                offer.getSupplierPartner(), callTender.getCallTenderSupplierList());
        contacts.stream()
            .map(Partner::getEmailAddress)
            .filter(Objects::nonNull)
            .forEach(messageToSend::addToEmailAddressSetItem);
        messageService.attachMetaFiles(messageToSend, Set.of(offerMail.getMetaFile()));
        var sentMessage = messageService.sendByEmail(messageToSend);
        offerMail.setSentMessage(sentMessage);
      }
    }
  }

  protected List<Partner> getContactPartnerList(
      Partner partner, List<CallTenderSupplier> suppliers) {
    if (suppliers != null) {
      return suppliers.stream()
          .filter(supplier -> supplier.getSupplierPartner().equals(partner))
          .limit(1)
          .map(CallTenderSupplier::getContactPartnerSet)
          .flatMap(Set::stream)
          .collect(Collectors.toList());
    }
    return List.of();
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void generateOfferMail(CallTenderOffer offer, Template template)
      throws AxelorException, IOException {
    Objects.requireNonNull(offer);

    if (template == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(PurchaseExceptionMessage.CALL_FOR_TENDER_MISSING_TEMPLATE));
    }

    var callTenderMail = new CallTenderMail();
    callTenderMail.setMailTemplate(template);

    // Generate csv here
    var csv = generateCsvFile(offer);
    callTenderMail.setMetaFile(csv);
    offer.setOfferMail(callTenderMail);
  }

  protected MetaFile generateCsvFile(CallTenderOffer offer) throws IOException {

    List<String[]> list = getValues(offer);

    var fileName =
        StringHelper.cutTooLongString(
            String.format(
                "CFT%s-%s",
                offer.getSupplierPartner().getSimpleFullName(), offer.getProduct().getName()));
    File file = MetaFiles.createTempFile(fileName, ".csv").toFile();
    fileName += ".csv";

    CsvHelper.csvWriter(file.getParent(), file.getName(), ';', getOfferCsvHeaders(), list);

    var inStream = new FileInputStream(file);
    return Beans.get(MetaFiles.class).upload(inStream, fileName);
  }

  protected List<String[]> getValues(CallTenderOffer offer) {
    List<String[]> list = new ArrayList<>();

    String[] lineValue =
        new String[] {
          offer.getProduct().getCode(),
          offer.getProduct().getName(),
          offer.getRequestedQty().toString(),
          offer.getRequestedDate().toString(),
          "",
          "",
          ""
        };

    list.add(lineValue);
    return list;
  }

  protected String[] getOfferCsvHeaders() {

    String[] headers = new String[7];
    headers[0] = I18n.get("Product code");
    headers[1] = I18n.get("Product name");
    headers[2] = I18n.get("Requested quantity");
    headers[3] = I18n.get("Requested date");
    headers[4] = I18n.get("Offered quantity");
    headers[5] = I18n.get("Offered date");
    headers[6] = I18n.get("Offered price");
    return headers;
  }
}
