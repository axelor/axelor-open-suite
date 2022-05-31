package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.tool.api.ResponseStructure;
import java.math.BigDecimal;
import java.util.Map;

public class StockProductResponse extends ResponseStructure {

  private final long id;
  private final BigDecimal realQty;
  private final BigDecimal futureQty;

  public StockProductResponse(Product product, Map<String, Object> qtys) {
    super(product.getVersion());
    this.id = product.getId();
    this.realQty = (BigDecimal) qtys.get("$realQty");
    this.futureQty = (BigDecimal) qtys.get("$futureQty");
  }

  public long getId() {
    return id;
  }

  public BigDecimal getRealQty() {
    return realQty;
  }

  public BigDecimal getFutureQty() {
    return futureQty;
  }
}
