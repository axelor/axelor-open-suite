package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.stock.db.TrackingNumber;
import java.util.Optional;

public interface TrackingNumberCompanyService {

  Optional<Company> getCompany(TrackingNumber trackingNumber) throws AxelorException;
}
