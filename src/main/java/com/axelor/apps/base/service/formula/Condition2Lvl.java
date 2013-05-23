package com.axelor.apps.base.service.formula;


public interface Condition2Lvl<P1, P2> extends Condition1Lvl<P1> {

	public boolean isRunnable(String key, P1 parameter1, P2 parameter2);
	
}
