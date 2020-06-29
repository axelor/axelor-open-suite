package com.axelor.apps.account.service;

import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerTurnover;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.PartnerTurnoverRepository;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PartnerTurnoverServiceImpl {

  private final Logger log = LoggerFactory.getLogger(PartnerTurnoverServiceImpl.class);
  @Inject AppBaseService appBaseService;
  @Inject CurrencyService currencyService;
  /**
   * Method of calculating turnover for one partner (customer/supplier)
   *
   * @param invoice
   * @param isSupplier: supplier or customer case
   */
  @Transactional
  public void calculCA(
      Partner partner,
      boolean isSupplier,
      Year yearFiscal,
      Year yearCivil,
      List<Partner> lstPartnerParent)
      throws AxelorException {

    // Retrieve of partner
    // Partner partner = invoice.getPartner();

    BigDecimal bgCivilYear = new BigDecimal(0);
    BigDecimal bgFiscalYear = new BigDecimal(0);

    // Retrieve of list objects of partnerTurnover existing for the Partner
    List<PartnerTurnover> lstPartnerTurnover = partner.getPartnerTurnoverList();

    /*
     * Search of existing objet PartnerTurnover for Fiscal year and Civil year
     */
    PartnerTurnover partnerTurnoverCivilYear =
        getPartnerTurnoverObject(
            lstPartnerTurnover,
            yearCivil,
            partner,
            (isSupplier
                ? PartnerTurnoverRepository.PARTNER_TYPE_SUPPLIER
                : PartnerTurnoverRepository.PARTNER_TYPE_CUSTOMER));
    PartnerTurnover partnerTurnoverFiscalYear = new PartnerTurnover();
    if (yearFiscal != null) {
      partnerTurnoverFiscalYear =
          getPartnerTurnoverObject(
              lstPartnerTurnover,
              yearFiscal,
              partner,
              (isSupplier
                  ? PartnerTurnoverRepository.PARTNER_TYPE_SUPPLIER
                  : PartnerTurnoverRepository.PARTNER_TYPE_CUSTOMER));
    }

    /** Calendar Year */
    /*
     * First treatment
     * Calcul of CA customer / supplier for the civil year
     */
    bgCivilYear = getCAPartner(partner, yearCivil, isSupplier, false, null);
    partnerTurnoverCivilYear.setRevenue(bgCivilYear);

    /*
     * Second treatment
     * Calcul of CA customer / supplier for the Fiscal year
     * only if date are different of Civil year
     */
    if (yearFiscal != null) {
      bgFiscalYear = getCAPartner(partner, yearFiscal, isSupplier, false, null);
      partnerTurnoverFiscalYear.setRevenue(bgFiscalYear);
    }

    // Variable list of All subsidiaries
    List<Partner> listChildPartner = new ArrayList<Partner>();
    listChildPartner.add(partner);
    // Call Method which identify subsidiaries
    listChildPartner = getListChild(listChildPartner);

    /** Fical Year */
    // Search if partner has subdiaries
    if (listChildPartner.size() > 1) {

      /*
       * First treatment
       * Calcul of CA customer / supplier for the civil year
       * for subdiaries of Partner
       */
      BigDecimal bgFilialeCivilYear =
          getCAPartner(partner, yearCivil, isSupplier, true, listChildPartner);
      // Add CA partner and CA subdiaries
      partnerTurnoverCivilYear.setRevenueSubsidiariesIncluded(bgFilialeCivilYear.add(bgCivilYear));

      /*
       * Second treatment
       * Calcul of CA customer / supplier for the Fiscal year
       * only if date are different of Civil year
       */
      if (yearFiscal != null) {
        BigDecimal bgFilialeFiscalYear =
            getCAPartner(partner, yearFiscal, isSupplier, true, listChildPartner);
        partnerTurnoverFiscalYear.setRevenueSubsidiariesIncluded(
            bgFilialeFiscalYear.add(bgFiscalYear));
      }
    } // end if of test Subdiaries

    // => Save
    Beans.get(PartnerRepository.class).save(partner);
  }

  /**
   * Method which calculate CA for a partner, start date and end date, refund of invoice, in the
   * ventilated status for partner only
   *
   * @param partner : objet Partner
   * @param year: Object year representing start and ane date of calculating
   * @param intTypeSelect : type of status
   * @return
   */
  @SuppressWarnings("unchecked")
  private BigDecimal getCalculCACurrency(
      Partner partner, List<Partner> lstPartner, Year year, int intTypeSelect) {

    // Query which return the sum of CA group by currency
    // After, uses the currency service for calculate in the good currency (depends of Partner)
    Query query =
        JPA.em()
            .createQuery(
                "SELECT new List(invoice.currency, sum(invoice.exTaxTotal)) FROM Invoice invoice "
                    + "WHERE invoice.partner in ?1 and "
                    + "invoice.operationTypeSelect = ?4 and "
                    + "invoice.statusSelect IN(3) and " // Only Ventilated status
                    + "invoice.invoiceDate >= ?2 and "
                    + "invoice.invoiceDate <= ?3 "
                    + "group by invoice.currency");
    query.setParameter(1, lstPartner);
    query.setParameter(2, year.getFromDate());
    query.setParameter(3, year.getToDate());
    query.setParameter(4, intTypeSelect);

    List<List<Object>> allCurrencySumList = new ArrayList<List<Object>>();
    allCurrencySumList = query.getResultList();
    BigDecimal amountCAbg = BigDecimal.ZERO;
    for (List<Object> objectList : allCurrencySumList) {

      // Currency ID
      Currency currencyObject = (Currency) objectList.get(0);
      BigDecimal amountCaCurrency = (BigDecimal) objectList.get(1);
      log.debug("Currency : " + currencyObject.getCode() + " : Amount = " + amountCaCurrency);
      try {
        BigDecimal rate =
            currencyService.getCurrencyConversionRate(currencyObject, partner.getCurrency());
        log.debug("Taux : " + rate);

        amountCAbg =
            amountCAbg
                .add(
                    amountCaCurrency.multiply(
                        currencyService.getCurrencyConversionRate(
                            currencyObject, partner.getCurrency())))
                .setScale(2, RoundingMode.HALF_EVEN);
        log.debug("Currency calculated: " + amountCAbg);

      } catch (AxelorException e) {
        amountCAbg = BigDecimal.ZERO;
      }
    }
    return (amountCAbg == null ? BigDecimal.ZERO : amountCAbg);
  }

  /**
   * Method which calculate CA for a partner, start date and end date, refund of invoice, in the
   * Validated or ventilated status for partner only
   *
   * @param partner : objet Partner
   * @param year: Object year representing start and ane date of calculating
   * @param intTypeSelect : type of status
   * @return
   */
  private BigDecimal getCalculCACurrency(Partner partner, Year year, int intTypeSelect) {

    List<Partner> lstPartner = new ArrayList<Partner>();
    lstPartner.add(partner);

    return getCalculCACurrency(partner, lstPartner, year, intTypeSelect);
  }

  /**
   * function which retrieve Year Object for the Calendar year current and the type of Year
   *
   * @return
   */
  public Year getYear(int intTypeSelect) {

    Year year =
        (Year)
            JPA.em()
                .createQuery(
                    "SELECT self FROM Year self "
                        + "WHERE self.fromDate <= ?1 and "
                        + "self.toDate > ?1 and "
                        + "self.typeSelect = ?2 ")
                .setParameter(1, appBaseService.getTodayDate())
                .setParameter(2, intTypeSelect)
                .getSingleResult();

    return year;
  }

  @SuppressWarnings("unchecked")
  private List<Partner> getListChild(List<Partner> lstPartner) {

    List<Partner> listPartnernResult =
        (List<Partner>)
            JPA.em()
                .createQuery("SELECT self FROM Partner self " + "WHERE parentPartner in ?1 ")
                .setParameter(1, lstPartner)
                .getResultList();

    if (listPartnernResult != null && listPartnernResult.size() > 0) {

      listPartnernResult.addAll(getListChild(listPartnernResult));
    }
    return listPartnernResult;
  }

  /**
   * Function to get a new object or the existing object of PartnerTrunover
   *
   * @param lstPartnerTurnover
   * @param yearCalendar
   * @param partner
   * @param intTypeOperation
   * @return
   */
  public PartnerTurnover getPartnerTurnoverObject(
      List<PartnerTurnover> lstPartnerTurnover,
      Year yearCalendar,
      Partner partner,
      int intTypeOperation) {
    PartnerTurnover partnerTurnover;

    List<PartnerTurnover> lstPTCivilYear =
        lstPartnerTurnover.stream()
            .filter(
                p ->
                    (p.getYear().equals(yearCalendar)
                        && p.getOperationTypeSelect() == intTypeOperation))
            .collect(Collectors.toList());
    if (lstPTCivilYear.size() > 0) {
      partnerTurnover = lstPTCivilYear.get(0);
    } else {
      partnerTurnover = new PartnerTurnover();
      partnerTurnover.setYear(yearCalendar);
      partnerTurnover.setCompany(
          Beans.get(CompanyRepository.class).findByCode(partner.getCompanyStr()));
      partnerTurnover.setPartner(partner);
      partnerTurnover.setOperationTypeSelect(intTypeOperation);
      lstPartnerTurnover.add(partnerTurnover);
    }

    return partnerTurnover;
  }

  /**
   * Calculate the CA total : CA Partner or with subdiaries for each one, calculate CA invoice - CA
   * refund
   *
   * @param partner
   * @param yearCalendar
   * @param intOperationType
   * @param withSubdiaries
   * @return the CA
   */
  public BigDecimal getCAPartner(
      Partner partner,
      Year yearCalendar,
      boolean isSupplier,
      boolean withSubdiaries,
      List<Partner> lstPartnerParent) {

    // Calcul des CA Factures Année civile (1)
    BigDecimal bgdCaFacture = new BigDecimal(0);
    if (!withSubdiaries) {
      bgdCaFacture =
          getCalculCACurrency(
              partner,
              yearCalendar,
              (isSupplier
                  ? InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
                  : InvoiceRepository.OPERATION_TYPE_CLIENT_SALE));
      log.debug("Currency bgdCaFacture : " + bgdCaFacture);
    } else {
      bgdCaFacture =
          getCalculCACurrency(
              partner,
              lstPartnerParent,
              yearCalendar,
              (isSupplier
                  ? InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
                  : InvoiceRepository.OPERATION_TYPE_CLIENT_SALE));
    }

    // Calcul des CA Avoirs Année Civile (2)
    BigDecimal bgdCaAvoir = new BigDecimal(0);
    if (!withSubdiaries) {
      bgdCaAvoir =
          getCalculCACurrency(
              partner,
              yearCalendar,
              (isSupplier
                  ? InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND
                  : InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND));
    } else {
      bgdCaAvoir =
          getCalculCACurrency(
              partner,
              lstPartnerParent,
              yearCalendar,
              (isSupplier
                  ? InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND
                  : InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND));
    }

    // Calcul du CA
    // (1) - (2)
    BigDecimal bgdCa = bgdCaFacture.subtract(bgdCaAvoir);

    return bgdCa;
  }
}
