package com.axelor.apps.stock.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.repo.StockLocationRepository;

public class StockLocationDomainServiceImpl implements StockLocationDomainService {
  @Override
  public String getSiteDomain(StockLocation stockLocation) {
    StringBuilder domain = new StringBuilder();
    int typeSelect = stockLocation.getTypeSelect();
    domain.append(String.format("self.typeSelect = %d", typeSelect));

    Company company = stockLocation.getCompany();
    if (company != null) {
      domain.append(String.format(" AND self.company = %d", company.getId()));
    }

    Partner partner = stockLocation.getPartner();
    if (partner != null && typeSelect == StockLocationRepository.TYPE_EXTERNAL) {
      domain.append(String.format(" AND self.partner = %d", partner.getId()));
    }

    TradingName tradingName = stockLocation.getTradingName();
    if (tradingName != null) {
      domain.append(String.format(" AND self.tradingName = %d", tradingName.getId()));
    }
    return domain.toString();
  }
}
