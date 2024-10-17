package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import java.util.Objects;

public class MassStockMovableProductAttrsServiceImpl
    implements MassStockMovableProductAttrsService {
  @Override
  public String getStockLocationDomain(MassStockMove massStockMove) {
    Objects.requireNonNull(massStockMove);

    var domain = new StringBuilder();
    domain.append(String.format("self.typeSelect != %d", StockLocationRepository.TYPE_VIRTUAL));

    if (massStockMove.getCompany() != null) {
      domain.append(String.format(" and self.company.id = %d", massStockMove.getCompany().getId()));
    }

    if (massStockMove.getCartStockLocation() != null) {
      domain.append(
          String.format(" and self.id != %d", massStockMove.getCartStockLocation().getId()));
    }

    return domain.toString();
  }
}
