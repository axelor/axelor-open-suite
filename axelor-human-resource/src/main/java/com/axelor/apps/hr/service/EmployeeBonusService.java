/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.EmployeeBonusMgt;
import com.axelor.apps.hr.db.EmployeeBonusMgtLine;
import com.axelor.apps.hr.db.repo.EmployeeBonusMgtLineRepository;
import com.axelor.apps.hr.db.repo.EmployeeBonusMgtRepository;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.service.employee.EmployeeServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.tool.template.TemplateMaker;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

public class EmployeeBonusService {
	
	@Inject
	EmployeeBonusMgtRepository employeeBonusMgtRepo;
	
	@Inject
	EmployeeBonusMgtLineRepository employeeBonusMgtLineRepo;
	
	@Inject
	EmployeeServiceImpl employeeService;
	
	private static final char TEMPLATE_DELIMITER = '$';
	
	@Transactional
	public void compute(EmployeeBonusMgt bonus) throws AxelorException{
		if ( bonus.getEmployeeBonusMgtLineList() == null || bonus.getEmployeeBonusMgtLineList().isEmpty() ){
			List<Employee> allEmployee = Beans.get(EmployeeRepository.class).all().filter("self.user.activeCompany = ?1", bonus.getCompany()).fetch();
			TemplateMaker maker = new TemplateMaker( Locale.FRENCH, TEMPLATE_DELIMITER, TEMPLATE_DELIMITER);
			String eval = "";
			CompilerConfiguration conf = new CompilerConfiguration();
			ImportCustomizer customizer = new ImportCustomizer();
			customizer.addStaticStars("java.lang.Math");                        
			conf.addCompilationCustomizers(customizer);
			Binding binding = new Binding();                                 
			GroovyShell shell = new GroovyShell(binding,conf);
			
			for (Employee employee : allEmployee) {
				maker.setContext(employee, "Employee");
				String formula = bonus.getEmployeeBonusType().getApplicationCondition();
				Integer lineStatus;
				try {
					formula = formula.replace(bonus.getCompany().getHrConfig().getAgeVariableName(), String.valueOf(employeeService.getAge(employee, bonus.getPayPeriod().getFromDate())))
									 .replace(bonus.getCompany().getHrConfig().getSeniorityVariableName(), String.valueOf(employeeService.getLengthOfService(employee, bonus.getPayPeriod().getFromDate())));
					lineStatus = EmployeeBonusMgtLineRepository.STATUS_CALCULATED;
				} catch (AxelorException e) {
					TraceBackService.trace(e);
					formula = "true";
					lineStatus = EmployeeBonusMgtLineRepository.STATUS_ANOMALY;
				}
				maker.setTemplate(formula);
				eval = maker.make();
				
				if (shell.evaluate(eval).toString().equals("true")) { 
					EmployeeBonusMgtLine line = new EmployeeBonusMgtLine();
					line.setEmployeeBonusMgt(bonus);
					line.setEmployee(employee);
					line.setStatusSelect(lineStatus);

					if (lineStatus == EmployeeBonusMgtLineRepository.STATUS_ANOMALY) {
						employeeBonusMgtLineRepo.save(line);
						continue;
					}

					line.setSeniorityDate( employee.getSeniorityDate() );
					line.setCoef( employee.getBonusCoef() );
					line.setPresence( employee.getPlanning() );

					maker.setContext(line, "employeeBonusMgtLine");
					maker.setTemplate( bonus.getEmployeeBonusType().getFormula() );
					eval = maker.make();
					line.setAmount( new BigDecimal(  shell.evaluate(eval).toString() ) );

					employeeBonusMgtLineRepo.save(line);
				}
			}
		}
		bonus.setStatusSelect( EmployeeBonusMgtRepository.STATUS_CALCULATED );
		employeeBonusMgtRepo.save(bonus);
	}

}
