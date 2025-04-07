package com.axelor.apps.purchase.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.purchase.db.CallTender;
import com.axelor.apps.purchase.db.CallTenderMail;
import com.axelor.apps.purchase.db.CallTenderOffer;
import com.axelor.apps.purchase.db.repo.CallTenderOfferRepository;
import com.axelor.apps.purchase.exception.PurchaseExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.Template;
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
import java.util.stream.Collectors;

public class CallTenderGenerateServiceImpl implements CallTenderGenerateService {

  protected final CallTenderOfferService callTenderOfferService;
  protected final TemplateMessageService templateMessageService;

  @Inject
  public CallTenderGenerateServiceImpl(
      CallTenderOfferService callTenderOfferService,
      TemplateMessageService templateMessageService) {
    this.callTenderOfferService = callTenderOfferService;
    this.templateMessageService = templateMessageService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void generateCallTenderOffers(CallTender callTender) {
    Objects.requireNonNull(callTender);

    if (callTender.getCallTenderSupplierList() == null) {
      return;
    }

    callTender.getCallTenderSupplierList().stream()
        .map(
            supplier ->
                callTenderOfferService.generateCallTenderOfferList(
                    supplier, callTender.getCallTenderNeedList()))
        .flatMap(List::stream)
        .forEach(
            resultOffer -> {
              if (!callTenderOfferService.alreadyGenerated(
                  resultOffer, callTender.getCallTenderOfferList())) {
                callTender.addCallTenderOfferListItem(resultOffer);
              }
            });
  }

  @Override
  public void sendCallTenderOffers(CallTender callTender) throws AxelorException, IOException {
    Objects.requireNonNull(callTender);

    if (callTender.getCallTenderOfferList() == null) {
      return;
    }

    // Get template
    var template = callTender.getCallForTenderMailTemplate();

    // Generate callTenderMail
    var offerToGenerateMailList =
        callTender.getCallTenderOfferList().stream()
            .filter(offer -> offer.getOfferMail() == null)
            .collect(Collectors.toList());

    for (CallTenderOffer offer : offerToGenerateMailList) {
      generateOfferMail(offer, template);
    }

    sendMails(callTender);
  }

  protected void sendMails(CallTender callTender) {
    // Send CallTenderMail
    callTender.getCallTenderOfferList().stream()
        .map(CallTenderOffer::getOfferMail)
        .forEach(
            offer -> {
              try {
                var sentMessage =
                    templateMessageService.generateAndSendMessage(offer, offer.getMailTemplate());
                offer.setSentMessage(sentMessage);
              } catch (IOException e) {
                throw new RuntimeException(e);
              } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
              }
            });
    callTender.getCallTenderOfferList().stream()
        .filter(offer -> offer.getOfferMail().getSentMessage() != null)
        .forEach(offer -> offer.setStatusSelect(CallTenderOfferRepository.STATUS_SENT));
  }

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
    callTenderMail.addMetaFileSetItem(csv);
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
