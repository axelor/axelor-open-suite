package com.axelor.apps.crm.service.batch;

import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.crm.db.CrmBatch;
import com.axelor.apps.crm.db.repo.CallForTendersRepository;
import com.axelor.apps.crm.exception.IExceptionMessage;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.service.MessageService;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.Query;

public class BatchCallForTendersReminder extends AbstractBatch {

  protected UserRepository userRepo;
  protected CallForTendersRepository callForTendersRepo;
  protected TemplateMessageService templateMessageService;
  protected MessageService messageService;

  @Inject
  public BatchCallForTendersReminder(
      UserRepository userRepository,
      CallForTendersRepository callForTendersRepository,
      TemplateMessageService templateMessageService,
      MessageService messageService) {
    this.userRepo = userRepository;
    this.callForTendersRepo = callForTendersRepository;
    this.templateMessageService = templateMessageService;
    this.messageService = messageService;
  }

  @Override
  protected void process() {

    CrmBatch crmBatch = batch.getCrmBatch();
    Map<Long, List<Long>> callForTendersByUserMap = new HashMap<>();

    String queryStr =
        "SELECT CFT.salesman.id as usr, CFT.id as cft"
            + " FROM CallForTenders CFT"
            + " WHERE CFT.endDate >= ?"
            + " AND CFT.endDate <= ?"
            + " AND (CFT.isAppointementScheduled is null or CFT.isAppointementScheduled is false)"
            + " AND (CFT.isAppointementMade is null or CFT.isAppointementMade is false)"
            + " ORDER BY usr";

    Query query = JPA.em().createQuery(queryStr);
    LocalDate today = LocalDate.now();
    query.setParameter(0, today);
    query.setParameter(1, today.plusMonths(crmBatch.getMonthLimitNbr()));
    List<Long[]> resultTable = query.getResultList();

    List<Long> callForTendersIdList;
    List<Long> orphanCFTList = new ArrayList<>();
    for (Object[] ids : resultTable) {

      if (ids[0] == null) {
        orphanCFTList.add(Long.valueOf(ids[1].toString()));
        continue;
      }

      Long userId = Long.valueOf(ids[0].toString());
      Long callForTendersId = Long.valueOf(ids[1].toString());

      if (!callForTendersByUserMap.containsKey(userId)) {
        callForTendersByUserMap.put(userId, new ArrayList<>());
      }

      callForTendersIdList = callForTendersByUserMap.get(userId);
      callForTendersIdList.add(callForTendersId);
    }

    if (callForTendersByUserMap.isEmpty()) {
      return;
    }

    Template template = crmBatch.getTemplate();

    for (Long usrId : callForTendersByUserMap.keySet()) {
      try {
        this.sendAlert(usrId, callForTendersByUserMap.get(usrId), template);
        incrementDone();
      } catch (AxelorException axelorE) {
        TraceBackService.trace(axelorE, batch.getClass().toString(), batch.getId());
        incrementAnomaly();
      } catch (Exception e) {
        TraceBackService.trace(e, batch.getClass().toString(), batch.getId());
        incrementAnomaly();
      }
    }

    if (!orphanCFTList.isEmpty()) {
      AxelorException orphanCFTException =
          new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(IExceptionMessage.CRM_BATCH_OPRPHAN_CFT_FOUND),
              orphanCFTList.size(),
              "\n" + this.get_callForTendersList(orphanCFTList, "\n"));
      TraceBackService.trace(orphanCFTException, batch.getClass().toString(), batch.getId());
      incrementAnomaly();
    }
  }

  protected void sendAlert(Long userId, List<Long> callForTendersIdList, Template template)
      throws Exception {

    User user = userRepo.find(userId);
    Set<EmailAddress> emailAddressSet = new HashSet<>();

    String recipientEmail = user.getEmail();
    if (Strings.isNullOrEmpty(recipientEmail)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.CRM_BATCH_RECIPIENT_EMAIL_NOT_CONFIGURED),
          user.getName());
    }

    emailAddressSet.add(new EmailAddress(recipientEmail));

    String callForTendersListStr = this.get_callForTendersList(callForTendersIdList, "<br/>");

    Message message = templateMessageService.generateMessage(AuthUtils.getUser(), template);
    message.setContent(
        message.getContent().replaceAll("_callForTendersList", callForTendersListStr));
    message.setToEmailAddressSet(emailAddressSet);
    if (batch.getCrmBatch().getSenderEmail() != null) {
      message.setFromEmailAddress(new EmailAddress(batch.getCrmBatch().getSenderEmail()));
    } else {
      message.setFromEmailAddress(new EmailAddress(AuthUtils.getUser().getEmail()));
    }

    messageService.sendByEmail(message);
  }

  protected String get_callForTendersList(List<Long> callForTendersIdList, String separator) {
    StringBuilder callForTendersStrBuilder = new StringBuilder();
    for (Long callForTendersId : callForTendersIdList) {
      callForTendersStrBuilder.append(
          callForTendersRepo.find(callForTendersId).getName() + separator);
    }
    return callForTendersStrBuilder.toString();
  }
}
