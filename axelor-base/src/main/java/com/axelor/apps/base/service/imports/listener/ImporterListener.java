/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.imports.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.data.Listener;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;

public class ImporterListener implements Listener {

	protected Logger log = LoggerFactory.getLogger(getClass());

	private String name, importLog = "";
	private int totalRecord, successRecord, notNull, anomaly;
	
	public ImporterListener( String name ){ 
		this.name = name;
		this.totalRecord = this.successRecord = this.notNull = this.anomaly = 0;
	}
	
	public String getImportLog(){
		
		String log = importLog;
		log += "\nTotal : "+ totalRecord + " - Réussi : " + successRecord + " - Non null : " + notNull;
		log += "\nAnomalies générées : " + anomaly;
		
		return log;
	}

	@Override
	public void imported(Model bean) { if( bean != null ) { notNull++; } }

	@Override
	public void imported(Integer total, Integer success) {
		totalRecord = total; successRecord = success;
	}

	@Override
	public void handle(Model bean, Exception e) {
		anomaly++;
		importLog += "\n"+e;
		TraceBackService.trace( new AxelorException (
				String.format( "La ligne ne peut être importée (import : %s)", name ), 
				e, IException.FUNCTIONNAL )
		, IException.IMPORT );
	}

}
