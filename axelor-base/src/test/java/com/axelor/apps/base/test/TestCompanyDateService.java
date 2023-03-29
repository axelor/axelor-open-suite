package com.axelor.apps.base.test;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.CompanyDateService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestCompanyDateService {

  private static final String DD_MM_YYYY_HH_MM = "dd/MM/yyyy HH:mm";
  private static final String M_D_YYYY_H_MM_A = "M/d/yyyy, h:mm a";
  private static final String DD_MM_YYYY = "dd/MM/yyyy";
  private static final String M_D_YYYY = "M/d/yyyy";

  protected LocalDateTime todayDateTime = LocalDateTime.of(2023, 3, 28, 0, 0, 0);
  protected LocalDate todayDate = LocalDate.of(2023, 3, 28);

  protected CompanyDateService companyDateService;
  protected Company usCompany;
  protected Company frCompany;

  @Before
  public void prepare() {
    companyDateService = new CompanyDateService();
    usCompany = prepareCompany(Locale.US.toLanguageTag());
    frCompany = prepareCompany(Locale.FRANCE.toLanguageTag());
  }

  @Test
  public void testDateFormatterUs() throws AxelorException {
    Assert.assertEquals(getDateTimeFormat(M_D_YYYY), getCompanyDateformat(usCompany));
  }

  @Test
  public void testDateFormatterFr() throws AxelorException {
    Assert.assertEquals(getDateTimeFormat(DD_MM_YYYY), getCompanyDateformat(frCompany));
  }

  @Test
  public void testDateTimeFormatterUs() throws AxelorException {
    Assert.assertEquals(getDateTimeFormat(M_D_YYYY_H_MM_A), getCompanyDateTimeformat(usCompany));
  }

  @Test
  public void testDateTimeFormatterFr() throws AxelorException {
    Assert.assertEquals(getDateTimeFormat(DD_MM_YYYY_HH_MM), getCompanyDateTimeformat(frCompany));
  }

  protected Company prepareCompany(String languageTag) {
    Company company = new Company();
    company.setLocale(languageTag);
    return company;
  }

  protected String getDateTimeFormat(String pattern) {
    return DateTimeFormatter.ofPattern(pattern).format(todayDateTime);
  }

  protected String getCompanyDateformat(Company company) throws AxelorException {
    return companyDateService.getDateFormat(company).format(todayDate);
  }

  protected String getCompanyDateTimeformat(Company company) throws AxelorException {
    return companyDateService.getDateTimeFormat(company).format(todayDateTime);
  }
}
