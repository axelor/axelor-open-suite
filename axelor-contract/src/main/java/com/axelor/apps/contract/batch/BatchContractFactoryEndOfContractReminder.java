package com.axelor.apps.contract.batch;

import com.axelor.apps.base.db.AppContract;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.app.AppService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractBatch;
import com.axelor.apps.contract.db.repo.ContractBatchRepository;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.exception.IExceptionMessage;
import com.axelor.apps.contract.service.ContractService;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.service.MessageService;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.io.IOException;
import java.time.LocalDate;
import javax.mail.MessagingException;

public class BatchContractFactoryEndOfContractReminder extends BatchContractFactory {

  protected TemplateMessageService templateMessageService;
  protected MessageService messageService;
  protected AppService appService;

  @Inject
  public BatchContractFactoryEndOfContractReminder(
      ContractRepository repository,
      ContractService service,
      AppBaseService baseService,
      TemplateMessageService templateMessageService,
      MessageService messageService,
      AppService appService) {
    super(repository, service, baseService);
    this.templateMessageService = templateMessageService;
    this.messageService = messageService;
    this.appService = appService;
  }

  @Override
  Query<Contract> prepare(Batch batch) {
    ContractBatch contractBatch = batch.getContractBatch();
    LocalDate todayDate = Beans.get(AppBaseService.class).getTodayDate();
    LocalDate endDate;
    if (contractBatch.getDurationTypeSelect()
        == ContractBatchRepository.CONTRACT_DURATION_TYPE_MONTHS) {
      endDate = todayDate.plusMonths(contractBatch.getDuration());
    } else if (contractBatch.getDurationTypeSelect()
        == ContractBatchRepository.CONTRACT_DURATION_TYPE_WEEKS) {
      endDate = todayDate.plusWeeks(contractBatch.getDuration());
    } else {
      endDate = todayDate.plusDays(contractBatch.getDuration());
    }
    return repository
        .all()
        .filter(
            "self.currentContractVersion.supposedEndDate BETWEEN :todayDate AND :endDate "
                + "AND self.statusSelect = :status "
                + "AND self.targetTypeSelect = :contractType "
                + "AND :batch NOT MEMBER of self.batchSet")
        .bind("todayDate", todayDate)
        .bind("endDate", endDate)
        .bind("contractType", contractBatch.getContractTypeSelect())
        .bind("status", ContractRepository.ACTIVE_CONTRACT)
        .bind("batch", batch);
  }

  @Override
  void process(Contract contract)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, IOException, MessagingException {
    sendEmail(contract);
  }

  public void sendEmail(Contract contract)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, IOException, MessagingException {
    AppContract contractApp = (AppContract) appService.getApp("contract");
    Template template = contractApp.getContractEndReminderTemplate();
    if (template != null) {
      String model = template.getMetaModel().getFullName();
      String tag = template.getMetaModel().getName();
      Message message =
          templateMessageService.generateMessage(contract.getId(), model, tag, template);
      messageService.sendByEmail(message);
    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.CONTRACT_END_REMINDER_TEMPLATE_NOT_DEFINED));
    }
  }
}
