/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
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
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.tool.db;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.axelor.db.JPA;
import com.axelor.db.JpaModel;
import com.axelor.db.Query;

@Entity
@Table(name = "TEST_MOVE")
public class Move extends JpaModel {

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "move", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<MoveLine> moveLines;
	
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Invoice invoice;
	
	public List<MoveLine> getMoveLines() {
		return moveLines;
	}
	
	public void setMoveLines(List<MoveLine> moveLines) {
		this.moveLines = moveLines;
	}
	
	public Invoice getInvoice() {
		return invoice;
	}
	
	public void setInvoice(Invoice invoice) {
		this.invoice = invoice;
	}
	
	public Move persist() {
		return JPA.persist(this);
	}
	
	public Move merge() {
		return JPA.merge(this);
	}
	
	public Move save() {
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
	
	public static Move find(Long id) {
		return JPA.find(Move.class, id);
	}
	
	public static Query<Move> all() {
		return JPA.all(Move.class);
	}
}
