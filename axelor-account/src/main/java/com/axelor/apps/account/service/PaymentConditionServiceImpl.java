package com.axelor.apps.account.service;

import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentConditionLine;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class PaymentConditionServiceImpl implements PaymentConditionService {
  @Override
  public void checkPaymentCondition(PaymentCondition paymentCondition) throws AxelorException {
    if (paymentCondition == null) {
      return;
    }

    List<PaymentConditionLine> paymentConditionLineList =
        paymentCondition.getPaymentConditionLineList();
    if (CollectionUtils.isEmpty(paymentConditionLineList)
        || checkPercentage(paymentConditionLineList)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.PAYMENT_CONDITION_CONFIGURATION_ERROR),
          paymentCondition.getName());
    }
  }

  protected boolean checkPercentage(List<PaymentConditionLine> paymentConditionLineList) {
    return paymentConditionLineList.stream()
            .map(PaymentConditionLine::getPaymentPercentage)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .compareTo(BigDecimal.valueOf(100))
        != 0;
  }
}
