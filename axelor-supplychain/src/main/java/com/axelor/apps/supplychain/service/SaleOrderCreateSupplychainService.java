package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.TaxNumber;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.stock.db.Incoterm;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.auth.db.User;
import com.axelor.team.db.Team;
import java.time.LocalDate;

public interface SaleOrderCreateSupplychainService {

  SaleOrder createSaleOrder(
      User salespersonUser,
      Company company,
      Partner contactPartner,
      Currency currency,
      LocalDate estimatedShippingDate,
      String internalReference,
      String externalReference,
      StockLocation stockLocation,
      PriceList priceList,
      Partner clientPartner,
      Team team,
      TaxNumber taxNumber,
      String internalNotes,
      FiscalPosition fiscalPosition,
      TradingName tradingName,
      Incoterm incoterm,
      Partner invoicedPartner,
      Partner deliveredPartner)
      throws AxelorException;
}
