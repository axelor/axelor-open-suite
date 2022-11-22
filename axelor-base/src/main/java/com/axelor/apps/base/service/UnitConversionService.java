package com.axelor.apps.base.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.codehaus.groovy.control.CompilationFailedException;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.UnitConversion;
import com.axelor.exception.AxelorException;

public interface UnitConversionService {
	public BigDecimal convert(Unit startUnit, Unit endUnit, BigDecimal value, int scale, Product product) throws AxelorException;
	
	public BigDecimal getCoefficient(List<? extends UnitConversion> unitConversionList, Unit startUnit, Unit endUnit, Product product) throws AxelorException, CompilationFailedException, ClassNotFoundException, IOException;
}
