package com.axelor.apps.base.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.UnitConversion;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

public class UnitConversionService {

	/**
	 * Obtenir le coefficient entre deux unités dans une liste de conversion. Si l'unité de départ et l'unité
	 * d'arrivée ne se trouve pas dans la liste alors on inverse l'unité de départ avec l'unité d'arrivée.
	 * Si il n'y a toujours pas de résultat alors on déclenche une exception.
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
	 * @throws AxelorException Les unités demandés ne se trouvent pas dans la liste de conversion
	 */
	public BigDecimal getCoefficient(List<UnitConversion> unitConversionList, Unit startUnit, Unit endUnit) throws AxelorException {
		/* Looking for the start unit and the end unit in the unitConversionList to get the coefficient */
		for (UnitConversion unitConversion : unitConversionList){
			
			if (unitConversion.getStartUnit().equals(startUnit) && unitConversion.getEndUnit().equals(endUnit)) { return unitConversion.getCoef(); }
			
		}
		/* The endUnit become the start unit and the startUnit become the end unit */
		for (UnitConversion unitConversion : unitConversionList){
			
			if (unitConversion.getStartUnit().equals(endUnit) && unitConversion.getEndUnit().equals(startUnit)) { return BigDecimal.ONE.divide(unitConversion.getCoef(), 6, RoundingMode.HALF_EVEN); }
			
		}
		/* If there is no startUnit and endUnit in the UnitConversion list so we throw an exception */
		throw new AxelorException("Veuillez configurer les conversions d'unités de '"+startUnit.getName()+"' à '"+endUnit.getName()+"'.", IException.CONFIGURATION_ERROR);
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
	 * @throws AxelorException Les unités demandés ne se trouvent pas dans la liste de conversion
	 */
	public BigDecimal convert(List<UnitConversion> unitConversionList, Unit startUnit, Unit endUnit, BigDecimal value) throws AxelorException {
		 
		if (startUnit == null || endUnit == null)
			throw new AxelorException("Veuillez configurer les conversions d'unités.", IException.CONFIGURATION_ERROR);
			
		if (startUnit.equals(endUnit))
			return value; 
		else { 		
			BigDecimal coefficient = this.getCoefficient(unitConversionList, startUnit, endUnit);		
			
			return value.multiply(coefficient).setScale(6, RoundingMode.HALF_EVEN);		
		}	
	}
	
	
	/**
	 * Convertir la valeur passée en paramètre en fonction des unités.
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
	 * @throws AxelorException Les unités demandés ne se trouvent pas dans la liste de conversion
	 */
	public BigDecimal convert(Unit startUnit, Unit endUnit, BigDecimal value) throws AxelorException {
		 
		if (startUnit == null || endUnit == null)
			throw new AxelorException("Veuillez configurer les conversions d'unités.", IException.CONFIGURATION_ERROR);
			
		if (startUnit.equals(endUnit))
			return value; 
		else { 		
			BigDecimal coefficient = this.getCoefficient(UnitConversion.all().fetch(), startUnit, endUnit);		
			
			return value.multiply(coefficient).setScale(6, RoundingMode.HALF_EVEN);		
		}	
	}
}