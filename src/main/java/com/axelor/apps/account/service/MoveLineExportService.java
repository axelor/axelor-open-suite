/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.JournalType;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveLineReport;
import com.axelor.apps.account.service.administration.GeneralServiceAccount;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.tool.file.CsvTool;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MoveLineExportService {
	
	private static final Logger LOG = LoggerFactory.getLogger(MoveLineExportService.class);
	
	private DateTime todayTime;
	
	@Inject
	private MoveLineReportService moveLineReportService;
	
	@Inject
	private SequenceService sequenceService;
	
	@Inject
	private AccountConfigService accountConfigService;
	
	@Inject
	public MoveLineExportService() {
		todayTime = GeneralService.getTodayDateTime();
	}
	
	
	public void updateMoveList(List<Move> moveList, MoveLineReport moveLineReport, LocalDate localDate, String exportToAgressoNumber)  {
		
		int i = 0;
		
		int moveListSize = moveList.size();
		
		for(Move move : moveList)  {
			
			this.updateMove(Move.find(move.getId()), MoveLineReport.find(moveLineReport.getId()), localDate, exportToAgressoNumber);
			
			if (i % 10 == 0) { JPA.clear(); }
			if (i++ % 100 == 0) { LOG.debug("Process : {} / {}" , i, moveListSize); }
		}
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Move updateMove(Move move, MoveLineReport moveLineReport, LocalDate localDate, String exportToAgressoNumber)  {
		
		move.setExportNumber(exportToAgressoNumber);
		move.setExportDate(localDate);
		move.setAccountingOk(true);
		move.setMoveLineReport(moveLineReport);
		move.save();
		
		return move;
	}
	
	
	public BigDecimal getSumDebit(String queryFilter, List<? extends Move> moveList)  {
		
		Query q = JPA.em().createQuery("select SUM(self.debit) FROM MoveLine as self WHERE " + queryFilter, BigDecimal.class);
		q.setParameter(1, moveList);
		
		BigDecimal result = (BigDecimal) q.getSingleResult();
		LOG.debug("Total debit : {}", result);
		
		if(result != null)  {
			return result;
		}
		else  {
			return BigDecimal.ZERO;
		}
		
	}
	
	public BigDecimal getSumCredit(String queryFilter, List<Move> moveList)  {
		
		Query q = JPA.em().createQuery("select SUM(self.credit) FROM MoveLine as self WHERE " + queryFilter, BigDecimal.class);
		q.setParameter(1, moveList);
		
		BigDecimal result = (BigDecimal) q.getSingleResult();
		LOG.debug("Total credit : {}", result);
		
		if(result != null)  {
			return result;
		}
		else  {
			return BigDecimal.ZERO;
		}
		
	}
	
	
	public BigDecimal getSumCredit(List<MoveLine> moveLineList)  {
		
		BigDecimal sumCredit = BigDecimal.ZERO;
		for(MoveLine moveLine : moveLineList) {
			sumCredit = sumCredit.add(moveLine.getCredit());
		}
		
		return sumCredit; 
	}
	
	
	
	public BigDecimal getTotalAmount(List<MoveLine> moveLinelst) {
		
		BigDecimal totDebit = BigDecimal.ZERO;
		BigDecimal totCredit = BigDecimal.ZERO;
		
		for(MoveLine moveLine : moveLinelst) {
			totDebit = totDebit.add(moveLine.getDebit());
			totCredit = totCredit.add(moveLine.getCredit());
		}
		
		return totCredit.subtract(totDebit);
	}
	
	
	public String getSaleExportNumber(Company company) throws AxelorException  {
		
		String exportNumber = sequenceService.getSequenceNumber(IAdministration.SALES_INTERFACE, company);
		if(exportNumber == null)  {  
			throw new AxelorException(String.format("%s :\n Erreur : Veuillez configurer une séquence Interface Vente pour la société %s",
					GeneralServiceAccount.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return exportNumber;
		
	}
	
	
	public String getRefundExportNumber(Company company) throws AxelorException  {
		
		String exportNumber = sequenceService.getSequenceNumber(IAdministration.REFUND_INTERFACE, company);
		if(exportNumber == null)  {  
			throw new AxelorException(String.format("%s :\n Erreur : Veuillez configurer une séquence Interface Avoir pour la société %s",
					GeneralServiceAccount.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
			
		return exportNumber;
		
	}
	
	
	public String getTreasuryExportNumber(Company company) throws AxelorException  {
		
		String exportNumber = sequenceService.getSequenceNumber(IAdministration.TREASURY_INTERFACE, company);
		if(exportNumber == null)  {  
			throw new AxelorException(String.format("%s :\n Erreur : Veuillez configurer une séquence Interface Trésorerie pour la société %s",
					GeneralServiceAccount.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
			
		return exportNumber;
		
	}

	
	public String getPurchaseExportNumber(Company company) throws AxelorException  {
	
		String exportNumber = sequenceService.getSequenceNumber(IAdministration.PURCHASE_INTERFACE, company);
		if(exportNumber == null)  {  
			throw new AxelorException(String.format("%s :\n Erreur : Veuillez configurer une séquence Interface Achat pour la société %s",
					GeneralServiceAccount.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return exportNumber;
		
	}

	
	/**
	 * Méthode réalisant l'export SI - Agresso pour les journaux de type vente
	 * @param mlr
	 * @param replay
	 * @throws AxelorException
	 * @throws IOException 
	 */
	public void exportMoveLineTypeSelect6(MoveLineReport mlr, boolean replay) throws AxelorException, IOException {
		
		LOG.info("In Export type service : ");
		
		String fileName = "detail"+todayTime.toString("YYYYMMddHHmmss")+"ventes.dat";
		this.exportMoveLineTypeSelect6FILE1(mlr, replay);
		this.exportMoveLineAllTypeSelectFILE2(mlr,fileName);
	}
	
	
	/**
	 * Méthode réalisant l'export SI - Agresso des en-têtes pour les journaux de type vente
	 * @param mlr
	 * @param replay
	 * @throws AxelorException
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void exportMoveLineTypeSelect6FILE1(MoveLineReport moveLineReport, boolean replay) throws AxelorException, IOException {
			
		LOG.info("In export service Type 6 FILE 1 :");
		
		Company company = moveLineReport.getCompany();
		
		String dateQueryStr = String.format(" WHERE self.company = %s", company.getId());
		JournalType journalType = moveLineReportService.getJournalType(moveLineReport);
		if(moveLineReport.getJournal()!=null) {
			dateQueryStr += String.format(" AND self.journal = %s", moveLineReport.getJournal().getId());
		}
		else  {
			dateQueryStr += String.format(" AND self.journal.type = %s", journalType.getId());
		}
		if(moveLineReport.getPeriod() != null)	{
			dateQueryStr += String.format(" AND self.period = %s", moveLineReport.getPeriod().getId());
		}
		if(replay)  {
			dateQueryStr += String.format(" AND self.accountingOk = true AND self.moveLineReport = %s", moveLineReport.getId());
		}
		else  {
			dateQueryStr += " AND self.accountingOk = false ";
		}
		dateQueryStr += " AND self.ignoreInAccountingOk = false AND self.journal.notExportOk = false ";
		dateQueryStr += String.format(" AND self.statusSelect = %s ", Move.STATUS_VALIDATED);
		Query dateQuery = JPA.em().createQuery("SELECT self.date from Move self" + dateQueryStr + "group by self.date order by self.date");

		List<LocalDate> allDates = new ArrayList<LocalDate>();
		allDates = dateQuery.getResultList();
		
		LOG.debug("allDates : {}" , allDates);
		
		List<String[]> allMoveData = new ArrayList<String[]>();
		String companyCode = "";
		
		String reference = "";
		String moveQueryStr = "";
		String moveLineQueryStr = "";
		if(moveLineReport.getRef()!=null) {
			reference = moveLineReport.getRef();
		}
		if(company != null) {
			companyCode = company.getCode();
			moveQueryStr += String.format(" AND self.company = %s", company.getId()); 
		}
		if(moveLineReport.getPeriod() != null)	{
			moveQueryStr += String.format(" AND self.period = %s", moveLineReport.getPeriod().getId());  
		}
		if(moveLineReport.getDateFrom() != null)  {
			moveLineQueryStr += String.format(" AND self.date >= '%s'", moveLineReport.getDateFrom().toString());  
		}
		if(moveLineReport.getDateTo() != null)  {
			moveLineQueryStr += String.format(" AND self.date <= '%s'", moveLineReport.getDateTo().toString());  
		}
		if(moveLineReport.getDate() != null)  {
			moveLineQueryStr += String.format(" AND self.date <= '%s'", moveLineReport.getDate().toString());  
		}
		if(replay)  {
			moveQueryStr += String.format(" AND self.accountingOk = true AND self.moveLineReport = %s", moveLineReport.getId());  
		}
		else  {
			moveQueryStr += " AND self.accountingOk = false ";
		}
		moveQueryStr += String.format(" AND self.statusSelect = %s ", Move.STATUS_VALIDATED);
		
		LocalDate interfaceDate = moveLineReport.getDate();
		
		for(LocalDate dt : allDates) {				
			
			List<Journal> journalList = (List<Journal>) Journal.filter("self.type = ?1 AND self.notExportOk = false", journalType).fetch();
			
			if(moveLineReport.getJournal() != null)  {
				journalList = new ArrayList<Journal>();
				journalList.add(moveLineReport.getJournal());
			}
			
			for(Journal journal : journalList)  {
			
				List<? extends Move> moveList = Move.filter("self.date = ?1 AND self.ignoreInAccountingOk = false AND self.journal.notExportOk = false AND self.journal = ?2" + moveQueryStr, dt, journal).fetch();
				
				String journalCode = journal.getExportCode();
				
				if (moveList.size() > 0) {
						
					BigDecimal sumDebit = this.getSumDebit("self.account.reconcileOk = true AND self.debit != 0.00 AND self.move in ?1 "+ moveLineQueryStr, moveList);
					
					if(sumDebit.compareTo(BigDecimal.ZERO) == 1)  {
						
						String exportNumber = this.getSaleExportNumber(company);
						
						Move firstMove = moveList.get(0);
						String periodCode = firstMove.getPeriod().getFromDate().toString("yyyyMM");
						
						this.updateMoveList((List<Move>) moveList, moveLineReport, interfaceDate, exportNumber);
						
						String items[] = new String[8];
						items[0] = companyCode;
						items[1] = journalCode;
						items[2] = exportNumber;
						items[3] = interfaceDate.toString("dd/MM/yyyy");
						items[4] = sumDebit.toString();
						items[5] = reference;
						items[6] = dt.toString("dd/MM/yyyy");
						items[7]= periodCode;
						allMoveData.add(items);
					}
				}
			}
		}
					
		String fileName = "entete"+todayTime.toString("YYYYMMddHHmmss")+"ventes.dat";			
		String filePath = accountConfigService.getExportPath(accountConfigService.getAccountConfig(company));
		new File(filePath).mkdirs();
		
		LOG.debug("Full path to export : {}{}" , filePath, fileName);
		CsvTool.csvWriter(filePath, fileName, '|', null, allMoveData);
		// Utilisé pour le debuggage
//			CsvTool.csvWriter(filePath, fileName, '|', this.createHeaderForHeaderFile(mlr.getTypeSelect()), allMoveData);
	}
	
	
	
	/**
	 * Méthode réalisant l'export SI - Agresso pour les journaux de type avoir
	 * @param mlr
	 * @param replay
	 * @throws AxelorException
	 * @throws IOException 
	 */
	public void exportMoveLineTypeSelect7(MoveLineReport moveLineReport, boolean replay) throws AxelorException, IOException {
		
		LOG.info("In Export type 7 service : ");
		
		String fileName = "detail"+todayTime.toString("YYYYMMddHHmmss")+"avoirs.dat";
		this.exportMoveLineTypeSelect7FILE1(moveLineReport, replay);
		this.exportMoveLineAllTypeSelectFILE2(moveLineReport, fileName);
	}
	
	
	/**
	 * Méthode réalisant l'export SI - Agresso des en-têtes pour les journaux de type avoir
	 * @param mlr
	 * @param replay
	 * @throws AxelorException
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void exportMoveLineTypeSelect7FILE1(MoveLineReport moveLineReport, boolean replay) throws AxelorException, IOException {
			
		LOG.info("In export service 7 FILE 1:");
		
		Company company = moveLineReport.getCompany();
		
		String dateQueryStr = String.format(" WHERE self.company = %s", company.getId());
		JournalType journalType = moveLineReportService.getJournalType(moveLineReport);
		if(moveLineReport.getJournal()!=null) {
			dateQueryStr += String.format(" AND self.journal = %s", moveLineReport.getJournal().getId());
		}
		else  {
			dateQueryStr += String.format(" AND self.journal.type = %s", journalType.getId());
		}
		if(moveLineReport.getPeriod() != null)	{
			dateQueryStr += String.format(" AND self.period = %s", moveLineReport.getPeriod().getId());
		}
		if(replay)  {
			dateQueryStr += String.format(" AND self.accountingOk = true AND self.moveLineReport = %s", moveLineReport.getId());
		}
		else  {
			dateQueryStr += " AND self.accountingOk = false ";
		}
		dateQueryStr += " AND self.ignoreInAccountingOk = false AND self.journal.notExportOk = false ";
		dateQueryStr += String.format(" AND self.statusSelect = %s ", Move.STATUS_VALIDATED);
		Query dateQuery = JPA.em().createQuery("SELECT self.date from Move self" + dateQueryStr + "group by self.date order by self.date");

		List<LocalDate> allDates = new ArrayList<LocalDate>();
		allDates = dateQuery.getResultList();
		
		LOG.debug("allDates : {}" , allDates);
		
		List<String[]> allMoveData = new ArrayList<String[]>();
		String companyCode = "";
		
		String reference = "";
		String moveQueryStr = "";
		String moveLineQueryStr = "";
		if(moveLineReport.getRef()!=null) {
			reference = moveLineReport.getRef();
		}
		if(moveLineReport.getCompany()!=null) {
			companyCode = moveLineReport.getCompany().getCode();
			moveQueryStr += String.format(" AND self.company = %s", moveLineReport.getCompany().getId()); 
		}
		if(moveLineReport.getPeriod() != null)	{
			moveQueryStr += String.format(" AND self.period = %s", moveLineReport.getPeriod().getId());  
		}
		if(moveLineReport.getDateFrom() != null)  {
			moveLineQueryStr += String.format(" AND self.date >= '%s'", moveLineReport.getDateFrom().toString());  
		}
		if(moveLineReport.getDateTo() != null)  {
			moveLineQueryStr += String.format(" AND self.date <= '%s'", moveLineReport.getDateTo().toString());  
		}
		if(moveLineReport.getDate() != null)  {
			moveLineQueryStr += String.format(" AND self.date <= '%s'", moveLineReport.getDate().toString());  
		}
		if(replay)  {
			moveQueryStr += String.format(" AND self.accountingOk = true AND self.moveLineReport = %s", moveLineReport.getId());  
		}
		else  {
			moveQueryStr += " AND self.accountingOk = false ";
		}
		moveQueryStr += String.format(" AND self.statusSelect = %s ", Move.STATUS_VALIDATED);
		
		LocalDate interfaceDate = moveLineReport.getDate();
		
		for(LocalDate dt : allDates) {				
			
			List<Journal> journalList = (List<Journal>) Journal.filter("self.type = ?1 AND self.notExportOk = false", journalType).fetch();
			
			if(moveLineReport.getJournal()!=null)  {
				journalList = new ArrayList<Journal>();
				journalList.add(moveLineReport.getJournal());
			}
			
			for(Journal journal : journalList)  {
			
				List<Move> moveList = (List<Move>) Move.filter("self.date = ?1 AND self.ignoreInAccountingOk = false AND self.journal.notExportOk = false AND self.journal = ?2" + moveQueryStr, dt, journal).fetch();
				
				String journalCode = journal.getExportCode();
				
				if (moveList.size() > 0) {

					BigDecimal sumCredit = this.getSumCredit("self.account.reconcileOk = true AND self.credit != 0.00 AND self.move in ?1 "+moveLineQueryStr, moveList);

					if(sumCredit.compareTo(BigDecimal.ZERO) == 1)  {
						
						String exportNumber = this.getSaleExportNumber(company);
						
						Move firstMove = moveList.get(0);
						String periodCode = firstMove.getPeriod().getFromDate().toString("yyyyMM");
						
						this.updateMoveList(moveList, moveLineReport, interfaceDate, exportNumber);
						
						String items[] = new String[8];
						items[0] = companyCode;
						items[1] = journalCode;
						items[2] = exportNumber;
						items[3] = interfaceDate.toString("dd/MM/yyyy");
						items[4] = sumCredit.toString();
						items[5] = reference;
						items[6] = dt.toString("dd/MM/yyyy");
						items[7]= periodCode;
						allMoveData.add(items);
					}
				}
			}
		}
					
		String fileName = "entete"+todayTime.toString("YYYYMMddHHmmss")+"avoirs.dat";
		String filePath = accountConfigService.getExportPath(accountConfigService.getAccountConfig(company));
		new File(filePath).mkdirs();
		
		LOG.debug("Full path to export : {}{}" , filePath, fileName);
		CsvTool.csvWriter(filePath, fileName, '|', null, allMoveData);
		// Utilisé pour le debuggage
//			CsvTool.csvWriter(filePath, fileName, '|', this.createHeaderForHeaderFile(mlr.getTypeSelect()), allMoveData);
	}
	
	
	/**
	 * Méthode réalisant l'export SI - Agresso pour les journaux de type trésorerie
	 * @param mlr
	 * @param replay
	 * @throws AxelorException
	 * @throws IOException 
	 */
	public void exportMoveLineTypeSelect8(MoveLineReport moveLineReport, boolean replay) throws AxelorException, IOException {
		
		LOG.info("In Export type 8 service : ");
		
		String fileName = "detail"+todayTime.toString("YYYYMMddHHmmss")+"tresorerie.dat";
		this.exportMoveLineTypeSelect8FILE1(moveLineReport, replay);
		this.exportMoveLineAllTypeSelectFILE2(moveLineReport, fileName);
	}
	
	
	/**
	 * Méthode réalisant l'export SI - Agresso des en-têtes pour les journaux de type trésorerie
	 * @param mlr
	 * @param replay
	 * @throws AxelorException
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void exportMoveLineTypeSelect8FILE1(MoveLineReport moveLineReport, boolean replay) throws AxelorException, IOException {
			
		LOG.info("In export service 8 FILE 1:");
		
		Company company = moveLineReport.getCompany();
		
		String dateQueryStr = String.format(" WHERE self.company = %s", company.getId());
		JournalType journalType = moveLineReportService.getJournalType(moveLineReport);
		if(moveLineReport.getJournal()!=null) {
			dateQueryStr += String.format(" AND self.journal = %s", moveLineReport.getJournal().getId());
		}
		else  {
			dateQueryStr += String.format(" AND self.journal.type = %s", journalType.getId());
		}
		if(moveLineReport.getPeriod() != null)	{
			dateQueryStr += String.format(" AND self.period = %s", moveLineReport.getPeriod().getId());
		}
		if(replay)  {
			dateQueryStr += String.format(" AND self.accountingOk = true AND self.moveLineReport = %s", moveLineReport.getId());
		}
		else  {
			dateQueryStr += " AND self.accountingOk = false ";
		}
		dateQueryStr += " AND self.ignoreInAccountingOk = false AND self.journal.notExportOk = false ";
		dateQueryStr += String.format(" AND self.statusSelect = %s ", Move.STATUS_VALIDATED);
		Query dateQuery = JPA.em().createQuery("SELECT self.date from Move self" + dateQueryStr + "group by self.date order by self.date");

		List<LocalDate> allDates = new ArrayList<LocalDate>();
		allDates = dateQuery.getResultList();
		
		LOG.debug("allDates : {}" , allDates);
		
		List<String[]> allMoveData = new ArrayList<String[]>();
		String companyCode = "";
		
		String reference = "";
		String moveQueryStr = "";
		String moveLineQueryStr = "";
		if(moveLineReport.getRef()!=null) {
			reference = moveLineReport.getRef();
		}
		if(company != null) {
			companyCode = moveLineReport.getCompany().getCode();
			moveQueryStr += String.format(" AND self.company = %s", company.getId()); 
		}
		if(moveLineReport.getPeriod() != null)	{
			moveQueryStr += String.format(" AND self.period = %s", moveLineReport.getPeriod().getId());  
		}
		if(moveLineReport.getDateFrom() != null)  {
			moveLineQueryStr += String.format(" AND self.date >= '%s'", moveLineReport.getDateFrom().toString());  
		}
		if(moveLineReport.getDateTo() != null)  {
			moveLineQueryStr += String.format(" AND self.date <= '%s'", moveLineReport.getDateTo().toString());  
		}
		if(moveLineReport.getDate() != null)  {
			moveLineQueryStr += String.format(" AND self.date <= '%s'", moveLineReport.getDate().toString());  
		}
		if(replay)  {
			moveQueryStr += String.format(" AND self.accountingOk = true AND self.moveLineReport = %s", moveLineReport.getId());  
		}
		else  {
			moveQueryStr += " AND self.accountingOk = false ";
		}
		moveQueryStr += String.format(" AND self.statusSelect = %s ", Move.STATUS_VALIDATED);
		
		LocalDate interfaceDate = moveLineReport.getDate();
		
		for(LocalDate dt : allDates) {				
			
			List<Journal> journalList = (List<Journal>) Journal.filter("self.type = ?1 AND self.notExportOk = false", journalType).fetch();
			
			if(moveLineReport.getJournal()!=null)  {
				journalList = new ArrayList<Journal>();
				journalList.add(moveLineReport.getJournal());
			}
			
			for(Journal journal : journalList)  {
			
				List<Move> moveList = (List<Move>) Move.filter("self.date = ?1 AND self.ignoreInAccountingOk = false AND self.journal.notExportOk = false AND self.journal = ?2" + moveQueryStr, dt, journal).fetch();
				
				String journalCode = journal.getExportCode();
				
				if (moveList.size() > 0) {
						
					long moveLineListSize = MoveLine.filter("self.move in ?1 AND (self.debit > 0 OR self.credit > 0) " + moveLineQueryStr, moveList).count();
					
					if(moveLineListSize > 0) {
						
						String exportNumber = this.getTreasuryExportNumber(company);
						
						Move firstMove = moveList.get(0);
						String periodCode = firstMove.getPeriod().getFromDate().toString("yyyyMM");
						
						this.updateMoveList(moveList, moveLineReport, interfaceDate, exportNumber);
						
						String items[] = new String[8];
						items[0] = companyCode;
						items[1] = journalCode;
						items[2] = exportNumber;
						items[3] = interfaceDate.toString("dd/MM/yyyy");
						items[4] = "0";
						items[5] = reference;
						items[6] = dt.toString("dd/MM/yyyy");
						items[7]= periodCode;
						allMoveData.add(items);
					}
				}
			}
		}
					
		String fileName = "entete"+todayTime.toString("YYYYMMddHHmmss")+"tresorerie.dat";
		String filePath = accountConfigService.getExportPath(accountConfigService.getAccountConfig(company));
		new File(filePath).mkdirs();
		
		LOG.debug("Full path to export : {}{}" , filePath, fileName);
		CsvTool.csvWriter(filePath, fileName, '|', null, allMoveData);
		// Utilisé pour le debuggage
//			CsvTool.csvWriter(filePath, fileName, '|', this.createHeaderForHeaderFile(mlr.getTypeSelect()), allMoveData);
	}
	
	
	/**
	 * Méthode réalisant l'export SI - Agresso pour les journaux de type achat
	 * @param mlr
	 * @param replay
	 * @throws AxelorException
	 * @throws IOException 
	 */
	public void exportMoveLineTypeSelect9(MoveLineReport moveLineReport, boolean replay) throws AxelorException, IOException {
		
		LOG.info("In Export type 9 service : ");
		String fileName = "detail"+todayTime.toString("YYYYMMddHHmmss")+"achats.dat";
		this.exportMoveLineTypeSelect9FILE1(moveLineReport, replay);
		this.exportMoveLineAllTypeSelectFILE2(moveLineReport, fileName);
	}
	
	
	/**
	 * Méthode réalisant l'export SI - Agresso des en-têtes pour les journaux de type achat
	 * @param mlr
	 * @param replay
	 * @throws AxelorException
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void exportMoveLineTypeSelect9FILE1(MoveLineReport moveLineReport, boolean replay) throws AxelorException, IOException {
			
		LOG.info("In export service 9 FILE 1:");
		
		Company company = moveLineReport.getCompany();
		String dateQueryStr = String.format(" WHERE self.company = %s", company.getId());
		JournalType journalType = moveLineReportService.getJournalType(moveLineReport);
		if(moveLineReport.getJournal() != null) {
			dateQueryStr += String.format(" AND self.journal = %s", moveLineReport.getJournal().getId());
		}
		else  {
			dateQueryStr += String.format(" AND self.journal.type = %s", journalType.getId());
		}
		if(moveLineReport.getPeriod() != null)	{
			dateQueryStr += String.format(" AND self.period = %s", moveLineReport.getPeriod().getId());
		}
		if(replay)  {
			dateQueryStr += String.format(" AND self.accountingOk = true AND self.moveLineReport = %s", moveLineReport.getId());
		}
		else  {
			dateQueryStr += " AND self.accountingOk = false ";
		}
		dateQueryStr += " AND self.ignoreInAccountingOk = false AND self.journal.notExportOk = false ";
		dateQueryStr += String.format(" AND self.statusSelect = %s ", Move.STATUS_VALIDATED);
		Query dateQuery = JPA.em().createQuery("SELECT self.date from Move self" + dateQueryStr + "group by self.date order by self.date");

		List<LocalDate> allDates = new ArrayList<LocalDate>();
		allDates = dateQuery.getResultList();
		
		LOG.debug("allDates : {}" , allDates);
		
		List<String[]> allMoveData = new ArrayList<String[]>();
		String companyCode = "";
		
		String reference = "";
		String moveQueryStr = "";
		String moveLineQueryStr = "";
		if(moveLineReport.getRef()!=null) {
			reference = moveLineReport.getRef();
		}
		if(company != null) {
			companyCode = company.getCode();
			moveQueryStr += String.format(" AND self.company = %s", company.getId()); 
		}
		if(moveLineReport.getPeriod() != null)	{
			moveQueryStr += String.format(" AND self.period = %s", moveLineReport.getPeriod().getId());  
		}
		if(moveLineReport.getDateFrom() != null)  {
			moveLineQueryStr += String.format(" AND self.date >= '%s'", moveLineReport.getDateFrom().toString());  
		}
		if(moveLineReport.getDateTo() != null)  {
			moveLineQueryStr += String.format(" AND self.date <= '%s'", moveLineReport.getDateTo().toString());  
		}
		if(moveLineReport.getDate() != null)  {
			moveLineQueryStr += String.format(" AND self.date <= '%s'", moveLineReport.getDate().toString());  
		}
		if(replay)  {
			moveQueryStr += String.format(" AND self.accountingOk = true AND self.moveLineReport = %s", moveLineReport.getId());  
		}
		else  {
			moveQueryStr += " AND self.accountingOk = false ";
		}
		moveQueryStr += String.format(" AND self.statusSelect = %s ", Move.STATUS_VALIDATED);
		
		LocalDate interfaceDate = moveLineReport.getDate();
		
		for(LocalDate dt : allDates) {				
			
			List<Journal> journalList = (List<Journal>) Journal.filter("self.type = ?1 AND self.notExportOk = false", journalType).fetch();
			
			if(moveLineReport.getJournal()!=null)  {
				journalList = new ArrayList<Journal>();
				journalList.add(moveLineReport.getJournal());
			}
			
			for(Journal journal : journalList)  {
			
				List<Move> moveList = (List<Move>) Move.filter("self.date = ?1 AND self.ignoreInAccountingOk = false AND self.journal.notExportOk = false AND self.journal = ?2" + moveQueryStr, dt, journal).fetch();
				
				String journalCode = journal.getExportCode();
				
				int moveListSize = moveList.size();
				
				if (moveListSize > 0) {
					
					int i = 0;
					
					for(Move move : moveList)  {
						
						List<MoveLine> moveLineList = (List<MoveLine>) MoveLine.filter("self.account.reconcileOk = true AND self.credit != 0.00 AND self.move in ?1" + moveLineQueryStr, moveList).fetch();
						
						if(moveLineList.size() > 0) {
							
							String exportNumber = this.getPurchaseExportNumber(company);
							
							String periodCode = move.getPeriod().getFromDate().toString("yyyyMM");
							
							BigDecimal totalCredit = this.getSumCredit(moveLineList);
							String invoiceId = "";
							String dueDate = "";
							if(move.getInvoice() != null)  {
								invoiceId = move.getInvoice().getInvoiceId();
								dueDate = move.getInvoice().getDueDate().toString();
							}
							
							MoveLine firstMoveLine = moveLineList.get(0);
							String items[] = new String[11];
							items[0] = companyCode;
							items[1] = journalCode;
							items[2] = exportNumber;
							items[3] = interfaceDate.toString("dd/MM/yyyy");
							items[4] = invoiceId;
							items[5] = dueDate;
							items[6]= firstMoveLine.getAccount().getCode();
							items[7]= totalCredit.toString();
							items[8]= reference;
							items[9]= dt.toString("dd/MM/yyyy");
							items[10]= periodCode;
							allMoveData.add(items);
							
							this.updateMove(move, moveLineReport, interfaceDate, exportNumber);
							
							if (i % 10 == 0) { JPA.clear(); }
							if (i++ % 100 == 0) { LOG.debug("Process : {} / {}" , i, moveListSize); }
						}
					}
				}
			}
		}
					
		String fileName = "entete"+todayTime.toString("YYYYMMddHHmmss")+"achats.dat";
		String filePath = accountConfigService.getExportPath(accountConfigService.getAccountConfig(company));
		new File(filePath).mkdirs();
		
		LOG.debug("Full path to export : {}{}" , filePath, fileName);
		CsvTool.csvWriter(filePath, fileName, '|', null, allMoveData);
		// Utilisé pour le debuggage
//			CsvTool.csvWriter(filePath, fileName, '|', this.createHeaderForHeaderFile(mlr.getTypeSelect()), allMoveData);
	}
	
	
	/**
	 * Méthode réalisant l'export SI - Agresso des fichiers détails
	 * @param mlr
	 * @param fileName
	 * @throws AxelorException
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	public void exportMoveLineAllTypeSelectFILE2(MoveLineReport moveLineReport, String fileName) throws AxelorException, IOException {
			
		LOG.info("In export service FILE 2 :");
		
		Company company = moveLineReport.getCompany();
		
		String companyCode = "";
		String moveLineQueryStr = "";
		
		int typeSelect = moveLineReport.getTypeSelect();
		
		if(company != null) {
			companyCode = company.getCode();
			moveLineQueryStr += String.format(" AND self.move.company = %s", company.getId());  
		}
		if(moveLineReport.getJournal() != null)	{
			moveLineQueryStr += String.format(" AND self.move.journal = %s", moveLineReport.getJournal().getId());  
		}
		else  {
			moveLineQueryStr += String.format(" AND self.move.journal.type = %s", moveLineReportService.getJournalType(moveLineReport).getId());  
		}
		
		if(moveLineReport.getPeriod() != null)	{
			moveLineQueryStr += String.format(" AND self.move.period = %s", moveLineReport.getPeriod().getId());  
		}
		if(moveLineReport.getDateFrom() != null)  {
			moveLineQueryStr += String.format(" AND self.date >= '%s'", moveLineReport.getDateFrom().toString());  
		}
		
		if(moveLineReport.getDateTo() != null)  {
			moveLineQueryStr += String.format(" AND self.date <= '%s'", moveLineReport.getDateTo().toString());  
		}
		if(moveLineReport.getDate() != null)  {
			moveLineQueryStr += String.format(" AND self.date <= '%s'", moveLineReport.getDate().toString());  
		}
		if(typeSelect != 8 )  {
			moveLineQueryStr += String.format(" AND self.account.reconcileOk = false ");  
		}
		moveLineQueryStr += String.format("AND self.move.accountingOk = true AND self.move.ignoreInAccountingOk = false AND self.move.moveLineReport = %s", moveLineReport.getId());  
		moveLineQueryStr += String.format(" AND self.move.statusSelect = %s ", Move.STATUS_VALIDATED);
		
		Query queryDate = JPA.em().createQuery("SELECT self.date from MoveLine self where self.account != null AND (self.debit > 0 OR self.credit > 0) " + moveLineQueryStr + " group by self.date ORDER BY self.date");
		
		List<LocalDate> dates = new ArrayList<LocalDate>();
		dates = queryDate.getResultList();
		
		LOG.debug("dates : {}" , dates);
		
		List<String[]> allMoveLineData = new ArrayList<String[]>();
		
		for (LocalDate localDate : dates)  {
			
			Query queryExportAgressoRef = JPA.em().createQuery("SELECT DISTINCT self.move.exportNumber from MoveLine self where self.account != null " +
					"AND (self.debit > 0 OR self.credit > 0) AND self.date = '"+ localDate.toString() +"'"+ moveLineQueryStr);
			List<String> exportAgressoRefs = new ArrayList<String>();
			exportAgressoRefs = queryExportAgressoRef.getResultList();
			for(String exportAgressoRef : exportAgressoRefs)  {
				
				if(exportAgressoRef != null && !exportAgressoRef.isEmpty())  {
					
					int sequence = 1;
					
					Query query = JPA.em().createQuery("SELECT self.account.id from MoveLine self where self.account != null AND (self.debit > 0 OR self.credit > 0) " +
							"AND self.date = '"+ localDate.toString() +"' AND self.move.exportNumber = '"+ exportAgressoRef + "'" + moveLineQueryStr +
							" group by self.account.id");
					
					List<Long> accountIds = new ArrayList<Long>();
					accountIds = query.getResultList();
					
					LOG.debug("accountIds : {}" , accountIds);
					
					for (Long accountId : accountIds) {
						if(accountId!=null) {
							String accountCode = Account.find(accountId).getCode();
							List<MoveLine> moveLines = (List<MoveLine>) MoveLine.filter("self.account.id = ?1 AND (self.debit > 0 OR self.credit > 0) AND self.date = '"+ 
							localDate.toString() +"' AND self.move.exportNumber = '"+ exportAgressoRef +"'" + moveLineQueryStr, accountId).fetch();
							
							LOG.debug("movelines  : {} " , moveLines);

							if(moveLines.size() > 0) {
								
								List<MoveLine> moveLineList = this.consolidateMoveLineByAnalyticAxis(moveLines);

								List<MoveLine> sortMoveLineList = this.sortMoveLineByDebitCredit(moveLineList);
								
								for(MoveLine moveLine3 : sortMoveLineList)   {
												
									Journal journal = moveLine3.getMove().getJournal();
									LocalDate date = moveLine3.getDate();
									String items[] = null;
	
									if(typeSelect == 9)  {
										items = new String[13];
									}
									else {
										items = new String[12];
									}
									
									items[0] = companyCode;
									items[1] = journal.getExportCode();
									items[2] = moveLine3.getMove().getExportNumber();
									items[3] = String.format("%s", sequence);
									sequence++;		
									items[4] = accountCode;
									
									BigDecimal totAmt = moveLine3.getCredit().subtract(moveLine3.getDebit());
									String moveLineSign = "C";
									if(totAmt.compareTo(BigDecimal.ZERO) == -1) {
										moveLineSign="D";
										totAmt = totAmt.negate();
									}
									items[5] = moveLineSign;
									items[6] = totAmt.toString();
									
									
									String activeStr = "";
									String crbStr = "";
									String metiertr = "";
									String siteStr = "";
									
									for(AnalyticAccount analyticAccount : moveLine3.getAnalyticAccountSet())  {
										
										if(analyticAccount.getAnalyticAxis() != null && analyticAccount.getAnalyticAxis().getCode().equals("ACTIVITE"))  {
											activeStr = analyticAccount.getCode();
										}
										if(analyticAccount.getAnalyticAxis() != null && analyticAccount.getAnalyticAxis().getCode().equals("CRB"))  {
											crbStr = analyticAccount.getCode();
										}
										if(analyticAccount.getAnalyticAxis() != null && analyticAccount.getAnalyticAxis().getCode().equals("METIER"))  {
											metiertr = analyticAccount.getCode();
										}
										if(analyticAccount.getAnalyticAxis() != null && analyticAccount.getAnalyticAxis().getCode().equals("SITE"))  {
											siteStr = analyticAccount.getCode();
										}
									}
									
									if(typeSelect == 9)  {
										items[7]= "";
										items[8]= crbStr;
										items[9]= siteStr;
										items[10]= metiertr;
										items[11]= activeStr;
										items[12]= String.format("%s DU %s", journal.getCode(), date.toString("dd/MM/yyyy"));
									}
									else  {
										items[7]= crbStr;
										items[8]= siteStr;
										items[9]= metiertr;
										items[10]= activeStr;
										items[11]= String.format("%s DU %s", journal.getCode(), date.toString("dd/MM/yyyy"));
									}
								
									allMoveLineData.add(items);
									
								}
							}
						}
					}
				}
			}
		}
		
		String filePath = accountConfigService.getExportPath(accountConfigService.getAccountConfig(company));
		new File(filePath).mkdirs();
		
		LOG.debug("Full path to export : {}{}" , filePath, fileName);
		CsvTool.csvWriter(filePath, fileName, '|',  null, allMoveLineData);
		// Utilisé pour le debuggage
//			CsvTool.csvWriter(filePath, fileName, '|',  this.createHeaderForDetailFile(typeSelect), allMoveLineData);  
	}
	
	
	/**
	 * Methode permettant de consolider une liste de ligne d'écriture par axe analytique
	 * @param moveLineList
	 * @return
	 */
	public List<MoveLine> consolidateMoveLineByAnalyticAxis(List<MoveLine> moveLineList)  {
		List<MoveLine> sortMoveLineList = new ArrayList<MoveLine>();

		for(MoveLine moveLine : moveLineList) {
			
			boolean found = false;
			for(MoveLine moveLine2 : sortMoveLineList)  {
				if(moveLine.getAnalyticAccountSet().equals(moveLine2.getAnalyticAccountSet()) 
						&& moveLine.getMove().getJournal().equals(moveLine2.getMove().getJournal())
						&& moveLine.getMove().getExportNumber().equals(moveLine2.getMove().getExportNumber()))  {
					moveLine2.setDebit(moveLine2.getDebit().add(moveLine.getDebit()));
					moveLine2.setCredit(moveLine2.getCredit().add(moveLine.getCredit()));
					found = true;
				}
			}
			if(!found)  {
				sortMoveLineList.add(moveLine);
			}
		}
		return sortMoveLineList;
	}
	
	
	/**
	 * Méthode permettant de trier une liste en ajoutant d'abord les lignes d'écriture au débit puis celles au crédit
	 * @param moveLineList
	 * 			Une list de ligne d'écriture non triée
	 * @return
	 */
	public List<MoveLine> sortMoveLineByDebitCredit(List<MoveLine> moveLineList)  {
		List<MoveLine> sortMoveLineList = new ArrayList<MoveLine>();
		List<MoveLine> debitMoveLineList = new ArrayList<MoveLine>();
		List<MoveLine> creditMoveLineList = new ArrayList<MoveLine>();
		for(MoveLine moveLine : moveLineList)  {
			if(moveLine.getDebit().compareTo(moveLine.getCredit()) == 1)  {
				debitMoveLineList.add(moveLine);
			}
			else  {
				creditMoveLineList.add(moveLine);
			}
		}
		sortMoveLineList.addAll(debitMoveLineList);
		sortMoveLineList.addAll(creditMoveLineList);
		return sortMoveLineList;
	}
	
	
	public String[] createHeaderForHeaderFile(int typeSelect)  {
		String header = null;
		switch(typeSelect)  {
			case 6:
				header = "Société;"+
						"Journal de Vente;"+
						"Numéro d'écriture;"+
						"Date de l'interface;"+
						"Montant de l'écriture;"+
						"Réf. de l'écriture;"+
						"Date de l'écriture;"+
						"Période de l'écriture;";
				return header.split(";");
			case 7:
				header = "Société;"+
						"Journal d'Avoir;"+
						"Numéro d'écriture;"+
						"Date de l'interface;"+
						"Montant de l'écriture;"+
						"Réf. de l'écriture;"+
						"Date de l'écriture;"+
						"Période de l'écriture;";
				return header.split(";");
			case 8:
				header = "Société;"+
						"Journal de Trésorerie;"+
						"Numéro d'écriture;"+
						"Date de l'interface;"+
						"Montant de l'écriture;"+
						"Réf. de l'écriture;"+
						"Date de l'écriture;"+
						"Période de l'écriture;";
				return header.split(";");
			case 9:
				header = "Société;"+
						"Journal d'Achat;"+
						"Numéro d'écriture;"+
						"Date de l'interface;"+
						"Code fournisseur;"+
						"Date de la facture;"+
						"Date d'exigibilité;"+
						"Numéro de compte de contrepartie;"+
						"Montant de l'écriture;"+
						"Réf. de l'écriture;"+
						"Date de l'écriture;"+
						"Période de l'écriture;";
				return header.split(";");
			default:
				return null;
		}
	}
	
	
	public String[] createHeaderForDetailFile(int typeSelect)  {
		String header = "";
		
		if(typeSelect == 9)  {
			header = "Société;"+
					"Journal;"+
					"Numéro d'écriture;"+
					"Num. ligne d'écriture;"+
					"Numéro de compte;"+
					"Sens de l'écriture;"+
					"Montant de la ligne;"+
					"Code TVA;"+
					"CRB;"+
					"Site;"+
					"Métier;"+
					"Activité;"+
					"Nom;";
		}
		else  {
			header = "Société;"+
					"Journal;"+
					"Numéro d'écriture;"+
					"Num. ligne d'écriture;"+
					"Numéro de compte;"+
					"Sens de l'écriture;"+
					"Montant de la ligne;"+
					"CRB;"+
					"Site;"+
					"Métier;"+
					"Activité;"+
					"Nom;";
		}
		
		return header.split(";");
		
	}

	
	public void exportMoveLine(MoveLineReport moveLineReport) throws AxelorException, IOException  {
		
		moveLineReportService.setStatus(moveLineReport);
		
		switch (moveLineReport.getTypeSelect()) {
		case MoveLineReport.EXPORT_SALES:
			
			this.exportMoveLineTypeSelect6(moveLineReport, false);
			break;
			
		case MoveLineReport.EXPORT_REFUNDS:
			
			this.exportMoveLineTypeSelect7(moveLineReport, false);
			break;
			
		case MoveLineReport.EXPORT_TREASURY:
			
			this.exportMoveLineTypeSelect8(moveLineReport, false);
			break;
			
		case MoveLineReport.EXPORT_PURCHASES:
			
			this.exportMoveLineTypeSelect9(moveLineReport, false);
			break;

		default:
			break;
		}
			
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public MoveLineReport createMoveLineReport(Company company, int exportTypeSelect, LocalDate startDate, LocalDate endDate) throws AxelorException  {
		
		MoveLineReport moveLineReport = new MoveLineReport();
		moveLineReport.setCompany(company);
		moveLineReport.setTypeSelect(exportTypeSelect);
		moveLineReport.setDateFrom(startDate);
		moveLineReport.setDateTo(endDate);
		moveLineReport.setStatusSelect(MoveLineReport.STATUS_DRAFT);
		moveLineReport.setDate(todayTime.toLocalDate());
		moveLineReport.setRef(moveLineReportService.getSequence(moveLineReport));
		
		String queryFilter = moveLineReportService.getMoveLineList(moveLineReport);
		BigDecimal debitBalance = moveLineReportService.getDebitBalance(queryFilter);
		BigDecimal creditBalance = moveLineReportService.getCreditBalance(queryFilter);
		
		moveLineReport.setTotalDebit(debitBalance);
		moveLineReport.setTotalCredit(creditBalance);
		moveLineReport.setBalance(debitBalance.subtract(creditBalance));
		
		moveLineReport.save();
		
		return moveLineReport;
		
	}
	
	
	
}
