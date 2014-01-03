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
package com.axelor.apps.tool.db;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.joda.time.LocalDate;

import com.axelor.db.JPA;
import com.axelor.db.JpaModel;
import com.axelor.db.Query;

@Entity
@Table(name = "TEST_INVOICE")
public class Invoice extends JpaModel {

	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Move move;
	
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Move oldMove;
	
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MoveLine rejectMoveLine;
	
	private LocalDate date;

	private LocalDate dueDate;

	public Move getMove() {
		return move;
	}
	
	public void setMove(Move move) {
		this.move = move;
	}
	
	public Move getOldMove() {
		return oldMove;
	}
	
	public void setOldMove(Move oldMove) {
		this.oldMove = oldMove;
	}
	
	public MoveLine getRejectMoveLine() {
		return rejectMoveLine;
	}
	
	public void setRejectMoveLine(MoveLine rejectMoveLine) {
		this.rejectMoveLine = rejectMoveLine;
	}
	
	public LocalDate getDate() {
		return date;
	}
	
	public void setDate(LocalDate date) {
		this.date = date;
	}
	
	public LocalDate getDueDate() {
		return dueDate;
	}
	
	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}
	
	public Invoice persist() {
		return JPA.persist(this);
	}
	
	public Invoice merge() {
		return JPA.merge(this);
	}
	
	public Invoice save() {
		return JPA.save(this);
	}
	
	public void remove() {
		JPA.remove(this);
	}
	
	public void refresh() {
		JPA.refresh(this);
	}
	
	public void flush() {
		JPA.flush();
	}
	
	public static Invoice find(Long id) {
		return JPA.find(Invoice.class, id);
	}
	
	public static Query<Invoice> all() {
		return JPA.all(Invoice.class);
	}
}
