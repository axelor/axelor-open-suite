package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.stock.db.MassMove;
import com.axelor.apps.stock.db.StockLocation;

public interface MassMoveService {

  public void importProductFromStockLocation(MassMove massMove) throws AxelorException;

  public void setStatusSelectToDraft(MassMove massMove);

  public MassMove createMassMoveMobility(
      Integer statusSelect,
      Company company,
      StockLocation cartStockLocation,
      StockLocation commonFromStockLocation,
      StockLocation CommonToStockLocation)
      throws AxelorException;

  public void updateMassMoveMobility(
      Long id,
      Integer statusSelect,
      Company company,
      StockLocation cartStockLocation,
      StockLocation commonFromStockLocation,
      StockLocation CommonToStockLocation);

  public String getAndSetSequence(Company company, MassMove massMoveToSet) throws AxelorException;
}
