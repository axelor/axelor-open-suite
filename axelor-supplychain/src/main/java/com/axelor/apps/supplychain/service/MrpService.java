package com.axelor.apps.supplychain.service;

import com.axelor.apps.supplychain.db.Mrp;
import com.axelor.exception.AxelorException;


public interface MrpService {
	
	public void runCalculation(Mrp mrp) throws AxelorException;
	
	public void generateProposals(Mrp mrp) throws AxelorException;
	
	public void reset(Mrp mrp);

}
