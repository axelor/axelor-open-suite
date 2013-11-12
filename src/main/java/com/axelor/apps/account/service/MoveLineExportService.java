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
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Status;
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
	private MoveLineReportService mlrs;
	
	@Inject
	private SequenceService sgs;
	
	@Inject
	public MoveLineExportService() {
		todayTime = GeneralService.getTodayDateTime();
	}
	
	public String getFilePath(Company company) throws AxelorException{
		if(company.getExportPath()!=null) {
			return company.getExportPath();
		}
		else  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Chemin Fichier Exporté (si -> AGRESSO) pour la société %s",
					GeneralService.getExceptionAccountingMsg(),company.getName()), IException.CONFIGURATION_ERROR);
		}
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
	
	
	public BigDecimal getSumDebit(String queryFilter, List<Move> moveList)  {
		
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
		
		String exportNumber = sgs.getSequence(IAdministration.SALES_INTERFACE, company, false);
		if(exportNumber == null)  {  
			throw new AxelorException(String.format("%s :\n Erreur : Veuillez configurer une séquence Interface Vente pour la société %s",
					GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return exportNumber;
		
	}
	
	
	public String getRefundExportNumber(Company company) throws AxelorException  {
		
		String exportNumber = sgs.getSequence(IAdministration.REFUND_INTERFACE, company, false);
		if(exportNumber == null)  {  
			throw new AxelorException(String.format("%s :\n Erreur : Veuillez configurer une séquence Interface Avoir pour la société %s",
					GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
			
		return exportNumber;
		
	}
	
	
	public String getTreasuryExportNumber(Company company) throws AxelorException  {
		
		String exportNumber = sgs.getSequence(IAdministration.TREASURY_INTERFACE, company, false);
		if(exportNumber == null)  {  
			throw new AxelorException(String.format("%s :\n Erreur : Veuillez configurer une séquence Interface Trésorerie pour la société %s",
					GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
			
		return exportNumber;
		
	}

	
	public String getPurchaseExportNumber(Company company) throws AxelorException  {
	
		String exportNumber = sgs.getSequence(IAdministration.PURCHASE_INTERFACE, company, false);
		if(exportNumber == null)  {  
			throw new AxelorException(String.format("%s :\n Erreur : Veuillez configurer une séquence Interface Achat pour la société %s",
					GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
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
		
		String dateQueryStr = " WHERE " + String.format("company = %s", company.getId());
		JournalType journalType = mlrs.getJournalType(moveLineReport);
		if(moveLineReport.getJournal()!=null) {
			dateQueryStr += " AND " + String.format("journal = %s", moveLineReport.getJournal().getId());
		}
		else  {
			dateQueryStr += " AND " + String.format("journal.type = %s", journalType.getId());
		}
		if(moveLineReport.getPeriod() != null)	{
			dateQueryStr += " AND " + String.format("period = %s", moveLineReport.getPeriod().getId());
		}
		if(replay)  {
			dateQueryStr += " AND accountingOk = true AND " + String.format("moveLineReport = %s", moveLineReport.getId());
		}
		else  {
			dateQueryStr += " AND accountingOk = false ";
		}
		dateQueryStr += " AND ignoreInAccountingOk = false AND journal.notExportOk = false  ";
		Query dateQuery = JPA.em().createQuery("SELECT mv.date from Move mv" + dateQueryStr + "group by mv.date order by mv.date");

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
			moveQueryStr += " AND ";
			moveQueryStr += String.format("self.company = %s", company.getId()); 
		}
		if(moveLineReport.getPeriod() != null)	{
			moveQueryStr += " AND ";
			moveQueryStr += String.format("self.period = %s", moveLineReport.getPeriod().getId());  
		}
		if(moveLineReport.getDateFrom() != null)  {
			moveLineQueryStr += " AND ";
			moveLineQueryStr += String.format("self.date >= '%s'", moveLineReport.getDateFrom().toString());  
		}
		if(moveLineReport.getDateTo() != null)  {
			moveLineQueryStr += " AND ";
			moveLineQueryStr += String.format("self.date <= '%s'", moveLineReport.getDateTo().toString());  
		}
		if(moveLineReport.getDate() != null)  {
			moveLineQueryStr += " AND ";
			moveLineQueryStr += String.format("self.date <= '%s'", moveLineReport.getDate().toString());  
		}
		if(replay)  {
			moveQueryStr += " AND self.accountingOk = true AND ";
			moveQueryStr += String.format("self.moveLineReport = %s", moveLineReport.getId());  
		}
		else  {
			moveQueryStr += " AND self.accountingOk = false ";
		}
		
		LocalDate interfaceDate = moveLineReport.getDate();
		
		for(LocalDate dt : allDates) {				
			
			List<Journal> journalList = Journal.all().filter("self.type = ?1 AND self.notExportOk = false", journalType).fetch();
			
			if(moveLineReport.getJournal() != null)  {
				journalList = new ArrayList<Journal>();
				journalList.add(moveLineReport.getJournal());
			}
			
			for(Journal journal : journalList)  {
			
				List<Move> moveList = Move.all().filter("self.date = ?1 AND self.ignoreInAccountingOk = false AND self.journal.notExportOk = false AND self.journal = ?2" + moveQueryStr, dt, journal).fetch();
				
				String journalCode = journal.getExportCode();
				
				if (moveList.size() > 0) {
						
					BigDecimal sumDebit = this.getSumDebit("self.account.reconcileOk = true AND self.debit!=0.00 AND self.move in ?1 "+moveLineQueryStr, moveList);
					
					if(sumDebit.compareTo(BigDecimal.ZERO) == 1)  {
						
						String exportNumber = this.getSaleExportNumber(company);
						
						Move firstMove = moveList.get(0);
						String periodCode = firstMove.getPeriod().getFromDate().toString("yyyyMM");
						
						this.updateMoveList(moveList, moveLineReport, interfaceDate, exportNumber);
						
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
		String filePath = this.getFilePath(company);
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
		
		String dateQueryStr = " WHERE " + String.format("company = %s", company.getId());
		JournalType journalType = mlrs.getJournalType(moveLineReport);
		if(moveLineReport.getJournal()!=null) {
			dateQueryStr += " AND " + String.format("journal = %s", moveLineReport.getJournal().getId());
		}
		else  {
			dateQueryStr += " AND " + String.format("journal.type = %s", journalType.getId());
		}
		if(moveLineReport.getPeriod() != null)	{
			dateQueryStr += " AND " + String.format("period = %s", moveLineReport.getPeriod().getId());
		}
		if(replay)  {
			dateQueryStr += " AND accountingOk = true AND " + String.format("moveLineReport = %s", moveLineReport.getId());
		}
		else  {
			dateQueryStr += " AND accountingOk = false ";
		}
		dateQueryStr += " AND ignoreInAccountingOk = false AND journal.notExportOk = false ";
		Query dateQuery = JPA.em().createQuery("SELECT mv.date from Move mv" + dateQueryStr + "group by mv.date order by mv.date");

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
			moveQueryStr += " AND ";
			moveQueryStr += String.format("self.company = %s", moveLineReport.getCompany().getId()); 
		}
		if(moveLineReport.getPeriod() != null)	{
			moveQueryStr += " AND ";
			moveQueryStr += String.format("self.period = %s", moveLineReport.getPeriod().getId());  
		}
		if(moveLineReport.getDateFrom() != null)  {
			moveLineQueryStr += " AND ";
			moveLineQueryStr += String.format("self.date >= '%s'", moveLineReport.getDateFrom().toString());  
		}
		if(moveLineReport.getDateTo() != null)  {
			moveLineQueryStr += " AND ";
			moveLineQueryStr += String.format("self.date <= '%s'", moveLineReport.getDateTo().toString());  
		}
		if(moveLineReport.getDate() != null)  {
			moveLineQueryStr += " AND ";
			moveLineQueryStr += String.format("self.date <= '%s'", moveLineReport.getDate().toString());  
		}
		if(replay)  {
			moveQueryStr += " AND self.accountingOk = true AND ";
			moveQueryStr += String.format("self.moveLineReport = %s", moveLineReport.getId());  
		}
		else  {
			moveQueryStr += " AND self.accountingOk = false ";
		}
		
		LocalDate interfaceDate = moveLineReport.getDate();
		
		for(LocalDate dt : allDates) {				
			
			List<Journal> journalList = Journal.all().filter("self.type = ?1 AND self.notExportOk = false", journalType).fetch();
			
			if(moveLineReport.getJournal()!=null)  {
				journalList = new ArrayList<Journal>();
				journalList.add(moveLineReport.getJournal());
			}
			
			for(Journal journal : journalList)  {
			
				List<Move> moveList = Move.all().filter("self.date = ?1 AND self.ignoreInAccountingOk = false AND self.journal.notExportOk = false AND self.journal = ?2" + moveQueryStr, dt, journal).fetch();
				
				String journalCode = journal.getExportCode();
				
				if (moveList.size() > 0) {

					BigDecimal sumCredit = this.getSumCredit("self.account.reconcileOk = true AND self.credit!=0.00 AND self.move in ?1 "+moveLineQueryStr, moveList);

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
		String filePath = this.getFilePath(company);
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
		
		String dateQueryStr = " WHERE " + String.format("company = %s", company.getId());
		JournalType journalType = mlrs.getJournalType(moveLineReport);
		if(moveLineReport.getJournal()!=null) {
			dateQueryStr += " AND " + String.format("journal = %s", moveLineReport.getJournal().getId());
		}
		else  {
			dateQueryStr += " AND " + String.format("journal.type = %s", journalType.getId());
		}
		if(moveLineReport.getPeriod() != null)	{
			dateQueryStr += " AND " + String.format("period = %s", moveLineReport.getPeriod().getId());
		}
		if(replay)  {
			dateQueryStr += " AND accountingOk = true AND " + String.format("moveLineReport = %s", moveLineReport.getId());
		}
		else  {
			dateQueryStr += " AND accountingOk = false ";
		}
		dateQueryStr += " AND ignoreInAccountingOk = false AND journal.notExportOk = false ";
		Query dateQuery = JPA.em().createQuery("SELECT mv.date from Move mv" + dateQueryStr + "group by mv.date order by mv.date");

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
			moveQueryStr += " AND ";
			moveQueryStr += String.format("self.company = %s", company.getId()); 
		}
		if(moveLineReport.getPeriod() != null)	{
			moveQueryStr += " AND ";
			moveQueryStr += String.format("self.period = %s", moveLineReport.getPeriod().getId());  
		}
		if(moveLineReport.getDateFrom() != null)  {
			moveLineQueryStr += " AND ";
			moveLineQueryStr += String.format("self.date >= '%s'", moveLineReport.getDateFrom().toString());  
		}
		if(moveLineReport.getDateTo() != null)  {
			moveLineQueryStr += " AND ";
			moveLineQueryStr += String.format("self.date <= '%s'", moveLineReport.getDateTo().toString());  
		}
		if(moveLineReport.getDate() != null)  {
			moveLineQueryStr += " AND ";
			moveLineQueryStr += String.format("self.date <= '%s'", moveLineReport.getDate().toString());  
		}
		if(replay)  {
			moveQueryStr += " AND self.accountingOk = true AND ";
			moveQueryStr += String.format("self.moveLineReport = %s", moveLineReport.getId());  
		}
		else  {
			moveQueryStr += " AND self.accountingOk = false ";
		}
		
		LocalDate interfaceDate = moveLineReport.getDate();
		
		for(LocalDate dt : allDates) {				
			
			List<Journal> journalList = Journal.all().filter("self.type = ?1 AND self.notExportOk = false", journalType).fetch();
			
			if(moveLineReport.getJournal()!=null)  {
				journalList = new ArrayList<Journal>();
				journalList.add(moveLineReport.getJournal());
			}
			
			for(Journal journal : journalList)  {
			
				List<Move> moveList = Move.all().filter("self.date = ?1 AND self.ignoreInAccountingOk = false AND self.journal.notExportOk = false AND self.journal = ?2" + moveQueryStr, dt, journal).fetch();
				
				String journalCode = journal.getExportCode();
				
				if (moveList.size() > 0) {
						
					long moveLineListSize = MoveLine.all().filter("self.move in ?1 AND (self.debit > 0 OR self.credit > 0) " + moveLineQueryStr, moveList).count();
					
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
		String filePath = this.getFilePath(company);
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
		String dateQueryStr = " WHERE " + String.format("company = %s", company.getId());
		JournalType journalType = mlrs.getJournalType(moveLineReport);
		if(moveLineReport.getJournal() != null) {
			dateQueryStr += " AND " + String.format("journal = %s", moveLineReport.getJournal().getId());
		}
		else  {
			dateQueryStr += " AND " + String.format("journal.type = %s", journalType.getId());
		}
		if(moveLineReport.getPeriod() != null)	{
			dateQueryStr += " AND " + String.format("period = %s", moveLineReport.getPeriod().getId());
		}
		if(replay)  {
			dateQueryStr += " AND accountingOk = true AND " + String.format("moveLineReport = %s", moveLineReport.getId());
		}
		else  {
			dateQueryStr += " AND accountingOk = false ";
		}
		dateQueryStr += " AND ignoreInAccountingOk = false AND journal.notExportOk = false ";
		Query dateQuery = JPA.em().createQuery("SELECT mv.date from Move mv" + dateQueryStr + "group by mv.date order by mv.date");

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
			moveQueryStr += " AND ";
			moveQueryStr += String.format("self.company = %s", company.getId()); 
		}
		if(moveLineReport.getPeriod() != null)	{
			moveQueryStr += " AND ";
			moveQueryStr += String.format("self.period = %s", moveLineReport.getPeriod().getId());  
		}
		if(moveLineReport.getDateFrom() != null)  {
			moveLineQueryStr += " AND ";
			moveLineQueryStr += String.format("self.date >= '%s'", moveLineReport.getDateFrom().toString());  
		}
		if(moveLineReport.getDateTo() != null)  {
			moveLineQueryStr += " AND ";
			moveLineQueryStr += String.format("self.date <= '%s'", moveLineReport.getDateTo().toString());  
		}
		if(moveLineReport.getDate() != null)  {
			moveLineQueryStr += " AND ";
			moveLineQueryStr += String.format("self.date <= '%s'", moveLineReport.getDate().toString());  
		}
		if(replay)  {
			moveQueryStr += " AND self.accountingOk = true AND ";
			moveQueryStr += String.format("self.moveLineReport = %s", moveLineReport.getId());  
		}
		else  {
			moveQueryStr += " AND self.accountingOk = false ";
		}
		
		LocalDate interfaceDate = moveLineReport.getDate();
		
		for(LocalDate dt : allDates) {				
			
			List<Journal> journalList = Journal.all().filter("self.type = ?1 AND self.notExportOk = false", journalType).fetch();
			
			if(moveLineReport.getJournal()!=null)  {
				journalList = new ArrayList<Journal>();
				journalList.add(moveLineReport.getJournal());
			}
			
			for(Journal journal : journalList)  {
			
				List<Move> moveList = Move.all().filter("self.date = ?1 AND self.ignoreInAccountingOk = false AND self.journal.notExportOk = false AND self.journal = ?2" + moveQueryStr, dt, journal).fetch();
				
				String journalCode = journal.getExportCode();
				
				int moveListSize = moveList.size();
				
				if (moveListSize > 0) {
					
					int i = 0;
					
					for(Move move : moveList)  {
						
						List<MoveLine> moveLineList = MoveLine.all().filter("self.account.reconcileOk = true AND self.credit!=0.00 AND self.move in ?1" + moveLineQueryStr, moveList).fetch();
						
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
							String items[] = new String[12];
							items[0] = companyCode;
							items[1] = journalCode;
							items[2] = exportNumber;
							items[3] = interfaceDate.toString("dd/MM/yyyy");
							items[4] = "";  //TODO code fournisseur
							items[5] = invoiceId;
							items[6] = dueDate;
							items[7]= firstMoveLine.getAccount().getCode();
							items[8]= totalCredit.toString();
							items[9]= reference;
							items[10]= dt.toString("dd/MM/yyyy");
							items[11]= periodCode;
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
		String filePath = this.getFilePath(company);
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
			moveLineQueryStr += " AND ";
			moveLineQueryStr += String.format("move.company = %s", company.getId());  
		}
		if(moveLineReport.getJournal() != null)	{
			moveLineQueryStr += " AND ";
			moveLineQueryStr += String.format("move.journal = %s", moveLineReport.getJournal().getId());  
		}
		else  {
			moveLineQueryStr += " AND ";
			moveLineQueryStr += String.format("move.journal.type = %s", mlrs.getJournalType(moveLineReport).getId());  
		}
		
		if(moveLineReport.getPeriod() != null)	{
			moveLineQueryStr += " AND ";
			moveLineQueryStr += String.format("move.period = %s", moveLineReport.getPeriod().getId());  
		}
		if(moveLineReport.getDateFrom() != null)  {
			moveLineQueryStr += " AND ";
			moveLineQueryStr += String.format("date >= '%s'", moveLineReport.getDateFrom().toString());  
		}
		
		if(moveLineReport.getDateTo() != null)  {
			moveLineQueryStr += " AND ";
			moveLineQueryStr += String.format("date <= '%s'", moveLineReport.getDateTo().toString());  
		}
		if(moveLineReport.getDate() != null)  {
			moveLineQueryStr += " AND ";
			moveLineQueryStr += String.format("date <= '%s'", moveLineReport.getDate().toString());  
		}
		if(typeSelect != 8 )  {
			moveLineQueryStr += " AND ";
			moveLineQueryStr += String.format("account.reconcileOk = false ");  
		}
		moveLineQueryStr += String.format("AND move.accountingOk = true AND move.ignoreInAccountingOk = false AND move.moveLineReport = %s", moveLineReport.getId());  
		
		Query queryDate = JPA.em().createQuery("SELECT mvl.date from MoveLine mvl where mvl.account!=null AND (mvl.debit > 0 OR mvl.credit > 0) " + moveLineQueryStr + " group by date ORDER BY mvl.date");
		
		List<LocalDate> dates = new ArrayList<LocalDate>();
		dates = queryDate.getResultList();
		
		LOG.debug("dates : {}" , dates);
		
		List<String[]> allMoveLineData = new ArrayList<String[]>();
		
		for (LocalDate localDate : dates)  {
			
			Query queryExportAgressoRef = JPA.em().createQuery("SELECT DISTINCT move.exportNumber from MoveLine mvl where mvl.account!=null " +
					"AND (mvl.debit > 0 OR mvl.credit > 0) AND mvl.date = '"+ localDate.toString() +"'"+ moveLineQueryStr);
			List<String> exportAgressoRefs = new ArrayList<String>();
			exportAgressoRefs = queryExportAgressoRef.getResultList();
			for(String exportAgressoRef : exportAgressoRefs)  {
				
				if(exportAgressoRef != null && !exportAgressoRef.isEmpty())  {
					
					int sequence = 1;
					
					Query query = JPA.em().createQuery("SELECT account.id from MoveLine mvl where mvl.account != null AND (mvl.debit > 0 OR mvl.credit > 0) " +
							"AND mvl.date = '"+ localDate.toString() +"' AND mvl.move.exportNumber = '"+ exportAgressoRef + "'" + moveLineQueryStr +
							" group by account.id");
					
					List<Long> accountIds = new ArrayList<Long>();
					accountIds = query.getResultList();
					
					LOG.debug("accountIds : {}" , accountIds);
					
					for (Long accountId : accountIds) {
						if(accountId!=null) {
							String accountCode = Account.find(accountId).getCode();
							List<MoveLine> moveLines = MoveLine.all().filter("account.id = ?1 AND (self.debit > 0 OR self.credit > 0) AND self.date = '"+ 
							localDate.toString() +"' AND move.exportNumber = '"+ exportAgressoRef +"'" + moveLineQueryStr, accountId).fetch();
							
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
		
		String filePath = this.getFilePath(company);
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
		
		mlrs.setStatus(moveLineReport);
		
			
		if(moveLineReport.getTypeSelect()==6){
			
			if(moveLineReport.getCompany().getExportPath()!=null) { this.exportMoveLineTypeSelect6(moveLineReport, false); }
			
		}
		else if(moveLineReport.getTypeSelect()==7){
			
			if(moveLineReport.getCompany().getExportPath()!=null) { this.exportMoveLineTypeSelect7(moveLineReport, false); }
			
		}
		else if(moveLineReport.getTypeSelect()==8){
			
			if(moveLineReport.getCompany().getExportPath()!=null) { this.exportMoveLineTypeSelect8(moveLineReport, false); }
			
		}
		else if(moveLineReport.getTypeSelect()==9){
			
			if(moveLineReport.getCompany().getExportPath()!=null) { this.exportMoveLineTypeSelect9(moveLineReport, false); }
			
		}
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public MoveLineReport createMoveLineReport(Company company, int exportTypeSelect, LocalDate startDate, LocalDate endDate) throws AxelorException  {
		
		MoveLineReport moveLineReport = new MoveLineReport();
		moveLineReport.setCompany(company);
		moveLineReport.setTypeSelect(exportTypeSelect);
		moveLineReport.setDateFrom(startDate);
		moveLineReport.setDateTo(endDate);
		moveLineReport.setStatus(Status.all().filter("self.code = 'dra'").fetchOne());
		moveLineReport.setDate(todayTime.toLocalDate());
		moveLineReport.setRef(mlrs.getSequence(moveLineReport));
		
		String queryFilter = mlrs.getMoveLineList(moveLineReport);
		BigDecimal debitBalance = mlrs.getDebitBalance(queryFilter);
		BigDecimal creditBalance = mlrs.getCreditBalance(queryFilter);
		
		moveLineReport.setTotalDebit(debitBalance);
		moveLineReport.setTotalCredit(creditBalance);
		moveLineReport.setBalance(debitBalance.subtract(creditBalance));
		
		moveLineReport.save();
		
		return moveLineReport;
		
	}
	
	
	
}