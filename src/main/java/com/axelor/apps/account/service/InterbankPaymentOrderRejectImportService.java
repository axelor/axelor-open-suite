package com.axelor.apps.account.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.tool.file.FileTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class InterbankPaymentOrderRejectImportService {
	
	private static final Logger LOG = LoggerFactory.getLogger(InterbankPaymentOrderRejectImportService.class); 
	
	@Inject
	private RejectImportService ris;
	
	@Inject
	private CfonbService cs;
	
	@Inject
	private SequenceService sGeneralService;
	
	@Inject
	private PaymentModeService pms;
	
	@Inject
	private MoveService ms;
	
	@Inject
	private MoveLineService mls;
	

	public List<String[]> getCFONBFile(Company company) throws AxelorException, IOException  {
		String dest = ris.getDestFilename(company.getInterbankPaymentOrderRejectImportPathCFONB(), company.getTempInterbankPaymentOrderRejectImportPathCFONB());
		
		// copie du fichier d'import dans un repetoire temporaire
		FileTool.copy(company.getInterbankPaymentOrderRejectImportPathCFONB(), dest);
		
		return cs.importCFONB(dest, company, 2);
	}
	
	
	/**
	 * Procédure permettant de 
	 * @param rejectList
	 * @param company
	 * @param interbankPaymentOrderRejectImport
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Invoice createInterbankPaymentOrderRejectMove(String[] reject, Company company) throws AxelorException  {
			String dateReject = reject[0];
			String refReject = reject[1];
			BigDecimal amountReject = new BigDecimal(reject[2]);
			InterbankCodeLine causeReject = ris.getInterbankCodeLine(reject[3], 0);
			
			Invoice invoice = Invoice.all().filter("UPPER(self.invoiceId) = ?1 AND self.company = ?2", refReject, company).fetchOne();
			if(invoice == null)  {
				throw new AxelorException(String.format("%s \nAucune facture trouvée pour le numéro de facture %s et la société %s",
						GeneralService.getExceptionAccountingMsg(), refReject, company.getName()), IException.INCONSISTENCY);
			}
			
			Partner partner = invoice.getPartner();
			if(invoice.getPaymentMode() == null)  {
				throw new AxelorException(String.format("%s - Aucun mode de paiement configuré pour la facture %s",
						GeneralService.getExceptionAccountingMsg(), refReject), IException.INCONSISTENCY);
			}
			
			Account bankAccount = pms.getCompanyAccount(invoice.getPaymentMode(), company);
			
			Move move = ms.createMove(company.getRejectJournal(), company, null, partner, null, true);
			
			// Création d'une ligne au crédit
			MoveLine debitMoveLine = mls.createMoveLine(move , partner, company.getCustomerAccount(), amountReject, true, false, 
					ris.createRejectDate(dateReject), 1, false, false, false, refReject);
			move.getMoveLineList().add(debitMoveLine);	
			debitMoveLine.setInterbankCodeLine(causeReject);
			
			// Création d'une ligne au crédit
			MoveLine creditMoveLine = mls.createMoveLine(move , partner, bankAccount, amountReject, false, false, 
					ris.createRejectDate(dateReject), 2, false, false, false, null);
			move.getMoveLineList().add(creditMoveLine);		
			ms.validateMove(move);
			move.save();
			
			return invoice;
	}
	
	
	/**
	 * Fonction permettant de récupérer le compte comptable associé au mode de paiement
	 * @param company
	 * 			Une société
	 * @param isTIPCheque
	 * 			Récupérer le compte comptable du mode de paiement TIP chèque ou seulement TIP ?
	 * @return
	 * @throws AxelorException
	 */
	public Account getAccount(Company company, boolean isTIPCheque) throws AxelorException  {
		PaymentMode paymentMode = null;
		
		if(isTIPCheque)  {
			paymentMode = PaymentMode.all().filter("self.code = 'TIC'").fetchOne();
			if(paymentMode == null)  {
				throw new AxelorException(String.format("%s :\n Le mode de paiement dont le code est 'TIC' n'a pas été trouvé",
						GeneralService.getExceptionAccountingMsg()), IException.CONFIGURATION_ERROR);
			}
		}
		else  {
			paymentMode = PaymentMode.all().filter("self.code = 'TIP'").fetchOne();
			if(paymentMode == null)  {
				throw new AxelorException(String.format("%s :\n Le mode de paiement dont le code est 'TIP' n'a pas été trouvé",
						GeneralService.getExceptionAccountingMsg()), IException.CONFIGURATION_ERROR);
			}
		}
		return pms.getCompanyAccount(paymentMode, company);
	}
	
	
	/**
	 * Procédure permettant de tester la présence des champs et des séquences nécessaires aux rejets de paiement par TIP et TIP chèque.
	 *
	 * @param company
	 * 			Une société
	 * @throws AxelorException
	 */
	public void testCompanyField(Company company) throws AxelorException  {
	
		if(company.getRejectJournal() == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un journal de rejet pour la société %s",
					GeneralService.getExceptionAccountingMsg(),company.getName()), IException.CONFIGURATION_ERROR);
		}
		
		// Test si les champs d'import sont configuré dans la société
		if(company.getInterbankPaymentOrderRejectImportPathCFONB() == null || company.getInterbankPaymentOrderRejectImportPathCFONB().isEmpty())  {
			throw new AxelorException(
					String.format("%s :\n Veuillez configurer un chemin pour le fichier d'imports des rejets de paiement par TIP et TIP chèque pour la société %s"
							,GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
		if(company.getTempInterbankPaymentOrderRejectImportPathCFONB() == null || company.getTempInterbankPaymentOrderRejectImportPathCFONB().isEmpty())  {
			throw new AxelorException(
					String.format("%s :\n Veuillez configurer un chemin pour le fichier des rejets de paiement par TIP et TIP chèque temporaire pour la société %s"
							,GeneralService.getExceptionAccountingMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
	}
	
	
}
