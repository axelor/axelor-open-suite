package com.axelor.apps.tool.db;

import java.math.BigDecimal;

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
@Table(name = "TEST_MOVE_LINE")
public class MoveLine extends JpaModel {

	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Move move;
	
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Invoice invoiceReject;
	
	private LocalDate date;

	private LocalDate dueDate;

	private BigDecimal credit;
	
	private BigDecimal debit;
	
	public Move getMove() {
		return move;
	}
	
	public void setMove(Move move) {
		this.move = move;
	}
	
	public Invoice getInvoiceReject() {
		return invoiceReject;
	}
	
	public void setInvoiceReject(Invoice invoiceReject) {
		this.invoiceReject = invoiceReject;
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
	
	public BigDecimal getCredit() {
		if (credit == null) return BigDecimal.ZERO;
		return credit;
	}
	
	public void setCredit(BigDecimal credit) {
		this.credit = credit;
	}
	
	public BigDecimal getDebit() {
		if (debit == null) return BigDecimal.ZERO;
		return debit;
	}
	
	public void setDebit(BigDecimal debit) {
		this.debit = debit;
	}
	
	public MoveLine persist() {
		return JPA.persist(this);
	}
	
	public MoveLine merge() {
		return JPA.merge(this);
	}
	
	public MoveLine save() {
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
	
	public static MoveLine find(Long id) {
		return JPA.find(MoveLine.class, id);
	}
	
	public static Query<MoveLine> all() {
		return JPA.all(MoveLine.class);
	}
}
