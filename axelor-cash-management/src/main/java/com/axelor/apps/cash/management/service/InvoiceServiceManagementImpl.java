package com.axelor.apps.cash.management.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.PaymentConditionRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.factory.CancelFactory;
import com.axelor.apps.account.service.invoice.factory.ValidateFactory;
import com.axelor.apps.account.service.invoice.factory.VentilateFactory;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.alarm.AlarmEngineService;
import com.axelor.apps.businessproject.service.InvoiceServiceProjectImpl;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;

public class InvoiceServiceManagementImpl extends InvoiceServiceProjectImpl {

  @Inject
  public InvoiceServiceManagementImpl(
      ValidateFactory validateFactory,
      VentilateFactory ventilateFactory,
      CancelFactory cancelFactory,
      AlarmEngineService<Invoice> alarmEngineService,
      InvoiceRepository invoiceRepo,
      AppAccountService appAccountService,
      PartnerService partnerService,
      InvoiceLineService invoiceLineService,
      AccountConfigService accountConfigService,
      MoveToolService moveToolService) {
    super(
        validateFactory,
        ventilateFactory,
        cancelFactory,
        alarmEngineService,
        invoiceRepo,
        appAccountService,
        partnerService,
        invoiceLineService,
        accountConfigService,
        moveToolService);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void ventilate(Invoice invoice) throws AxelorException {
    super.ventilate(invoice);
    if (invoice.getEstimatedPaymentDate() == null) {
      this.computeEstimatedPaymentDate(invoice);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  public void computeEstimatedPaymentDate(Invoice invoice) {
    LocalDate estimatedPaymentDate = invoice.getDueDate();
    if (invoice.getPartner() != null && invoice.getPartner().getPaymentDelay() != null) {
      estimatedPaymentDate =
          estimatedPaymentDate.plusDays(invoice.getPartner().getPaymentDelay().intValue());
    }
    if (invoice.getPaymentCondition() != null) {
      if (invoice.getPaymentCondition().getTypeSelect() == PaymentConditionRepository.TYPE_NET) {
        if (invoice.getPaymentCondition().getPeriodTypeSelect()
            == PaymentConditionRepository.PERIOD_TYPE_DAYS) {
          estimatedPaymentDate =
              estimatedPaymentDate.plusDays(invoice.getPaymentCondition().getPaymentTime());
        } else if (invoice.getPaymentCondition().getPeriodTypeSelect()
            == PaymentConditionRepository.PERIOD_TYPE_MONTH) {
          estimatedPaymentDate =
              estimatedPaymentDate.plusMonths(invoice.getPaymentCondition().getPaymentTime());
        }
      } else if (invoice.getPaymentCondition().getTypeSelect()
          == PaymentConditionRepository.TYPE_END_OF_MONTH_N_DAYS) {
        estimatedPaymentDate =
            estimatedPaymentDate.withDayOfMonth(estimatedPaymentDate.lengthOfMonth());
        if (invoice.getPaymentCondition().getPeriodTypeSelect()
            == PaymentConditionRepository.PERIOD_TYPE_DAYS) {
          estimatedPaymentDate =
              estimatedPaymentDate.plusDays(invoice.getPaymentCondition().getPaymentTime());
        } else if (invoice.getPaymentCondition().getPeriodTypeSelect()
            == PaymentConditionRepository.PERIOD_TYPE_MONTH) {
          estimatedPaymentDate =
              estimatedPaymentDate.plusMonths(invoice.getPaymentCondition().getPaymentTime());
        }
      } else if (invoice.getPaymentCondition().getTypeSelect()
          == PaymentConditionRepository.TYPE_N_DAYS_END_OF_MONTH) {
        if (invoice.getPaymentCondition().getPeriodTypeSelect()
            == PaymentConditionRepository.PERIOD_TYPE_DAYS) {
          estimatedPaymentDate =
              estimatedPaymentDate.plusDays(invoice.getPaymentCondition().getPaymentTime());
        } else if (invoice.getPaymentCondition().getPeriodTypeSelect()
            == PaymentConditionRepository.PERIOD_TYPE_MONTH) {
          estimatedPaymentDate =
              estimatedPaymentDate.plusMonths(invoice.getPaymentCondition().getPaymentTime());
        }
        estimatedPaymentDate =
            estimatedPaymentDate.withDayOfMonth(estimatedPaymentDate.lengthOfMonth());
      } else if (invoice.getPaymentCondition().getTypeSelect()
          == PaymentConditionRepository.TYPE_N_DAYS_END_OF_MONTH_AT) {
        if (invoice.getPaymentCondition().getPeriodTypeSelect()
            == PaymentConditionRepository.PERIOD_TYPE_DAYS) {
          estimatedPaymentDate =
              estimatedPaymentDate.plusDays(invoice.getPaymentCondition().getPaymentTime());
        } else if (invoice.getPaymentCondition().getPeriodTypeSelect()
            == PaymentConditionRepository.PERIOD_TYPE_MONTH) {
          estimatedPaymentDate =
              estimatedPaymentDate.plusMonths(invoice.getPaymentCondition().getPaymentTime());
        }
        estimatedPaymentDate =
            estimatedPaymentDate.withDayOfMonth(
                invoice.getPaymentCondition().getDaySelect() == 0
                    ? estimatedPaymentDate.lengthOfMonth()
                    : invoice.getPaymentCondition().getDaySelect());
      }
    }
    invoice.setEstimatedPaymentDate(estimatedPaymentDate);
    invoiceRepo.save(invoice);
  }
}
