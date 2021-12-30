package com.axelor.apps.account.service.payment.paymentvoucher;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.FinancialDiscount;
import com.axelor.apps.account.db.PayVoucherDueElement;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.FinancialDiscountRepository;
import com.axelor.apps.account.db.repo.PayVoucherDueElementRepository;
import com.axelor.apps.account.db.repo.PaymentVoucherRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import javax.inject.Inject;

public class PayVoucherDueElementServiceImpl implements PayVoucherDueElementService {

  protected PayVoucherDueElementRepository payVoucherDueElementRepository;
  protected AppBaseService appBaseService;
  protected AccountConfigService accountConfigService;
  protected AppAccountService appAccountService;

  private final int RETURN_SCALE = 2;
  private final int CALCULATION_SCALE = 10;

  @Inject
  public PayVoucherDueElementServiceImpl(
      PayVoucherDueElementRepository payVoucherDueElementRepository,
      AppBaseService appBaseService,
      AccountConfigService accountConfigService,
      AppAccountService appAccountService) {
    this.payVoucherDueElementRepository = payVoucherDueElementRepository;
    this.appBaseService = appBaseService;
    this.accountConfigService = accountConfigService;
    this.appAccountService = appAccountService;
  }

  @Override
  @Transactional
  public PayVoucherDueElement updateDueElementWithFinancialDiscount(
      PayVoucherDueElement payVoucherDueElement, PaymentVoucher paymentVoucher)
      throws AxelorException {
    if (paymentVoucher.getPartner() == null
        || paymentVoucher.getPartner().getFinancialDiscount() == null
        || payVoucherDueElement.getMoveLine() == null
        || payVoucherDueElement.getMoveLine().getDueDate() == null
        || paymentVoucher.getPaymentDate() == null) {
      return payVoucherDueElement;
    }
    FinancialDiscount financialDiscount = paymentVoucher.getPartner().getFinancialDiscount();
    LocalDate financialDiscountDeadlineDate =
        payVoucherDueElement
            .getMoveLine()
            .getDueDate()
            .minusDays(financialDiscount.getDiscountDelay());
    if (financialDiscountDeadlineDate.compareTo(paymentVoucher.getPaymentDate()) >= 0) {
      payVoucherDueElement.setApplyFinancialDiscount(true);
      payVoucherDueElement.setFinancialDiscount(financialDiscount);
      payVoucherDueElement.setFinancialDiscountDeadlineDate(financialDiscountDeadlineDate);
      payVoucherDueElement.setFinancialDiscountAmount(
          calculateFinancialDiscountAmount(payVoucherDueElement));
      payVoucherDueElement.setFinancialDiscountTaxAmount(
          calculateFinancialDiscountTaxAmount(payVoucherDueElement));
      payVoucherDueElement.setFinancialDiscountTotalAmount(
          calculateFinancialDiscountTotalAmount(payVoucherDueElement));
      payVoucherDueElement.setAmountRemainingFinDiscountDeducted(
          payVoucherDueElement
              .getAmountRemaining()
              .subtract(payVoucherDueElement.getFinancialDiscountTotalAmount()));
    }
    return payVoucherDueElement;
  }

  public BigDecimal calculateFinancialDiscountAmount(PayVoucherDueElement payVoucherDueElement)
      throws AxelorException {
    return calculateFinancialDiscountAmountUnscaled(payVoucherDueElement)
        .setScale(RETURN_SCALE, RoundingMode.HALF_UP);
  }

  protected BigDecimal calculateFinancialDiscountAmountUnscaled(
      PayVoucherDueElement payVoucherDueElement) throws AxelorException {
    if (payVoucherDueElement == null || payVoucherDueElement.getFinancialDiscount() == null) {
      return BigDecimal.ZERO;
    }

    BigDecimal baseAmount = payVoucherDueElement.getAmountRemaining();
    AccountConfig accountConfig =
        accountConfigService.getAccountConfig(
            payVoucherDueElement.getPaymentVoucher().getCompany());

    BigDecimal baseAmountByRate =
        baseAmount.multiply(
            payVoucherDueElement
                .getFinancialDiscount()
                .getDiscountRate()
                .divide(new BigDecimal(100), CALCULATION_SCALE, RoundingMode.HALF_UP));

    if (payVoucherDueElement.getFinancialDiscount().getDiscountBaseSelect()
        == FinancialDiscountRepository.DISCOUNT_BASE_HT) {
      return baseAmountByRate.setScale(CALCULATION_SCALE, RoundingMode.HALF_UP);
    } else if (payVoucherDueElement.getFinancialDiscount().getDiscountBaseSelect()
            == FinancialDiscountRepository.DISCOUNT_BASE_VAT
        && (payVoucherDueElement.getPaymentVoucher().getOperationTypeSelect()
                == PaymentVoucherRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
            || payVoucherDueElement.getPaymentVoucher().getOperationTypeSelect()
                == PaymentVoucherRepository.OPERATION_TYPE_CLIENT_SALE)
        && accountConfig.getPurchFinancialDiscountTax() != null) {
      return baseAmountByRate.divide(
          accountConfig
              .getPurchFinancialDiscountTax()
              .getActiveTaxLine()
              .getValue()
              .add(new BigDecimal(1)),
          CALCULATION_SCALE,
          RoundingMode.HALF_UP);
    } else if (payVoucherDueElement.getFinancialDiscount().getDiscountBaseSelect()
            == FinancialDiscountRepository.DISCOUNT_BASE_VAT
        && (payVoucherDueElement.getPaymentVoucher().getOperationTypeSelect()
                == PaymentVoucherRepository.OPERATION_TYPE_SUPPLIER_REFUND
            || payVoucherDueElement.getPaymentVoucher().getOperationTypeSelect()
                == PaymentVoucherRepository.OPERATION_TYPE_CLIENT_REFUND)
        && accountConfig.getSaleFinancialDiscountTax() != null) {
      return baseAmountByRate.divide(
          accountConfig
              .getSaleFinancialDiscountTax()
              .getActiveTaxLine()
              .getValue()
              .add(new BigDecimal(1)),
          CALCULATION_SCALE,
          RoundingMode.HALF_UP);
    } else {
      return BigDecimal.ZERO;
    }
  }

  @Override
  public BigDecimal calculateFinancialDiscountTaxAmount(PayVoucherDueElement payVoucherDueElement)
      throws AxelorException {
    return calculateFinancialDiscountTaxAmountUnscaled(payVoucherDueElement)
        .setScale(RETURN_SCALE, RoundingMode.HALF_UP);
  }

  protected BigDecimal calculateFinancialDiscountTaxAmountUnscaled(
      PayVoucherDueElement payVoucherDueElement) throws AxelorException {
    if (payVoucherDueElement == null
        || payVoucherDueElement.getFinancialDiscount() == null
        || payVoucherDueElement.getFinancialDiscount().getDiscountBaseSelect()
            != FinancialDiscountRepository.DISCOUNT_BASE_VAT) {
      return BigDecimal.ZERO;
    }

    BigDecimal financialDiscountAmount =
        calculateFinancialDiscountAmountUnscaled(payVoucherDueElement);

    AccountConfig accountConfig =
        accountConfigService.getAccountConfig(
            payVoucherDueElement.getPaymentVoucher().getCompany());
    if ((payVoucherDueElement.getPaymentVoucher().getOperationTypeSelect()
                == PaymentVoucherRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
            || payVoucherDueElement.getPaymentVoucher().getOperationTypeSelect()
                == PaymentVoucherRepository.OPERATION_TYPE_CLIENT_SALE)
        && accountConfig.getPurchFinancialDiscountTax() != null) {
      return financialDiscountAmount.multiply(
          accountConfig.getPurchFinancialDiscountTax().getActiveTaxLine().getValue());
    } else if ((payVoucherDueElement.getPaymentVoucher().getOperationTypeSelect()
                == PaymentVoucherRepository.OPERATION_TYPE_SUPPLIER_REFUND
            || payVoucherDueElement.getPaymentVoucher().getOperationTypeSelect()
                == PaymentVoucherRepository.OPERATION_TYPE_CLIENT_REFUND)
        && accountConfig.getSaleFinancialDiscountTax() != null) {
      return financialDiscountAmount.multiply(
          accountConfig.getSaleFinancialDiscountTax().getActiveTaxLine().getValue());
    }
    return BigDecimal.ZERO;
  }

  @Override
  public BigDecimal calculateFinancialDiscountTotalAmount(PayVoucherDueElement payVoucherDueElement)
      throws AxelorException {
    return (calculateFinancialDiscountAmountUnscaled(payVoucherDueElement)
            .add(calculateFinancialDiscountTaxAmountUnscaled(payVoucherDueElement)))
        .setScale(RETURN_SCALE, RoundingMode.HALF_UP);
  }

  public boolean applyFinancialDiscount(PayVoucherDueElement payVoucherDueElement) {
    return (payVoucherDueElement != null
        && payVoucherDueElement.getFinancialDiscount() != null
        && payVoucherDueElement.getFinancialDiscountDeadlineDate() != null
        && appAccountService.getAppAccount().getManageFinancialDiscount()
        && payVoucherDueElement.getMoveLine() != null
        && payVoucherDueElement.getMoveLine().getDueDate() != null
        && payVoucherDueElement
                .getFinancialDiscountDeadlineDate()
                .compareTo(payVoucherDueElement.getPaymentVoucher().getPaymentDate())
            >= 0);
  }

  @Override
  public PayVoucherDueElement updateAmounts(PayVoucherDueElement payVoucherDueElement)
      throws AxelorException {
    if (payVoucherDueElement != null && !payVoucherDueElement.getApplyFinancialDiscount()) {
      payVoucherDueElement.setAmountRemainingFinDiscountDeducted(
          payVoucherDueElement.getDueAmount().subtract(payVoucherDueElement.getPaidAmount()));
      payVoucherDueElement.setFinancialDiscountAmount(BigDecimal.ZERO);
      payVoucherDueElement.setFinancialDiscountTaxAmount(BigDecimal.ZERO);
      payVoucherDueElement.setFinancialDiscountTotalAmount(BigDecimal.ZERO);
    } else if (payVoucherDueElement != null
        && payVoucherDueElement.getApplyFinancialDiscount()
        && payVoucherDueElement.getPaymentVoucher() != null) {
      updateDueElementWithFinancialDiscount(
          payVoucherDueElement, payVoucherDueElement.getPaymentVoucher());
    }
    return payVoucherDueElement;
  }
}
