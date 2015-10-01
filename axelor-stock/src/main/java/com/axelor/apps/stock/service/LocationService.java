package com.axelor.apps.stock.service;

import java.math.BigDecimal;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.Location;

public interface LocationService {
	
	public Location getDefaultLocation();
	
	public BigDecimal getQty(Long productId, Long locationId, String qtyType);
	
	public BigDecimal getRealQty(Long productId, Long locationId);
	
	public BigDecimal getFutureQty(Long productId, Long locationId);
	
}
