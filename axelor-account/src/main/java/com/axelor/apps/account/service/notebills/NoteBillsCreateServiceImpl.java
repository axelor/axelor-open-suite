package com.axelor.apps.account.service.notebills;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.NoteBills;
import com.axelor.apps.account.db.repo.NoteBillsRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Objects;

public class NoteBillsCreateServiceImpl implements NoteBillsCreateService {

  protected NoteBillsRepository noteBillsRepository;
  protected SequenceService sequenceService;

  @Inject
  public NoteBillsCreateServiceImpl(
      NoteBillsRepository noteBillsRepository, SequenceService sequenceService) {
    this.noteBillsRepository = noteBillsRepository;
    this.sequenceService = sequenceService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public NoteBills createNoteBills(Company company, Partner partner, Batch batch)
      throws AxelorException {
    Objects.requireNonNull(company);
    Objects.requireNonNull(partner);
    Objects.requireNonNull(batch);

    NoteBills noteBills = new NoteBills();

    noteBills.setCompany(company);
    noteBills.setPartner(partner);
    noteBills.setBatch(batch);
    noteBills.setEmailAddress(partner.getEmailAddress());
    AccountingBatch accountingBatch = batch.getAccountingBatch();
    if (accountingBatch != null) {
      noteBills.setDueDate(accountingBatch.getDueDate());
      noteBills.setBillOfExchangeTypeSelect(accountingBatch.getBillOfExchangeTypeSelect());
    }
    noteBills.setNoteBillsSeq(generateSequence(noteBills));

    return noteBillsRepository.save(noteBills);
  }

  public String generateSequence(NoteBills noteBills) throws AxelorException {

    if (!sequenceService.hasSequence(SequenceRepository.NOTE_BILLS, noteBills.getCompany())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.NOTE_BILLS_CONFIG_SEQUENCE),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          noteBills.getCompany().getName());
    }
    String seq =
        sequenceService.getSequenceNumber(SequenceRepository.NOTE_BILLS, noteBills.getCompany());
    return seq;
  }
}
