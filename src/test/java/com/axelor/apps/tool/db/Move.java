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
