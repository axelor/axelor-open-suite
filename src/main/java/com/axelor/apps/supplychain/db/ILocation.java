package com.axelor.apps.supplychain.db;

public interface ILocation {

	/**
	 * Static location status select
	 */

	static final int INTERNAL = 1;
	static final int EXTERNAL = 2;
	static final int VIRTUAL = 3;
}
