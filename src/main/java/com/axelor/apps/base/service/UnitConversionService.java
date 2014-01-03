/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.base.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.UnitConversion;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.meta.service.MetaTranslations;
import com.google.inject.Inject;

public class UnitConversionService {
	
	@Inject
	private MetaTranslations metaTranslations;

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
		throw new AxelorException(String.format(metaTranslations.get(IExceptionMessage.UNIT_CONVERSION_1), 
				startUnit.getName(), endUnit.getName()), IException.CONFIGURATION_ERROR);

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
			throw new AxelorException(metaTranslations.get(IExceptionMessage.UNIT_CONVERSION_2), IException.CONFIGURATION_ERROR);
			
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
			throw new AxelorException(metaTranslations.get(IExceptionMessage.UNIT_CONVERSION_2), IException.CONFIGURATION_ERROR);
			
		if (startUnit.equals(endUnit))
			return value; 
		else { 		
			BigDecimal coefficient = this.getCoefficient(UnitConversion.all().fetch(), startUnit, endUnit);		
			
			return value.multiply(coefficient).setScale(6, RoundingMode.HALF_EVEN);		
		}	
	}
}