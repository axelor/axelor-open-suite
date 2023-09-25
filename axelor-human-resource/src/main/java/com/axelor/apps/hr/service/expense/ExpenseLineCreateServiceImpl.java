package com.axelor.apps.hr.service.expense;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.KilometricAllowParam;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.KilometricService;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.apps.project.db.Project;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseLineCreateServiceImpl implements ExpenseLineCreateService {

  protected ExpenseLineRepository expenseLineRepository;
  protected AppHumanResourceService appHumanResourceService;
  protected KilometricService kilometricService;
  protected HRConfigService hrConfigService;
  protected AppBaseService appBaseService;
  protected ExpenseProofFileService expenseProofFileService;

  @Inject
  public ExpenseLineCreateServiceImpl(
      ExpenseLineRepository expenseLineRepository,
      AppHumanResourceService appHumanResourceService,
      KilometricService kilometricService,
      HRConfigService hrConfigService,
      AppBaseService appBaseService,
      ExpenseProofFileService expenseProofFileService) {
    this.expenseLineRepository = expenseLineRepository;
    this.appHumanResourceService = appHumanResourceService;
    this.kilometricService = kilometricService;
    this.hrConfigService = hrConfigService;
    this.appBaseService = appBaseService;
    this.expenseProofFileService = expenseProofFileService;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public ExpenseLine createGeneralExpenseLine(
      Project project,
      Product expenseProduct,
      LocalDate expenseDate,
      BigDecimal totalAmount,
      BigDecimal totalTax,
      MetaFile justificationMetaFile,
      String comments,
      Employee employee,
      Currency currency,
      Boolean toInvoice)
      throws AxelorException {

    if (expenseProduct == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_LINE_MISSING_EXPENSE_PRODUCT));
    }

    ExpenseLine expenseLine =
        createBasicExpenseLine(project, employee, expenseDate, comments, currency, toInvoice);
    setGeneralExpenseLineInfo(
        expenseProduct, totalAmount, totalTax, justificationMetaFile, expenseLine);
    expenseProofFileService.convertProofFileToPdf(expenseLine);
    return expenseLineRepository.save(expenseLine);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public ExpenseLine createKilometricExpenseLine(
      Project project,
      LocalDate expenseDate,
      KilometricAllowParam kilometricAllowParam,
      Integer kilometricType,
      BigDecimal distance,
      String fromCity,
      String toCity,
      String comments,
      Employee employee,
      Company company,
      Currency currency,
      Boolean toInvoice)
      throws AxelorException {

    checkKilometricLineRequiredValues(
        kilometricAllowParam, kilometricType, fromCity, toCity, company);

    ExpenseLine expenseLine =
        createBasicExpenseLine(project, employee, expenseDate, comments, currency, toInvoice);
    setKilometricExpenseLineInfo(
        kilometricAllowParam, kilometricType, fromCity, toCity, company, expenseLine);

    computeDistance(distance, expenseLine);
    computeAmount(employee, expenseLine);

    return expenseLineRepository.save(expenseLine);
  }

  protected void computeAmount(Employee employee, ExpenseLine expenseLine) throws AxelorException {
    BigDecimal amount = kilometricService.computeKilometricExpense(expenseLine, employee);
    expenseLine.setTotalAmount(amount);
    expenseLine.setUntaxedAmount(amount);
  }

  protected void computeDistance(BigDecimal distance, ExpenseLine expenseLine)
      throws AxelorException {
    if (distance == null) {
      expenseLine.setDistance(BigDecimal.ZERO);
    }

    expenseLine.setDistance(distance);
    if (appHumanResourceService.getAppExpense().getComputeDistanceWithWebService()) {
      expenseLine.setDistance(kilometricService.computeDistance(expenseLine));
    }
  }

  protected void checkKilometricLineRequiredValues(
      KilometricAllowParam kilometricAllowParam,
      Integer kilometricType,
      String fromCity,
      String toCity,
      Company company)
      throws AxelorException {
    if (kilometricAllowParam == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_LINE_MISSING_KILOMETRIC_ALLOWANCE_PARAM));
    }

    if (kilometricType == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_LINE_MISSING_KILOMETRIC_TYPE));
    }

    if (fromCity == null || toCity == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_LINE_MISSING_CITIES));
    }

    if (company == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_LINE_MISSING_COMPANY));
    }
  }

  protected void setGeneralExpenseLineInfo(
      Product expenseProduct,
      BigDecimal totalAmount,
      BigDecimal totalTax,
      MetaFile justificationMetaFile,
      ExpenseLine expenseLine)
      throws AxelorException {

    checkExpenseProduct(expenseProduct);

    expenseLine.setIsAloneMeal(expenseProduct.getDeductLunchVoucher());
    expenseLine.setExpenseProduct(expenseProduct);
    expenseLine.setJustificationMetaFile(justificationMetaFile);

    setAmountAndTax(expenseProduct, totalAmount, totalTax, expenseLine);
  }

  protected void setAmountAndTax(
      Product expenseProduct,
      BigDecimal totalAmount,
      BigDecimal totalTax,
      ExpenseLine expenseLine) {
    if (totalAmount != null) {
      expenseLine.setTotalAmount(totalAmount);
    }

    if (totalTax != null) {
      expenseLine.setTotalTax(totalTax);

      if (totalAmount != null) {
        expenseLine.setUntaxedAmount(totalAmount.subtract(totalTax));
      }

      if (expenseProduct.getBlockExpenseTax()) {
        expenseLine.setTotalTax(BigDecimal.ZERO);
        expenseLine.setUntaxedAmount(totalAmount);
      }
    }
  }

  protected void checkExpenseProduct(Product expenseProduct) throws AxelorException {
    User user = AuthUtils.getUser();
    if (user != null) {
      Employee userEmployee = user.getEmployee();
      if (!userEmployee.getHrManager() && expenseProduct.getUnavailableToUsers()) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(HumanResourceExceptionMessage.EXPENSE_LINE_EXPENSE_TYPE_NOT_ALLOWED));
      }
    }
  }

  protected void setKilometricExpenseLineInfo(
      KilometricAllowParam kilometricAllowParam,
      Integer kilometricType,
      String fromCity,
      String toCity,
      Company company,
      ExpenseLine expenseLine)
      throws AxelorException {
    expenseLine.setExpenseProduct(
        hrConfigService.getKilometricExpenseProduct(hrConfigService.getHRConfig(company)));
    expenseLine.setFromCity(fromCity);
    expenseLine.setToCity(toCity);
    expenseLine.setKilometricAllowParam(kilometricAllowParam);
    expenseLine.setKilometricTypeSelect(kilometricType);
  }

  protected ExpenseLine createBasicExpenseLine(
      Project project,
      Employee employee,
      LocalDate expenseDate,
      String comments,
      Currency currency,
      Boolean toInvoice)
      throws AxelorException {
    ExpenseLine expenseLine = new ExpenseLine();
    if (expenseDate.isAfter(appBaseService.getTodayDate(null))) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          HumanResourceExceptionMessage.EXPENSE_LINE_DATE_ERROR);
    }

    setCurrency(currency, expenseLine);
    expenseLine.setProject(project);
    expenseLine.setEmployee(employee);
    expenseLine.setExpenseDate(expenseDate);
    expenseLine.setComments(comments);
    expenseLine.setTotalAmount(BigDecimal.ZERO);
    return expenseLine;
  }

  protected void setCurrency(Currency currency, ExpenseLine expenseLine) {
    if (currency != null) {
      expenseLine.setCurrency(currency);
    } else {
      setActiveCompanyCurrency(expenseLine);
    }
  }

  protected void setActiveCompanyCurrency(ExpenseLine expenseLine) {
    User user = AuthUtils.getUser();
    if (user != null) {
      Company company = user.getActiveCompany();
      if (company != null) {
        expenseLine.setCurrency(company.getCurrency());
      }
    }
  }
}
