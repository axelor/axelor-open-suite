/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.UnitConversion;
import com.axelor.apps.base.db.repo.UnitConversionRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.tool.template.TemplateMaker;
import com.google.inject.Inject;

public class UnitConversionService {
	
	private static final char TEMPLATE_DELIMITER = '$';
	protected TemplateMaker maker;
	
	@Inject
	protected GeneralService generalService;
	
	@Inject
	private UnitConversionRepository unitConversionRepo;

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
	public BigDecimal getCoefficient(List<? extends UnitConversion> unitConversionList, Unit startUnit, Unit endUnit) throws AxelorException {
		/* Looking for the start unit and the end unit in the unitConversionList to get the coefficient */
		for (UnitConversion unitConversion : unitConversionList){

			if (unitConversion.getStartUnit().equals(startUnit) && unitConversion.getEndUnit().equals(endUnit)) { return unitConversion.getCoef(); }

		}
		/* The endUnit become the start unit and the startUnit become the end unit */
		for (UnitConversion unitConversion : unitConversionList){

			if (unitConversion.getStartUnit().equals(endUnit) && unitConversion.getEndUnit().equals(startUnit) && unitConversion.getCoef().compareTo(BigDecimal.ZERO) != 0) { return BigDecimal.ONE.divide(unitConversion.getCoef(), generalService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_EVEN); }

		}
		/* If there is no startUnit and endUnit in the UnitConversion list so we throw an exception */
		throw new AxelorException(String.format(I18n.get(IExceptionMessage.UNIT_CONVERSION_1),
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
			throw new AxelorException(I18n.get(IExceptionMessage.UNIT_CONVERSION_2), IException.CONFIGURATION_ERROR);

		if (startUnit.equals(endUnit))
			return value;
		else {
			BigDecimal coefficient = this.getCoefficient(unitConversionList, startUnit, endUnit);

			return value.multiply(coefficient).setScale(generalService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_EVEN);
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
			throw new AxelorException(I18n.get(IExceptionMessage.UNIT_CONVERSION_2), IException.CONFIGURATION_ERROR);

		if (startUnit.equals(endUnit))
			return value;
		else {
			BigDecimal coefficient = this.getCoefficient(unitConversionRepo.all().fetch(), startUnit, endUnit);

			return value.multiply(coefficient).setScale(generalService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_EVEN);
		}
	}
	
	
	public BigDecimal convertWithProduct(Unit startUnit, Unit endUnit, BigDecimal value, Product product) throws AxelorException {

		if (startUnit == null || endUnit == null)
			throw new AxelorException(I18n.get(IExceptionMessage.UNIT_CONVERSION_2), IException.CONFIGURATION_ERROR);

		if (startUnit.equals(endUnit))
			return value;
		else {
			try{
				BigDecimal coefficient = this.getCoefficient(unitConversionRepo.all().fetch(), startUnit, endUnit, product);

				return value.multiply(coefficient).setScale(generalService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_EVEN);
			}
			catch(IOException e){
				e.printStackTrace();
			}
			catch(ClassNotFoundException e){
				e.printStackTrace();
			}
		}
		return value;
	}
	
	public BigDecimal getCoefficient(List<? extends UnitConversion> unitConversionList, Unit startUnit, Unit endUnit, Product product) throws AxelorException, CompilationFailedException, ClassNotFoundException, IOException {
		/* Looking for the start unit and the end unit in the unitConversionList to get the coefficient */
		this.maker = new TemplateMaker( Locale.FRENCH, TEMPLATE_DELIMITER, TEMPLATE_DELIMITER);
		this.maker.setContext(product, "Product");
		String eval = null;
		for (UnitConversion unitConversion : unitConversionList){

			if (unitConversion.getStartUnit().equals(startUnit) && unitConversion.getEndUnit().equals(endUnit)) { 
				if(unitConversion.getTypeSelect() == UnitConversionRepository.TYPE_COEFF){
					return unitConversion.getCoef(); 
				}
				else{
					maker.setTemplate(unitConversion.getFormula());
					eval = maker.make();
					CompilerConfiguration conf = new CompilerConfiguration();
					ImportCustomizer customizer = new ImportCustomizer();
					customizer.addStaticStars("java.lang.Math");                        
					conf.addCompilationCustomizers(customizer);
					Binding binding = new Binding();                                 
					GroovyShell shell = new GroovyShell(binding,conf);
					return new BigDecimal(shell.evaluate(eval).toString());   
				}
			}

		}
		/* The endUnit become the start unit and the startUnit become the end unit */
		for (UnitConversion unitConversion : unitConversionList){

			if (unitConversion.getStartUnit().equals(endUnit) && unitConversion.getEndUnit().equals(startUnit)) { 
				if(unitConversion.getTypeSelect() == UnitConversionRepository.TYPE_COEFF && unitConversion.getCoef().compareTo(BigDecimal.ZERO) != 0){
					return BigDecimal.ONE.divide(unitConversion.getCoef(), generalService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_EVEN);  
				}
				else{
					maker.setTemplate(unitConversion.getFormula());
					eval = maker.make();
					CompilerConfiguration conf = new CompilerConfiguration();
					ImportCustomizer customizer = new ImportCustomizer();
					customizer.addStaticStars("java.lang.Math");                        
					conf.addCompilationCustomizers(customizer);
					Binding binding = new Binding();                                 
					GroovyShell shell = new GroovyShell(binding,conf);
					BigDecimal result = new BigDecimal(shell.evaluate(eval).toString()); 
					if(result.compareTo(BigDecimal.ZERO) != 0){
						return BigDecimal.ONE.divide(result, generalService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_EVEN);
					}
				}
			}

		}
		/* If there is no startUnit and endUnit in the UnitConversion list so we throw an exception */
		throw new AxelorException(String.format(I18n.get(IExceptionMessage.UNIT_CONVERSION_1),
				startUnit.getName(), endUnit.getName()), IException.CONFIGURATION_ERROR);

	}
	
}
