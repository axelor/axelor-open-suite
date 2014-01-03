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
package com.axelor.apps.tool.date;

import org.joda.time.LocalDate;
import org.joda.time.Months;

import com.google.inject.Inject;

/**
 * Classe permettant d'appliquer plusieurs opérations sur une période.
 * Une période est composée d'une date de début, d'une date de fin, et d'un
 * attribut booléen qui détermine si l'on fixe l'année sur 360 jours.
 * 
 */
public class Period {

	private LocalDate from;
	private LocalDate to;
	private boolean days360;
	
	@Inject
	public Period () {
		
	}

	public LocalDate getFrom() {
		return from;
	}

	public void setFrom(LocalDate from) {
		this.from = from;
	}

	public LocalDate getTo() {
		return to;
	}

	public void setTo(LocalDate to) {
		this.to = to;
	}

	public boolean isDays360() {
		return days360;
	}

	public void setDays360(boolean days360) {
		this.days360 = days360;
	}
	
	public Period(boolean days360) {
		this.days360 = days360;
		
	}

	public Period(LocalDate from, LocalDate to, boolean days360) {
		
		this.from = from;
		this.to = to;
		this.days360 = days360;
		
	}
	
	public Period(LocalDate from, LocalDate to) {
		
		this.from = from;
		this.to = to;
		this.days360 = false;
		
	}
	
	public Period(Period p) {
		
		this.from = p.getFrom();
		this.to = p.getTo();
		this.days360 = p.isDays360();
		
	}
	
	public int getDays() { return DateTool.daysBetween(this.from, this.to, this.days360); }
	
	public int getMonths() {
		
		if (this.days360) { return DateTool.days360MonthsBetween(this.from, this.to); }
		else { return Months.monthsBetween(this.from, this.to).getMonths(); }
		
	}
	
	public Period prorata(Period period) { return prorata(period.getFrom(), period.getTo()); }
	
	
	public Period prorata(LocalDate date1, LocalDate date2) {
		
		Period p = null;
		
		if (DateTool.isProrata(this.from, this.to, date1, date2)){
			
			p = new Period(this);
			
			if (date1.isAfter(this.from)) { p.setFrom(date1); }
			
			if (date2 != null && date2.isBefore(this.to)) { p.setTo(date2); }
		}
				
		return p;
	}
	
	public boolean isProrata (Period period) { return DateTool.isProrata(this.from, this.to, period.getFrom(), period.getTo()); }
	
	public boolean fromBetween(LocalDate date1, LocalDate date2) {
		
		return DateTool.isBetween(date1, date2, this.from);
		
	}
	
	public boolean toBetween(LocalDate date1, LocalDate date2) {

		return DateTool.isBetween(date1, date2, this.to);
		
	}
	
	public boolean contains(LocalDate date) { return DateTool.isBetween(this.from, this.to, date); }
	
	public boolean isNotNull(){ return this.getFrom() != null && this.getTo() != null; }
	
	@Override
	public boolean equals(Object obj){
		
		if (obj == this) {
            return true ;
        }
        
		if (obj instanceof Period) {

            Period period = (Period) obj;
            return this.from.equals(period.getFrom()) && this.to.equals(period.getTo()) && this.days360 == period.isDays360();
			
		}
		
		return false;
		
	}
	
	@Override
	public int hashCode(){
		
		if (days360) { return from.hashCode() ^ to.hashCode(); }
		else { return from.hashCode() ^ to.hashCode() * -1; }
		
	}
	
	@Override
	public String toString() {
		return this.from + " - " + this.to + "(Années sur 360 jours :" + this.days360 + ")";
	}
	
}
