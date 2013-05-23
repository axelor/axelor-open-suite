package com.axelor.apps.base.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.UnitConversion;

public class UnitConversionService {

	/**
	 * Obtenir le coefficient entre deux unités dans une liste de conversion.
	 * 
	 * @param unitConversionList
	 * 				La liste des unités de conversion.
	 * 
	 * @param startUnit
	 * 				L'unité de départ.
	 * 
	 * @param endUnit
	 * 				L'unité d'arrivée.
	 * 
	 * @return Le coefficient de conversion.
	 */
	public BigDecimal getCoefficient(List<UnitConversion> unitConversionList, Unit startUnit, Unit endUnit) {

		for (UnitConversion unitConversion : unitConversionList){
			
			if (unitConversion.getStartUnit().equals(startUnit) && unitConversion.getEndUnit().equals(endUnit)) { return unitConversion.getCoef(); }
		}
		
		return BigDecimal.ONE;
		
	}
	
	/**
	 * Convertir la valeur passée en paramètre en fonction des unités.
	 * 
	 * @param unitConversionList
	 * 				La liste des unités de conversion.
	 * 
	 * @param startUnit
	 * 				L'unité de départ.
	 * 
	 * @param endUnit
	 * 				L'unité d'arrivée.
	 * 
	 * @param value
	 * 				La valeur à convertir.
	 * 
	 * @return Le coefficient de conversion.
	 */
	public BigDecimal convert(List<UnitConversion> unitConversionList, Unit startUnit, Unit endUnit, BigDecimal value) {
		 
		if (startUnit == null || endUnit == null || startUnit.equals(endUnit)) { return value; }
		else { return value.multiply(this.getCoefficient(unitConversionList, startUnit, endUnit)).setScale(6, RoundingMode.HALF_EVEN); }
		
	}

}