package com.axelor.apps.account.service;

import java.math.BigDecimal;
import java.util.List;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public interface ReconcileService {
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Reconcile createReconcile(MoveLine debitMoveLine, MoveLine creditMoveLine, BigDecimal amount, boolean canBeZeroBalanceOk);
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public int confirmReconcile(Reconcile reconcile) throws AxelorException;
	
	public void reconcilePreconditions(Reconcile reconcile) throws AxelorException;
	
	public void updatePartnerAccountingSituation(Reconcile reconcile);
	
	public List<Partner> getPartners(Reconcile reconcile);
	
	public void updateInvoiceRemainingAmount(Reconcile reconcile) throws AxelorException;
	
	public Reconcile reconcile(MoveLine debitMoveLine, MoveLine creditMoveLine, boolean canBeZeroBalanceOk) throws AxelorException;
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void unreconcile(Reconcile reconcile) throws AxelorException ;
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void canBeZeroBalance(Reconcile reconcile) throws AxelorException;
	
	public void balanceCredit(MoveLine creditMoveLine) throws AxelorException;
	
	public List<Reconcile> getReconciles(MoveLine moveLine);

}
