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
	private MoveLineReportService mlrs;
	
	@Inject
	private SequenceService sgs;
	
	@Inject
	public MoveLineExportService() {
		todayTime = GeneralService.getTodayDateTime();
	}
	
	public String getFilePath(MoveLineReport mlr) throws AxelorException{
		if(mlr.getCompany().getExportPath()!=null) {
			return mlr.getCompany().getExportPath();
		}
		else  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un Chemin Fichier Exporté (si -> AGRESSO) pour la société %s",
					GeneralService.getExceptionAccountingMsg(),mlr.getCompany().getName()), IException.CONFIGURATION_ERROR);
		}
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
	
	
	/**
	 * Méthode réalisant l'export SI - Agresso pour les journaux de type vente
	 * @param mlr
	 * @param replay
	 * @throws AxelorException
	 */
	public void exportMoveLineTypeSelect6(MoveLineReport mlr, boolean replay) throws AxelorException {
		
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
	 */
	@SuppressWarnings("unchecked")
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void exportMoveLineTypeSelect6FILE1(MoveLineReport mlr, boolean replay) throws AxelorException {
		try{
			
			LOG.info("In export service Type 6 FILE 1 :");
			
			String queryStr = " WHERE " + String.format("company = %s", mlr.getCompany().getId());
			JournalType journalType = mlrs.getJournalType(mlr);
			if(mlr.getJournal()!=null) {
				queryStr += " AND " + String.format("journal = %s", mlr.getJournal().getId());
			}
			else  {
				queryStr += " AND " + String.format("journal.type = %s", journalType.getId());
			}
			if(mlr.getPeriod() != null)	{
				queryStr += " AND " + String.format("period = %s", mlr.getPeriod().getId());
			}
			if(replay)  {
				queryStr += " AND accountingOk = true AND " + String.format("moveLineReport = %s", mlr.getId());
			}
			else  {
				queryStr += " AND accountingOk = false ";
			}
			queryStr += " AND ignoreInAccountingOk = false ";
			Query query = JPA.em().createQuery("SELECT mv.date from Move mv" + queryStr + "group by mv.date order by mv.date");

			List<LocalDate> allDates = new ArrayList<LocalDate>();
			allDates = query.getResultList();
			
			LOG.debug("allDates : {}" , allDates);
			
			List<String[]> allMoveData = new ArrayList<String[]>();
			String companyCode = "";
			
			String reference = "";
			String sqlRequest = "";
			String sqlRequestML = "";
			if(mlr.getRef()!=null) {
				reference = mlr.getRef();
			}
			if(mlr.getCompany()!=null) {
				companyCode = mlr.getCompany().getCode();
				sqlRequest += " AND ";
				sqlRequest += String.format("self.company = %s", mlr.getCompany().getId()); 
			}
			if(mlr.getPeriod() != null)	{
				sqlRequest += " AND ";
				sqlRequest += String.format("self.period = %s", mlr.getPeriod().getId());  
			}
			if(mlr.getDateFrom() != null)  {
				sqlRequestML += " AND ";
				sqlRequestML += String.format("self.date >= '%s'", mlr.getDateFrom().toString());  
			}
			if(mlr.getDateTo() != null)  {
				sqlRequestML += " AND ";
				sqlRequestML += String.format("self.date <= '%s'", mlr.getDateTo().toString());  
			}
			if(mlr.getDate() != null)  {
				sqlRequestML += " AND ";
				sqlRequestML += String.format("self.date <= '%s'", mlr.getDate().toString());  
			}
			if(replay)  {
				sqlRequest += " AND self.accountingOk = true AND ";
				sqlRequest += String.format("self.moveLineReport = %s", mlr.getId());  
			}
			else  {
				sqlRequest += " AND self.accountingOk = false ";
			}
			
			LocalDate interfaceDate = mlr.getDate();
			
			
			for(LocalDate dt : allDates) {				
				
				List<Journal> journalList = Journal.all().filter("self.type = ?1", journalType).fetch();
				
				if(mlr.getJournal()!=null)  {
					journalList = new ArrayList<Journal>();
					journalList.add(mlr.getJournal());
				}
				
				for(Journal journal : journalList)  {
				
					List<Move> moves = Move.all().filter("self.date = ?1 AND self.ignoreInAccountingOk = false AND self.journal = ?2" + sqlRequest, dt, journal).fetch();
					
					String journalCode = journal.getExportCode();
					
					if (moves.size() > 0) {
							
						List<MoveLine> moveLines = MoveLine.all().filter("self.account.reconcileOk = true AND self.ignoreInAccountingOk = false AND self.debit!=0.00 AND self.move in ?1" + sqlRequestML, moves).fetch();
						
						LOG.debug("movelines : {}" , moveLines);
						
						if(moveLines.size() > 0) {
							
							String exportToAgressoNumber = sgs.getSequence(IAdministration.SALES_INTERFACE, mlr.getCompany(), false);
							if(exportToAgressoNumber == null)  {  
								throw new AxelorException(String.format("%s :\n Erreur : Veuillez configurer une séquence Interface Vente pour la société %s",
										GeneralService.getExceptionAccountingMsg(), mlr.getCompany().getName()), IException.CONFIGURATION_ERROR);
							}
							
							Move firstMove = moves.get(0);
//							String periodCode = String.format("%s%s", firstMove.getPeriod().getFromDate().getYear(), firstMove.getPeriod().getFromDate().getMonthOfYear());
							String periodCode = firstMove.getPeriod().getFromDate().toString("yyyyMM");
							
							for(Move move : moves)  {
								move.setExportToAgressoNumber(exportToAgressoNumber);
								move.setExportToAgressoDate(interfaceDate);
								move.setAccountingOk(true);
								move.setMoveLineReport(mlr);
								move.save();
							}
							
							BigDecimal totalDebit = BigDecimal.ZERO;
							for(MoveLine moveLine : moveLines) {
								totalDebit = totalDebit.add(moveLine.getDebit());
							}
							String items[] = new String[8];
							items[0] = companyCode;
							items[1] = journalCode;
							items[2] = exportToAgressoNumber;
							items[3] = interfaceDate.toString("dd/MM/yyyy");
							items[4] = totalDebit.toString();
							items[5] = reference;
							items[6] = dt.toString("dd/MM/yyyy");
							items[7]= periodCode;
							allMoveData.add(items);
						}
					}
				}
			}
						
			String fileName = "entete"+todayTime.toString("YYYYMMddHHmmss")+"ventes.dat";			
			String filePath = this.getFilePath(mlr);
			new File(filePath).mkdirs();
			
			LOG.debug("Full path to export : {}{}" , filePath, fileName);
			CsvTool.csvWriter(filePath, fileName, '|', null, allMoveData);
			// Utilisé pour le debuggage
//			CsvTool.csvWriter(filePath, fileName, '|', this.createHeaderForHeaderFile(mlr.getTypeSelect()), allMoveData);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	

	
	
	
	/**
	 * Méthode réalisant l'export SI - Agresso pour les journaux de type avoir
	 * @param mlr
	 * @param replay
	 * @throws AxelorException
	 */
	public void exportMoveLineTypeSelect7(MoveLineReport mlr, boolean replay) throws AxelorException {
		
		LOG.info("In Export type 7 service : ");
		
		String fileName = "detail"+todayTime.toString("YYYYMMddHHmmss")+"avoirs.dat";
		this.exportMoveLineTypeSelect7FILE1(mlr, replay);
		this.exportMoveLineAllTypeSelectFILE2(mlr, fileName);
	}
	
	
	/**
	 * Méthode réalisant l'export SI - Agresso des en-têtes pour les journaux de type avoir
	 * @param mlr
	 * @param replay
	 * @throws AxelorException
	 */
	@SuppressWarnings("unchecked")
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void exportMoveLineTypeSelect7FILE1(MoveLineReport mlr, boolean replay) throws AxelorException {
		try{
			
			LOG.info("In export service 7 FILE 1:");
			
			String queryStr = " WHERE " + String.format("company = %s", mlr.getCompany().getId());
			JournalType journalType = mlrs.getJournalType(mlr);
			if(mlr.getJournal()!=null) {
				queryStr += " AND " + String.format("journal = %s", mlr.getJournal().getId());
			}
			else  {
				queryStr += " AND " + String.format("journal.type = %s", journalType.getId());
			}
			if(mlr.getPeriod() != null)	{
				queryStr += " AND " + String.format("period = %s", mlr.getPeriod().getId());
			}
			if(replay)  {
				queryStr += " AND accountingOk = true AND " + String.format("moveLineReport = %s", mlr.getId());
			}
			else  {
				queryStr += " AND accountingOk = false ";
			}
			queryStr += " AND ignoreInAccountingOk = false ";
			Query query = JPA.em().createQuery("SELECT mv.date from Move mv" + queryStr + "group by mv.date order by mv.date");

			List<LocalDate> allDates = new ArrayList<LocalDate>();
			allDates = query.getResultList();
			
			LOG.debug("allDates : {}" , allDates);
			
			List<String[]> allMoveData = new ArrayList<String[]>();
			String companyCode = "";
			
			String reference = "";
			String sqlRequest = "";
			String sqlRequestML = "";
			if(mlr.getRef()!=null) {
				reference = mlr.getRef();
			}
			if(mlr.getCompany()!=null) {
				companyCode = mlr.getCompany().getCode();
				sqlRequest += " AND ";
				sqlRequest += String.format("self.company = %s", mlr.getCompany().getId()); 
			}
			if(mlr.getPeriod() != null)	{
				sqlRequest += " AND ";
				sqlRequest += String.format("self.period = %s", mlr.getPeriod().getId());  
			}
			if(mlr.getDateFrom() != null)  {
				sqlRequestML += " AND ";
				sqlRequestML += String.format("self.date >= '%s'", mlr.getDateFrom().toString());  
			}
			if(mlr.getDateTo() != null)  {
				sqlRequestML += " AND ";
				sqlRequestML += String.format("self.date <= '%s'", mlr.getDateTo().toString());  
			}
			if(mlr.getDate() != null)  {
				sqlRequestML += " AND ";
				sqlRequestML += String.format("self.date <= '%s'", mlr.getDate().toString());  
			}
			if(replay)  {
				sqlRequest += " AND self.accountingOk = true AND ";
				sqlRequest += String.format("self.moveLineReport = %s", mlr.getId());  
			}
			else  {
				sqlRequest += " AND self.accountingOk = false ";
			}
			
			LocalDate interfaceDate = mlr.getDate();
			
			for(LocalDate dt : allDates) {				
				
				List<Journal> journalList = Journal.all().filter("self.type = ?1", journalType).fetch();
				
				if(mlr.getJournal()!=null)  {
					journalList = new ArrayList<Journal>();
					journalList.add(mlr.getJournal());
				}
				
				for(Journal journal : journalList)  {
				
					List<Move> moves = Move.all().filter("self.date = ?1 AND self.ignoreInAccountingOk = false AND self.journal = ?2" + sqlRequest, dt, journal).fetch();
					
					String journalCode = journal.getExportCode();
					
					if (moves.size() > 0) {

						List<MoveLine> moveLines = MoveLine.all().filter("self.account.reconcileOk = true AND self.ignoreInAccountingOk = false AND self.credit!=0.00 AND self.move in ?1" + sqlRequestML, moves).fetch();
						
						LOG.debug("movelines : {}" , moveLines);
						
						if(moveLines.size() > 0) {
							
							String exportToAgressoNumber = sgs.getSequence(IAdministration.REFUND_INTERFACE, mlr.getCompany(), false);
							if(exportToAgressoNumber == null)  {  
								throw new AxelorException(String.format("%s :\n Erreur : Veuillez configurer une séquence Interface Avoir pour la société %s",
										GeneralService.getExceptionAccountingMsg(), mlr.getCompany().getName()), IException.CONFIGURATION_ERROR);
							}
							
							Move firstMove = moves.get(0);
							String periodCode = firstMove.getPeriod().getFromDate().toString("yyyyMM");
							
							for(Move move : moves)  {
								
								move.setExportToAgressoNumber(exportToAgressoNumber);
								move.setExportToAgressoDate(interfaceDate);
								move.setAccountingOk(true);
								move.setMoveLineReport(mlr);
								move.save();
							}
							
							BigDecimal totalCredit = BigDecimal.ZERO;
							for(MoveLine moveLine : moveLines) {
								totalCredit = totalCredit.add(moveLine.getCredit());
							}
							String items[] = new String[8];
							items[0] = companyCode;
							items[1] = journalCode;
							items[2] = exportToAgressoNumber;
							items[3] = interfaceDate.toString("dd/MM/yyyy");
							items[4] = totalCredit.toString();
							items[5] = reference;
							items[6] = dt.toString("dd/MM/yyyy");
							items[7]= periodCode;
							allMoveData.add(items);
						}
					}
				}
			}
						
			String fileName = "entete"+todayTime.toString("YYYYMMddHHmmss")+"avoirs.dat";
			String filePath = this.getFilePath(mlr);
			new File(filePath).mkdirs();
			
			LOG.debug("Full path to export : {}{}" , filePath, fileName);
			CsvTool.csvWriter(filePath, fileName, '|', null, allMoveData);
			// Utilisé pour le debuggage
//			CsvTool.csvWriter(filePath, fileName, '|', this.createHeaderForHeaderFile(mlr.getTypeSelect()), allMoveData);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Méthode réalisant l'export SI - Agresso pour les journaux de type trésorerie
	 * @param mlr
	 * @param replay
	 * @throws AxelorException
	 */
	public void exportMoveLineTypeSelect8(MoveLineReport mlr, boolean replay) throws AxelorException {
		
		LOG.info("In Export type 8 service : ");
		
		String fileName = "detail"+todayTime.toString("YYYYMMddHHmmss")+"tresorerie.dat";
		this.exportMoveLineTypeSelect8FILE1(mlr, replay);
		this.exportMoveLineAllTypeSelectFILE2(mlr, fileName);
	}
	
	
	/**
	 * Méthode réalisant l'export SI - Agresso des en-têtes pour les journaux de type trésorerie
	 * @param mlr
	 * @param replay
	 * @throws AxelorException
	 */
	@SuppressWarnings("unchecked")
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void exportMoveLineTypeSelect8FILE1(MoveLineReport mlr, boolean replay) throws AxelorException {
		try{
			
			LOG.info("In export service 8 FILE 1:");
			
			String queryStr = " WHERE " + String.format("company = %s", mlr.getCompany().getId());
			JournalType journalType = mlrs.getJournalType(mlr);
			if(mlr.getJournal()!=null) {
				queryStr += " AND " + String.format("journal = %s", mlr.getJournal().getId());
			}
			else  {
				queryStr += " AND " + String.format("journal.type = %s", journalType.getId());
			}
			if(mlr.getPeriod() != null)	{
				queryStr += " AND " + String.format("period = %s", mlr.getPeriod().getId());
			}
			if(replay)  {
				queryStr += " AND accountingOk = true AND " + String.format("moveLineReport = %s", mlr.getId());
			}
			else  {
				queryStr += " AND accountingOk = false ";
			}
			queryStr += " AND ignoreInAccountingOk = false ";
			Query query = JPA.em().createQuery("SELECT mv.date from Move mv" + queryStr + "group by mv.date order by mv.date");

			List<LocalDate> allDates = new ArrayList<LocalDate>();
			allDates = query.getResultList();
			
			LOG.debug("allDates : {}" , allDates);
			
			List<String[]> allMoveData = new ArrayList<String[]>();
			String companyCode = "";
			
			String reference = "";
			String sqlRequest = "";
			String sqlRequestML = "";
			if(mlr.getRef()!=null) {
				reference = mlr.getRef();
			}
			if(mlr.getCompany()!=null) {
				companyCode = mlr.getCompany().getCode();
				sqlRequest += " AND ";
				sqlRequest += String.format("self.company = %s", mlr.getCompany().getId()); 
			}
			if(mlr.getPeriod() != null)	{
				sqlRequest += " AND ";
				sqlRequest += String.format("self.period = %s", mlr.getPeriod().getId());  
			}
			if(mlr.getDateFrom() != null)  {
				sqlRequestML += " AND ";
				sqlRequestML += String.format("self.date >= '%s'", mlr.getDateFrom().toString());  
			}
			if(mlr.getDateTo() != null)  {
				sqlRequestML += " AND ";
				sqlRequestML += String.format("self.date <= '%s'", mlr.getDateTo().toString());  
			}
			if(mlr.getDate() != null)  {
				sqlRequestML += " AND ";
				sqlRequestML += String.format("self.date <= '%s'", mlr.getDate().toString());  
			}
			if(replay)  {
				sqlRequest += " AND self.accountingOk = true AND ";
				sqlRequest += String.format("self.moveLineReport = %s", mlr.getId());  
			}
			else  {
				sqlRequest += " AND self.accountingOk = false ";
			}
			
			LocalDate interfaceDate = mlr.getDate();
			
			for(LocalDate dt : allDates) {				
				
				List<Journal> journalList = Journal.all().filter("self.type = ?1", journalType).fetch();
				
				if(mlr.getJournal()!=null)  {
					journalList = new ArrayList<Journal>();
					journalList.add(mlr.getJournal());
				}
				
				for(Journal journal : journalList)  {
				
					List<Move> moves = Move.all().filter("self.date = ?1 AND self.ignoreInAccountingOk = false AND self.journal = ?2" + sqlRequest, dt, journal).fetch();
					
					String journalCode = journal.getExportCode();
					
					if (moves.size() > 0) {
							
						List<MoveLine> moveLines = MoveLine.all().filter("self.move in ?1 AND self.ignoreInAccountingOk = false AND (self.debit > 0 OR self.credit > 0) " + sqlRequestML, moves).fetch();
						
						LOG.debug("movelines : {}" , moveLines);
						
						if(moveLines.size() > 0) {
							
							String exportToAgressoNumber = sgs.getSequence(IAdministration.TREASURY_INTERFACE, mlr.getCompany(), false);
							if(exportToAgressoNumber == null)  {  
								throw new AxelorException(String.format("%s :\n Erreur : Veuillez configurer une séquence Interface Trésorerie pour la société %s",
										GeneralService.getExceptionAccountingMsg(), mlr.getCompany().getName()), IException.CONFIGURATION_ERROR);
							}
							
							Move firstMove = moves.get(0);
							String periodCode = firstMove.getPeriod().getFromDate().toString("yyyyMM");
							
							for(Move move : moves)  {
								move.setExportToAgressoNumber(exportToAgressoNumber);
								move.setExportToAgressoDate(interfaceDate);
								move.setAccountingOk(true);
								move.setMoveLineReport(mlr);
								move.save();
							}
							
							String items[] = new String[8];
							items[0] = companyCode;
							items[1] = journalCode;
							items[2] = exportToAgressoNumber;
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
			String filePath = this.getFilePath(mlr);
			new File(filePath).mkdirs();
			
			LOG.debug("Full path to export : {}{}" , filePath, fileName);
			CsvTool.csvWriter(filePath, fileName, '|', null, allMoveData);
			// Utilisé pour le debuggage
//			CsvTool.csvWriter(filePath, fileName, '|', this.createHeaderForHeaderFile(mlr.getTypeSelect()), allMoveData);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Méthode réalisant l'export SI - Agresso pour les journaux de type achat
	 * @param mlr
	 * @param replay
	 * @throws AxelorException
	 */
	public void exportMoveLineTypeSelect9(MoveLineReport mlr, boolean replay) throws AxelorException {
		
		LOG.info("In Export type 9 service : ");
		String fileName = "detail"+todayTime.toString("YYYYMMddHHmmss")+"achats.dat";
		this.exportMoveLineTypeSelect9FILE1(mlr, replay);
		this.exportMoveLineAllTypeSelectFILE2(mlr, fileName);
	}
	
	
	/**
	 * Méthode réalisant l'export SI - Agresso des en-têtes pour les journaux de type achat
	 * @param mlr
	 * @param replay
	 * @throws AxelorException
	 */
	@SuppressWarnings("unchecked")
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void exportMoveLineTypeSelect9FILE1(MoveLineReport mlr, boolean replay) throws AxelorException {
		try{
			
			LOG.info("In export service 9 FILE 1:");
			String queryStr = " WHERE " + String.format("company = %s", mlr.getCompany().getId());
			JournalType journalType = mlrs.getJournalType(mlr);
			if(mlr.getJournal()!=null) {
				queryStr += " AND " + String.format("journal = %s", mlr.getJournal().getId());
			}
			else  {
				queryStr += " AND " + String.format("journal.type = %s", journalType.getId());
			}
			if(mlr.getPeriod() != null)	{
				queryStr += " AND " + String.format("period = %s", mlr.getPeriod().getId());
			}
			if(replay)  {
				queryStr += " AND accountingOk = true AND " + String.format("moveLineReport = %s", mlr.getId());
			}
			else  {
				queryStr += " AND accountingOk = false ";
			}
			queryStr += " AND ignoreInAccountingOk = false ";
			Query query = JPA.em().createQuery("SELECT mv.date from Move mv" + queryStr + "group by mv.date order by mv.date");

			List<LocalDate> allDates = new ArrayList<LocalDate>();
			allDates = query.getResultList();
			
			LOG.debug("allDates : {}" , allDates);
			
			List<String[]> allMoveData = new ArrayList<String[]>();
			String companyCode = "";
			
			String reference = "";
			String sqlRequest = "";
			String sqlRequestML = "";
			if(mlr.getRef()!=null) {
				reference = mlr.getRef();
			}
			if(mlr.getCompany()!=null) {
				companyCode = mlr.getCompany().getCode();
				sqlRequest += " AND ";
				sqlRequest += String.format("self.company = %s", mlr.getCompany().getId()); 
			}
			if(mlr.getPeriod() != null)	{
				sqlRequest += " AND ";
				sqlRequest += String.format("self.period = %s", mlr.getPeriod().getId());  
			}
			if(mlr.getDateFrom() != null)  {
				sqlRequestML += " AND ";
				sqlRequestML += String.format("self.date >= '%s'", mlr.getDateFrom().toString());  
			}
			if(mlr.getDateTo() != null)  {
				sqlRequestML += " AND ";
				sqlRequestML += String.format("self.date <= '%s'", mlr.getDateTo().toString());  
			}
			if(mlr.getDate() != null)  {
				sqlRequestML += " AND ";
				sqlRequestML += String.format("self.date <= '%s'", mlr.getDate().toString());  
			}
			if(replay)  {
				sqlRequest += " AND self.accountingOk = true AND ";
				sqlRequest += String.format("self.moveLineReport = %s", mlr.getId());  
			}
			else  {
				sqlRequest += " AND self.accountingOk = false ";
			}
			
			LocalDate interfaceDate = mlr.getDate();
			
			for(LocalDate dt : allDates) {				
				
				List<Journal> journalList = Journal.all().filter("self.type = ?1", journalType).fetch();
				
				if(mlr.getJournal()!=null)  {
					journalList = new ArrayList<Journal>();
					journalList.add(mlr.getJournal());
				}
				
				for(Journal journal : journalList)  {
				
					List<Move> moves = Move.all().filter("self.date = ?1 AND self.ignoreInAccountingOk = false AND self.journal = ?2" + sqlRequest, dt, journal).fetch();
					
					String journalCode = journal.getExportCode();
					
					if (moves.size() > 0) {
						
						for(Move move : moves)  {
							
							List<MoveLine> moveLines = MoveLine.all().filter("self.account.reconcileOk = true AND self.ignoreInAccountingOk = false AND self.credit!=0.00 AND self.move in ?1" + sqlRequestML, moves).fetch();
							
							LOG.debug("movelines : {}" , moveLines);
							
							if(moveLines.size() > 0) {
								
								String exportToAgressoNumber = sgs.getSequence(IAdministration.PURCHASE_INTERFACE, mlr.getCompany(), false);
								if(exportToAgressoNumber == null)  {  
									throw new AxelorException(String.format("%s :\n Erreur : Veuillez configurer une séquence Interface Achat pour la société %s",
											GeneralService.getExceptionAccountingMsg(), mlr.getCompany().getName()), IException.CONFIGURATION_ERROR);
								}
								
								String periodCode = move.getPeriod().getFromDate().toString("yyyyMM");
								
								move.setExportToAgressoNumber(exportToAgressoNumber);
								move.setExportToAgressoDate(interfaceDate);
								move.setAccountingOk(true);
								move.setMoveLineReport(mlr);
								move.save();
								
								BigDecimal totalDebit = BigDecimal.ZERO;
								for(MoveLine moveLine : moveLines) {
									totalDebit = totalDebit.add(moveLine.getDebit());
								}
								String invoiceId = "";
								String dueDate = "";
								if(move.getInvoice() != null)  {
									invoiceId = move.getInvoice().getInvoiceId();
									dueDate = move.getInvoice().getDueDate().toString();
								}
								
								MoveLine firstMoveLine = moveLines.get(0);
								String items[] = new String[12];
								items[0] = companyCode;
								items[1] = journalCode;
								items[2] = exportToAgressoNumber;
								items[3] = interfaceDate.toString("dd/MM/yyyy");
								items[4] = "";  //TODO code fournisseur
								items[5] = invoiceId;
								items[6] = dueDate;
								items[7]= firstMoveLine.getAccount().getCode();
								items[8]= totalDebit.toString();
								items[9]= reference;
								items[10]= dt.toString("dd/MM/yyyy");
								items[11]= periodCode;
								allMoveData.add(items);
							}
						}
					}
				}
			}
						
			String fileName = "entete"+todayTime.toString("YYYYMMddHHmmss")+"achats.dat";
			String filePath = this.getFilePath(mlr);
			new File(filePath).mkdirs();
			
			LOG.debug("Full path to export : {}{}" , filePath, fileName);
			CsvTool.csvWriter(filePath, fileName, '|', null, allMoveData);
			// Utilisé pour le debuggage
//			CsvTool.csvWriter(filePath, fileName, '|', this.createHeaderForHeaderFile(mlr.getTypeSelect()), allMoveData);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Méthode réalisant l'export SI - Agresso des fichiers détails
	 * @param mlr
	 * @param fileName
	 * @throws AxelorException
	 */
	@SuppressWarnings("unchecked")
	public void exportMoveLineAllTypeSelectFILE2(MoveLineReport mlr, String fileName) throws AxelorException {
		try {
			
			LOG.info("In export service FILE 2 :");
			
			String companyCode = "";
			String sqlRequest = "";
			
			int typeSelect = mlr.getTypeSelect();
			
			if(mlr.getCompany() != null) {
				companyCode = mlr.getCompany().getCode();
				sqlRequest += " AND ";
				sqlRequest += String.format("company = %s", mlr.getCompany().getId());  
			}
			if(mlr.getJournal() != null)	{
				sqlRequest += " AND ";
				sqlRequest += String.format("move.journal = %s", mlr.getJournal().getId());  
			}
			else  {
				sqlRequest += " AND ";
				sqlRequest += String.format("move.journal.type = %s", mlrs.getJournalType(mlr).getId());  
			}
			
			if(mlr.getPeriod() != null)	{
				sqlRequest += " AND ";
				sqlRequest += String.format("move.period = %s", mlr.getPeriod().getId());  
			}
			if(mlr.getDateFrom() != null)  {
				sqlRequest += " AND ";
				sqlRequest += String.format("date >= '%s'", mlr.getDateFrom().toString());  
			}
			
			if(mlr.getDateTo() != null)  {
				sqlRequest += " AND ";
				sqlRequest += String.format("date <= '%s'", mlr.getDateTo().toString());  
			}
			if(mlr.getDate() != null)  {
				sqlRequest += " AND ";
				sqlRequest += String.format("date <= '%s'", mlr.getDate().toString());  
			}
			if(typeSelect != 8 )  {
				sqlRequest += " AND ";
				sqlRequest += String.format("account.reconcileOk = false ");  
			}
			sqlRequest += String.format("AND move.accountingOk = true AND ignoreInAccountingOk = false AND move.moveLineReport = %s", mlr.getId());  
			
			Query queryDate = JPA.em().createQuery("SELECT mvl.date from MoveLine mvl where mvl.account!=null AND (mvl.debit > 0 OR mvl.credit > 0) " + sqlRequest + " group by date ORDER BY mvl.date");
			
			List<LocalDate> dates = new ArrayList<LocalDate>();
			dates = queryDate.getResultList();
			
			List<String[]> allMoveLineData = new ArrayList<String[]>();
			
			for (LocalDate localDate : dates)  {
				
				Query queryExportAgressoRef = JPA.em().createQuery("SELECT DISTINCT move.exportToAgressoNumber from MoveLine mvl where mvl.account!=null AND (mvl.debit > 0 OR mvl.credit > 0) AND mvl.date = '"+ localDate.toString() +"'"+ sqlRequest);
				List<String> exportAgressoRefs = new ArrayList<String>();
				exportAgressoRefs = queryExportAgressoRef.getResultList();
				for(String exportAgressoRef : exportAgressoRefs)  {
					
					if(exportAgressoRef != null && !exportAgressoRef.isEmpty())  {
						
						int sequence = 1;
						
						Query query = JPA.em().createQuery("SELECT account.id from MoveLine mvl where mvl.account!=null AND (mvl.debit > 0 OR mvl.credit > 0) AND mvl.date = '"+ localDate.toString() +"' AND mvl.move.exportToAgressoNumber = '"+ exportAgressoRef + "'" + sqlRequest + " group by account.id");
						
						List<Long> accountIds = new ArrayList<Long>();
						accountIds = query.getResultList();
						
						LOG.debug("accountIds : {}" , accountIds);
						
						for (Long accountId : accountIds) {
							if(accountId!=null) {
								String accountCode = Account.find(accountId).getCode();
								List<MoveLine> moveLines = MoveLine.all().filter("account.id = ?1 AND (self.debit > 0 OR self.credit > 0) AND self.date = '"+ localDate.toString() +"' AND move.exportToAgressoNumber = '"+ exportAgressoRef +"'" + sqlRequest, accountId).fetch();
								
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
										items[2] = moveLine3.getMove().getExportToAgressoNumber();
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
			
			String filePath = this.getFilePath(mlr);
			new File(filePath).mkdirs();
			
			LOG.debug("Full path to export : {}{}" , filePath, fileName);
			CsvTool.csvWriter(filePath, fileName, '|',  null, allMoveLineData);
			// Utilisé pour le debuggage
//			CsvTool.csvWriter(filePath, fileName, '|',  this.createHeaderForDetailFile(typeSelect), allMoveLineData);  
		}
		catch(IOException e){
			e.printStackTrace();
		}
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
						&& moveLine.getMove().getExportToAgressoNumber().equals(moveLine2.getMove().getExportToAgressoNumber()))  {
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
	
	
}