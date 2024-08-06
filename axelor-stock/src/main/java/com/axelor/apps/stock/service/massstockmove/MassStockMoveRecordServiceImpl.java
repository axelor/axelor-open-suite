package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import java.util.Objects;
import java.util.Optional;

public class MassStockMoveRecordServiceImpl implements MassStockMoveRecordService {

  @Override
  public void onNew(MassStockMove massStockMove) {
    Objects.requireNonNull(massStockMove);

    massStockMove.setCompany(
        Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null));

    massStockMove.setCartStockLocation(
        Optional.ofNullable(massStockMove.getCompany())
            .map(Company::getStockConfig)
            .map(StockConfig::getCartStockLocation)
            .orElse(null));
  }
}
