package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.supplychain.db.MrpLine;
import com.axelor.apps.supplychain.db.MrpLineOrigin;
import com.axelor.apps.supplychain.db.MrpLineType;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;


public interface MrpLineService {
	
	public void generateProposal(MrpLine mrpLine) throws AxelorException;

	public MrpLine createMrpLine(Product product, int maxLevel, MrpLineType mrpLineType, BigDecimal qty, LocalDate maturityDate, BigDecimal cumulativeQty, Location location, Model... models);
	
	public MrpLineOrigin createMrpLineOrigin(Model model);

	public MrpLineOrigin copyMrpLineOrigin(MrpLineOrigin mrpLineOrigin);
}
