package com.axelor.apps.production.service;

import com.axelor.apps.sale.db.SaleOrderLine;
import java.math.BigDecimal;
import java.util.Optional;

public interface SaleOrderLineMoService {

  Optional<BigDecimal> fillQtyProduced(SaleOrderLine saleOrderLine);
}
