/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.exception;

/**
 * Interface of Exceptions. Enum all exception of axelor-account.
 *
 * @author dubaux
 *
 */
public interface IExceptionMessage {


	static final String INVOICE_LINE_TAX_LINE = /*$$(*/ "Il manque une ligne de taxe"/*)*/ ;
	/**
	 * Bank statement service
	 */

	static final String BANK_STATEMENT_1 = /*$$(*/ "%s :\n Computed balance and Ending Balance must be equal" /*)*/ ;
	static final String BANK_STATEMENT_2 = /*$$(*/ "%s :\n MoveLine amount is not equals with bank statement line %s" /*)*/ ;
	static final String BANK_STATEMENT_3 = /*$$(*/ "%s :\n Bank statement line %s amount can't be null" /*)*/ ;

	/**
	 * Move service
	 */

	static final String NO_MOVES_SELECTED = /*$$(*/ "Please select 'Draft' or 'Simulated' moves" /*)*/ ;
	static final String MOVE_VALIDATION_NOT_OK = /*$$(*/ "Error in move validation, please check the log" /*)*/ ;
	static final String MOVE_VALIDATION_OK = /*$$(*/ "Moves validated successfully" /*)*/;


	/**
	 * Account management service
	 */
	static final String ACCOUNT_MANAGEMENT_1_ACCOUNT = /*$$(*/ "Accounting configuration is missing for Product: %s (company: %s)" /*)*/ ;

	/**
	 * Mail service
	 */
	static final String MAIL_1 = /*$$(*/ "%s :\n Please define an email template for cash register (company: %s)" /*)*/ ;

	/**
	 * Account clearance service and controller
	 */
	static final String ACCOUNT_CLEARANCE_1 = /*$$(*/ "%s :\n Veuillez configurer des informations comptables pour la société %s" /*)*/ ;
	static final String ACCOUNT_CLEARANCE_2 = /*$$(*/ "%s :\n Veuillez configurer un compte de profit pour la société %s" /*)*/ ;
	static final String ACCOUNT_CLEARANCE_3 = /*$$(*/ "%s :\n Veuillez configurer une taxe taux normal pour la société %s" /*)*/ ;
	static final String ACCOUNT_CLEARANCE_4 = /*$$(*/ "%s :\n Veuillez configurer les comptes d'apurements pour la société %s" /*)*/ ;
	static final String ACCOUNT_CLEARANCE_5 = /*$$(*/ "%s :\n Veuillez configurer une séquence Apurement des trop-perçus pour la société %s" /*)*/ ;
	static final String ACCOUNT_CLEARANCE_6 = /*$$(*/ "%s :\n Veuillez configurer un journal d'apurements des trop-perçus pour la société %s" /*)*/ ;
	static final String ACCOUNT_CLEARANCE_7 = /*$$(*/ "Lignes d'écriture générées" /*)*/ ;

	/**
	 * Account customer service
	 */
	static final String ACCOUNT_CUSTOMER_1 = /*$$(*/ "%s :\nCompte comptable Client manquant pour la société %s" /*)*/ ;
	static final String ACCOUNT_CUSTOMER_2 = /*$$(*/ "%s :\nCompte comptable Fournisseur manquant pour la société %s" /*)*/ ;

	/**
	 * Cash register line service
	 */
	static final String CASH_REGISTER_1 = /*$$(*/ "%s :\n Veuillez configurer une société active pour l'utilisateur %s" /*)*/ ;
	static final String CASH_REGISTER_2 = /*$$(*/ "%s :\n Une fermeture de caisse existe déjà pour la même date et la même caisse" /*)*/ ;
	static final String CASH_REGISTER_3 = /*$$(*/ "%s :\n Veuillez configurer une adresse email Caisses pour la société %s" /*)*/ ;

	/**
	 * Check rejection service
	 */
	static final String CHECK_REJECTION_1 = /*$$(*/ "%s :\n Veuillez configurer une séquence Rejet de chèque pour la société %s" /*)*/ ;

	/**
	 * Interbank payment order import service
	 */
	static final String INTER_BANK_PO_IMPORT_1 = /*$$(*/ "%s :\n La facture n°%s n'a pas été trouvée pour la société %s" /*)*/ ;

	/**
	 * Interbank payment order reject import service
	 */
	static final String INTER_BANK_PO_REJECT_IMPORT_1 = /*$$(*/ "%s \nAucune facture trouvée pour le numéro de facture %s et la société %s" /*)*/ ;
	static final String INTER_BANK_PO_REJECT_IMPORT_2 = /*$$(*/ "%s - Aucun mode de paiement configuré pour la facture %s" /*)*/ ;
	static final String INTER_BANK_PO_REJECT_IMPORT_3 = /*$$(*/ "%s :\n Le mode de paiement dont le code est 'TIC' n'a pas été trouvé" /*)*/ ;
	static final String INTER_BANK_PO_REJECT_IMPORT_4 = /*$$(*/ "%s :\n Le mode de paiement dont le code est 'TIP' n'a pas été trouvé" /*)*/ ;

	/**
	 * Irrecoverable service and controller
	 */
	static final String IRRECOVERABLE_1 = /*$$(*/ "Ligne d'échéancier %s" /*)*/ ;
	static final String IRRECOVERABLE_2 = /*$$(*/ "%s :\n Erreur généré lors de la création de l'écriture de passage en irrécouvrable %s" /*)*/ ;
	static final String IRRECOVERABLE_3 = /*$$(*/ "%s :\n La facture %s ne possède pas de pièce comptable dont le restant à payer est positif" /*)*/ ;
	static final String IRRECOVERABLE_4 = /*$$(*/ "%s :\n Veuillez configurer une séquence de Passage en irrécouvrable pour la société %s" /*)*/ ;
	static final String IRRECOVERABLE_5 = /*$$(*/ "Traitement terminé" /*)*/ ;
	static final String IRRECOVERABLE_6 = /*$$(*/ "anomalies générées" /*)*/ ;
	static final String IRRECOVERABLE_7 = /*$$(*/ "Veuillez selectionner un type d'impression" /*)*/ ;

	/**
	 * Journal service
	 */
	static final String JOURNAL_1 = /*$$(*/ "Type de facture absent de la facture %s" /*)*/ ;

	/**
	 * Move line export service
	 */
	static final String MOVE_LINE_EXPORT_1 = /*$$(*/ "%s :\n Erreur : Veuillez configurer une séquence Interface Vente pour la société %s" /*)*/ ;
	static final String MOVE_LINE_EXPORT_2 = /*$$(*/ "%s :\n Erreur : Veuillez configurer une séquence Interface Avoir pour la société %s" /*)*/ ;
	static final String MOVE_LINE_EXPORT_3 = /*$$(*/ "%s :\n Erreur : Veuillez configurer une séquence Interface Trésorerie pour la société %s" /*)*/ ;
	static final String MOVE_LINE_EXPORT_4 = /*$$(*/ "%s :\n Erreur : Veuillez configurer une séquence Interface Achat pour la société %s" /*)*/ ;

	/**
	 * Move line report service and controller
	 */
	static final String MOVE_LINE_REPORT_1 = /*$$(*/ "%s :\n Erreur : Veuillez configurer une séquence Reporting comptable pour la société %s" /*)*/ ;
	static final String MOVE_LINE_REPORT_2 = /*$$(*/ "%s :\n Erreur : Veuillez configurer une séquence Export comptable pour la société %s" /*)*/ ;
	static final String MOVE_LINE_REPORT_3 = /*$$(*/ "Lignes d'écritures récupérées" /*)*/ ;
	static final String MOVE_LINE_REPORT_4 = /*$$(*/ "Veuillez selectionner un type d'export" /*)*/ ;
	static final String MOVE_LINE_REPORT_6 = /*$$(*/ "Ecritures exportées" /*)*/ ;

	/**
	 * Move line service
	 */
	static final String MOVE_LINE_1 = /*$$(*/ "Tiers absent de la facture %s" /*)*/ ;
	static final String MOVE_LINE_2 = /*$$(*/ "Compte tiers absent de la facture %s" /*)*/ ;
	static final String MOVE_LINE_3 = /*$$(*/ "Produit absent de la ligne de facture, facture : %s (société : %s)" /*)*/ ;
	static final String MOVE_LINE_4 = /*$$(*/ "Compte comptable absent de la configuration pour la ligne : %s (société : %s)" /*)*/ ;
	static final String MOVE_LINE_5 = /*$$(*/ "Le compte analytique %s associé au compte comptable de vente pour le produit %s n'est pas configuré: (société : %s)" /*)*/ ;
	static final String MOVE_LINE_6 = /*$$(*/ "Compte comptable absent de la ligne de taxe : %s (société : %s)" /*)*/ ;

	/**
	 * Move service
	 */
	static final String MOVE_1 = /*$$(*/ "Type de facture absent de la facture %s" /*)*/ ;
	static final String MOVE_2 = /*$$(*/ "Veuillez selectionner un journal pour l'écriture" /*)*/ ;
	static final String MOVE_3 = /*$$(*/ "Veuillez selectionner une société pour l'écriture" /*)*/ ;
	static final String MOVE_4 = /*$$(*/ "Veuillez selectionner une période pour l'écriture" /*)*/ ;
	static final String MOVE_5 = /*$$(*/ "Le journal %s n'a pas de séquence d'écriture comptable configurée" /*)*/ ;
	static final String MOVE_6 = /*$$(*/ "Le sens de l'écriture comptable %s ne peut être déterminé" /*)*/ ;
	static final String MOVE_7 = /*$$(*/ "L'écriture comptable %s comporte un total débit différent du total crédit : %s <> %s" /*)*/ ;

	/**
	 * Payment schedule export service
	 */
	static final String PAYMENT_SCHEDULE_1 = /*$$(*/ "%s :\n Veuillez configurer un RIB pour l'échéancier de paiement %s" /*)*/ ;
	static final String PAYMENT_SCHEDULE_2 = /*$$(*/ "%s :\n Veuillez configurer un RIB pour le tiers %s" /*)*/ ;
	static final String PAYMENT_SCHEDULE_3 = /*$$(*/ "%s :\n Erreur : Veuillez configurer une date de prélèvement pour la configuration de batch %s" /*)*/ ;
	static final String PAYMENT_SCHEDULE_4 = /*$$(*/ "%s :\n Veuillez configurer une séquence de rejet des prélèvements\n pour la société %s pour le journal %s" /*)*/ ;
	static final String PAYMENT_SCHEDULE_5 = /*$$(*/ "Veuillez configurer une séquence Echéancier pour la société" /*)*/ ;
	static final String PAYMENT_SCHEDULE_6 = /*$$(*/ "%s :\n Erreur : Veuillez d'abord créer les lignes d'échéancier pour l'échéancier %s" /*)*/ ;

	/**
	 * Reconcile service
	 */
	static final String RECONCILE_1 = /*$$(*/ "%s :\nReconciliation : Merci de renseigner les lignes d'écritures concernées." /*)*/ ;
	static final String RECONCILE_2 = /*$$(*/ "%s :\nReconciliation : Les lignes d'écritures sélectionnées doivent concerner le même compte comptable." /*)*/ ;
	static final String RECONCILE_3 = /*$$(*/ "\n (Débit %s compte %s - Crédit %s compte %s)" /*)*/ ;
	static final String RECONCILE_4 = /*$$(*/ "%s :\nReconciliation %s: Le montant réconcilié doit être différent de zéro. \n (Débit %s compte %s - Crédit %s compte %s)" /*)*/ ;
	static final String RECONCILE_5 = /*$$(*/ "%s :\nReconciliation %s: Le montant réconcilié doit être inférieur ou égale au montant restant à réconcilier des lignes d'écritures." /*)*/ ;

	/**
	 * Reimbursement service and controller
	 */
	static final String REIMBURSEMENT_1 = /*$$(*/ "%s :\n Veuillez configurer une séquence Remboursement pour la société %s" /*)*/ ;
	static final String REIMBURSEMENT_2 = /*$$(*/ "Le dossier d'export des remboursement (format SEPA) n'est pas configuré pour la société %s." /*)*/ ;
	static final String REIMBURSEMENT_3 = /*$$(*/ "Aucun remboursement trouvé pour la ref %s et la société %s." /*)*/ ;
	static final String REIMBURSEMENT_4 = /*$$(*/ "Vous devez configurer un RIB." /*)*/ ;

	/**
	 * Year service
	 */
	static final String YEAR_1 = /*$$(*/ "%s :\n Veuillez renseigner une société pour l'année fiscale %s" /*)*/ ;

	/**
	 * Batch Account customer
	 */
	static final String BATCH_ACCOUNT_1 = /*$$(*/ "Situation comptable %s" /*)*/ ;
	static final String BATCH_ACCOUNT_2 = /*$$(*/ "Compte rendu de la détermination des soldes des comptes clients :\n" /*)*/ ;
	static final String BATCH_ACCOUNT_3 = /*$$(*/ "\t* %s Situation(s) compable(s) traitée(s)\n" /*)*/ ;
	static final String BATCH_ACCOUNT_4 = /*$$(*/ "Les soldes de %s situations comptables n'ont pas été mis à jour, merci de lancer le batch de mise à jour des comptes clients" /*)*/ ;
	static final String BATCH_ACCOUNT_5 = /*$$(*/ "Les soldes de l'ensemble des situations compables (%s) ont été mis à jour." /*)*/ ;

	/**
	 * Batch doubtful customer
	 */
	static final String BATCH_DOUBTFUL_1 = /*$$(*/ "Compte rendu de la détermination des créances douteuses" /*)*/ ;
	static final String BATCH_DOUBTFUL_2 = /*$$(*/ "\t* %s Facture(s) traitée(s)\n" /*)*/ ;

	/**
	 * Batch interbank payment order import
	 */
	static final String BATCH_INTERBANK_PO_IMPORT_1 = /*$$(*/ "Batch d'import des paiements par TIP et TIP chèque %s" /*)*/ ;
	static final String BATCH_INTERBANK_PO_IMPORT_2 = /*$$(*/ "Paiement de la facture %s" /*)*/ ;
	static final String BATCH_INTERBANK_PO_IMPORT_3 = /*$$(*/ "Compte rendu de l'import des paiements par TIP et TIP chèque :" /*)*/ ;
	static final String BATCH_INTERBANK_PO_IMPORT_4 = /*$$(*/ "%s paiement(s) traité(s)" /*)*/ ;
	static final String BATCH_INTERBANK_PO_IMPORT_5 = /*$$(*/ "Montant total" /*)*/ ;

	/**
	 * Batch interbank payment order reject import
	 */
	static final String BATCH_INTERBANK_PO_REJECT_IMPORT_1 = /*$$(*/ "Batch d'import des rejets de paiement par TIP et TIP chèque %s" /*)*/ ;
	static final String BATCH_INTERBANK_PO_REJECT_IMPORT_2 = /*$$(*/ "Rejet de paiement de la facture %s" /*)*/ ;
	static final String BATCH_INTERBANK_PO_REJECT_IMPORT_3 = /*$$(*/ "Compte rendu de l'import des rejets de paiement par TIP et TIP chèque" /*)*/ ;

	/**
	 * Batch move line export
	 */
	static final String BATCH_MOVELINE_EXPORT_1 = /*$$(*/ "%s :\n Erreur : Veuillez configurer une société pour le configurateur de batch %s" /*)*/ ;
	static final String BATCH_MOVELINE_EXPORT_2 = /*$$(*/ "%s :\n Erreur : Veuillez configurer une date de fin pour le configurateur de batch %s" /*)*/ ;
	static final String BATCH_MOVELINE_EXPORT_3 = /*$$(*/ "%s :\n Erreur : Veuillez configurer un type d'export pour le configurateur de batch %s" /*)*/ ;
	static final String BATCH_MOVELINE_EXPORT_4 = /*$$(*/ "Compte rendu du batch d'export des écritures :\n" /*)*/ ;
	static final String BATCH_MOVELINE_EXPORT_5 = /*$$(*/ "Lignes d'écritures (Ecritures) exportées" /*)*/ ;


	/**
	 * Batch payment schedule import/export
	 */
	static final String BATCH_PAYMENT_SCHEDULE_1 = /*$$(*/ "Type de donnée inconnu pour le traitement %s" /*)*/ ;
	static final String BATCH_PAYMENT_SCHEDULE_2 = /*$$(*/ "Batch d'export des prélèvements %s" /*)*/ ;
	static final String BATCH_PAYMENT_SCHEDULE_3 = /*$$(*/ "Prélèvement de l'échéance %s" /*)*/ ;
	static final String BATCH_PAYMENT_SCHEDULE_4 = /*$$(*/ "Compte rendu d'export des prélèvements factures :\n" /*)*/ ;
	static final String BATCH_PAYMENT_SCHEDULE_5 = /*$$(*/ "prélèvements(s) facture(s) traité(s)" /*)*/ ;
	static final String BATCH_PAYMENT_SCHEDULE_6 = /*$$(*/ "Compte rendu d'export des prélèvements de mensualité :\n" /*)*/ ;
	static final String BATCH_PAYMENT_SCHEDULE_7 = /*$$(*/ "prélèvements(s) mensualité(s) traité(s)" /*)*/ ;
	static final String BATCH_PAYMENT_SCHEDULE_8 = /*$$(*/ "%s :\n Aucun échéancier ou facture trouvé(e) pour le numéro de prélèvement : %s" /*)*/;
	static final String BATCH_PAYMENT_SCHEDULE_9 = /*$$(*/ "Rejet %s" /*)*/;
	static final String BATCH_PAYMENT_SCHEDULE_10 = /*$$(*/ "Création de l'écriture de rejet de l'échéance %s" /*)*/;
	static final String BATCH_PAYMENT_SCHEDULE_11 = /*$$(*/ "Création de l'écriture de rejet de la facture %s" /*)*/;
	static final String BATCH_PAYMENT_SCHEDULE_12 = /*$$(*/ "Compte rendu de l'import des rejets de prélèvement :\n" /*)*/;
	static final String BATCH_PAYMENT_SCHEDULE_13 = /*$$(*/ "prélèvement(s) rejeté(s)" /*)*/;

	/**
	 * Batch reimbursement export/import
	 */
	static final String BATCH_REIMBURSEMENT_1 = /*$$(*/ "Bug(Anomalie) généré(e)e dans l'export SEPA - Batch %s" /*)*/;
	static final String BATCH_REIMBURSEMENT_2 = /*$$(*/ "Compte rendu de création de remboursement :\n" /*)*/;
	static final String BATCH_REIMBURSEMENT_3 = /*$$(*/ "remboursement(s) créé(s)" /*)*/;
	static final String BATCH_REIMBURSEMENT_4 = /*$$(*/ "Compte rendu d'export de remboursement :\n" /*)*/;
	static final String BATCH_REIMBURSEMENT_5 = /*$$(*/ "remboursement(s) traité(s)" /*)*/;
	static final String BATCH_REIMBURSEMENT_6 = /*$$(*/ "Batch d'import des remboursements %s" /*)*/;
	static final String BATCH_REIMBURSEMENT_7 = /*$$(*/ "Rejet de remboursement %s" /*)*/;
	static final String BATCH_REIMBURSEMENT_8 = /*$$(*/ "Compte rendu de l'import des rejets de remboursement :\n" /*)*/;
	static final String BATCH_REIMBURSEMENT_9 = /*$$(*/ "remboursement(s) rejeté(s)" /*)*/;

	/**
	 * Batch reminder
	 */
	static final String BATCH_REMINDER_1 = /*$$(*/ "Compte rendu de relance :\n" /*)*/;
	static final String BATCH_REMINDER_2 = /*$$(*/ "tiers(s) traité(s)" /*)*/;

	/**
	 * Batch strategy
	 */
	static final String BATCH_STRATEGY_1 = /*$$(*/ "%s :\n Veuillez configurer un RIB pour le configurateur de batch %s" /*)*/;

	/**
	 * Cfonb export service
	 */
	static final String CFONB_EXPORT_1 = /*$$(*/ "Veuillez configurer un RIB pour le remboursement" /*)*/;
	static final String CFONB_EXPORT_2 = /*$$(*/ "%s :\n Erreur detectée pendant l'ecriture du fichier CFONB : %s" /*)*/;
	static final String CFONB_EXPORT_3 = /*$$(*/ "%s :\n Veuillez configurer un Code Guichet pour le RIB %s du tiers payeur %s" /*)*/;
	static final String CFONB_EXPORT_4 = /*$$(*/ "%s :\n Veuillez configurer un Numéro de compte pour le RIB %s du tiers payeur %s" /*)*/;
	static final String CFONB_EXPORT_5 = /*$$(*/ "%s :\n Veuillez configurer un Code Banque pour le RIB %s du tiers payeur %s" /*)*/;
	static final String CFONB_EXPORT_6 = /*$$(*/ "%s :\n Veuillez configurer une Adresse de Banque pour le RIB %s du tiers payeur %s" /*)*/;

	/**
	 * Cfonb import service
	 */
	static final String CFONB_IMPORT_1 = /*$$(*/ "%s :\n Veuillez configurer une Liste des codes motifs de rejet/retour relatifs aux Virements, Prélèvements et TIP dans l'administration générale" /*)*/;
	static final String CFONB_IMPORT_2 = /*$$(*/ "%s :\n Il manque un enregistrement en-tête dans le fichier %s" /*)*/;
	static final String CFONB_IMPORT_3 = /*$$(*/ "%s :\n Il manque un ou plusieurs enregistrements détail dans le fichier %s" /*)*/;
	static final String CFONB_IMPORT_4 = /*$$(*/ "%s :\n Il manque un enregistrement fin dans le fichier %s" /*)*/;
	static final String CFONB_IMPORT_5 = /*$$(*/ "%s :\n Le montant total de l'enregistrement suivant n'est pas correct (fichier %s) :\n %s" /*)*/;
	static final String CFONB_IMPORT_6 = /*$$(*/ "%s :\n Aucun mode de paiement trouvé pour le code %s et la société %s" /*)*/;

	/**
	 * Cfonb tool service
	 */
	static final String CFONB_TOOL_1 = /*$$(*/ "%s :\n Annomlie détectée (la valeur n'est pas numérique : %s) pour l'émetteur" /*)*/;
	static final String CFONB_TOOL_2 = /*$$(*/ "%s :\n Annomlie détectée (la valeur n'est pas numérique : %s) pour le destinataire" /*)*/;
	static final String CFONB_TOOL_3 = /*$$(*/ "%s :\n Annomlie détectée (la valeur n'est pas numérique : %s) pour le total" /*)*/;
	static final String CFONB_TOOL_4 = /*$$(*/ "%s :\n Annomlie détectée (l'enregistrement ne fait pas %s caractères : %s) pour l'enregistrement %s, société %s" /*)*/;

	/**
	 * Account config service
	 */
	static final String ACCOUNT_CONFIG_1 = /*$$(*/ "%s :\n Veuillez configurer les informations comptables pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_2 = /*$$(*/ "%s :\n Veuillez configurer un Dossier d'export des remboursements au format CFONB pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_3 = /*$$(*/ "%s :\n Veuillez configurer un Dossier d'export des prélèvements au format CFONB pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_4 = /*$$(*/ "%s :\n Veuillez configurer un chemin d'import des paiements par TIP et TIP + chèque pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_5 = /*$$(*/ "%s :\n Veuillez configurer un chemin d'import temporaire des paiements par TIP et TIP + chèque pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_6 = /*$$(*/ "%s :\n Veuillez configurer un chemin pour le fichier d'imports des rejets de paiement par TIP et TIP chèque pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_7 = /*$$(*/ "%s :\n Veuillez configurer un chemin pour le fichier des rejets de paiement par TIP et TIP chèque temporaire pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_8 = /*$$(*/ "%s :\n Veuillez configurer un chemin pour le fichier de rejet pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_9 = /*$$(*/ "%s :\n Veuillez configurer un chemin pour le fichier de rejet temporaire pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_10 = /*$$(*/ "%s :\n Veuillez configurer un chemin pour le fichier d'imports des rejets des remboursements pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_11 = /*$$(*/ "%s :\n Veuillez configurer un chemin pour le fichier temporaire d'imports des rejets des remboursements pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_12 = /*$$(*/ "%s :\n Veuillez configurer un journal de rejet pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_13 = /*$$(*/ "%s :\n Veuillez configurer un journal irrécouvrable pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_14 = /*$$(*/ "%s :\n Veuillez configurer un journal des achats Fourn. pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_15 = /*$$(*/ "%s :\n Veuillez configurer un journal des avoirs fournisseurs pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_16 = /*$$(*/ "%s :\n Veuillez configurer un journal des ventes clients pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_17 = /*$$(*/ "%s :\n Veuillez configurer un journal des avoirs clients pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_18 = /*$$(*/ "%s :\n Veuillez configurer un journal des O.D pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_19 = /*$$(*/ "%s :\n Veuillez configurer un journal de remboursement pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_20 = /*$$(*/ "%s :\n Veuillez configurer un type de journal ventes pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_21 = /*$$(*/ "%s :\n Veuillez configurer un type de journal avoirs pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_22 = /*$$(*/ "%s :\n Veuillez configurer un type de journal trésorerie pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_23 = /*$$(*/ "%s :\n Veuillez configurer un type de journal achats pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_24 = /*$$(*/ "%s :\n Veuillez configurer un compte de créance irrécouvrable pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_25 = /*$$(*/ "%s :\n Veuillez configurer un compte client pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_26 = /*$$(*/ "%s :\n Veuillez configurer un compte fournisseur pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_27 = /*$$(*/ "%s :\n Veuillez configurer un compte différence de caisse pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_28 = /*$$(*/ "%s :\n Veuillez configurer un compte de remboursement pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_29 = /*$$(*/ "%s :\n Veuillez configurer un compte client douteux pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_30 = /*$$(*/ "%s :\n Veuillez configurer un mode de paiement par prélèvement pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_31 = /*$$(*/ "%s :\n Veuillez configurer un mode de paiement après rejet pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_32 = /*$$(*/ "%s :\n Veuillez configurer un motif de passage en irrécouvrable pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_33 = /*$$(*/ "%s :\n Veuillez configurer un Chemin Fichier Exporté (si -> AGRESSO) pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_34 = /*$$(*/ "%s :\n Veuillez configurer les modèles de courrier Imports de rejet pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_35 = /*$$(*/ "%s :\n Veuillez configurer un Motif de passage (créance de plus de six mois) pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_36 = /*$$(*/ "%s :\n Veuillez configurer un Motif de passage (créance de plus de trois mois) pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_37 = /*$$(*/ "%s :\n Veuillez configurer le tableau de relance pour la société %s" /*)*/;
	static final String ACCOUNT_CONFIG_38 = /*$$(*/ "%s :\n Veuillez configurer un compte d'acompte pour la société %s" /*)*/;
	
	static final String ACCOUNT_CONFIG_SEQUENCE_1 = /*$$(*/ "%s :\n Please, configure a sequence for the customer invoices and the company %s" /*)*/;
	static final String ACCOUNT_CONFIG_SEQUENCE_2 = /*$$(*/ "%s :\n Please, configure a sequence for the customer refunds and the company %s" /*)*/;
	static final String ACCOUNT_CONFIG_SEQUENCE_3 = /*$$(*/ "%s :\n Please, configure a sequence for the supplier invoices and the company %s" /*)*/;
	static final String ACCOUNT_CONFIG_SEQUENCE_4 = /*$$(*/ "%s :\n Please, configure a sequence for the supplier refunds and the company %s" /*)*/;


	/**
	 * Cfonb config service
	 */
	static final String CFONB_CONFIG_1 = /*$$(*/ "%s :\n Veuillez configurer CFONB pour la société %s" /*)*/;
	static final String CFONB_CONFIG_2 = /*$$(*/ "%s :\n Veuillez configurer un Code enregistrement émetteur CFONB pour la société %s" /*)*/;
	static final String CFONB_CONFIG_3 = /*$$(*/ "%s :\n Veuillez configurer un Numéro d'émetteur CFONB pour la société %s" /*)*/;
	static final String CFONB_CONFIG_4 = /*$$(*/ "%s :\n Veuillez configurer un Nom/Raison sociale émetteur CFONB pour la société %s" /*)*/;
	static final String CFONB_CONFIG_5 = /*$$(*/ "%s :\n Veuillez configurer un Code enregistrement destinataire CFONB pour la société %s" /*)*/;
	static final String CFONB_CONFIG_6 = /*$$(*/ "%s :\n Veuillez configurer un Code enregistrement total CFONB pour la société %s" /*)*/;
	static final String CFONB_CONFIG_7 = /*$$(*/ "%s :\n Veuillez configurer un Code opération Virement CFONB pour la société %s" /*)*/;
	static final String CFONB_CONFIG_8 = /*$$(*/ "%s :\n Veuillez configurer un Code opération Prélèvement CFONB pour la société %s" /*)*/;
	static final String CFONB_CONFIG_9 = /*$$(*/ "%s :\n Veuillez configurer un Code enregistrement en-tête CFONB pour la société %s" /*)*/;
	static final String CFONB_CONFIG_10 = /*$$(*/ "%s :\n Veuillez configurer un Code enregistrement detail CFONB pour la société %s" /*)*/;
	static final String CFONB_CONFIG_11 = /*$$(*/ "%s :\n Veuillez configurer un Code enregistrement fin CFONB pour la société %s" /*)*/;
	static final String CFONB_CONFIG_12 = /*$$(*/ "%s :\n Veuillez configurer un Code opération Virement rejeté CFONB pour la société %s" /*)*/;
	static final String CFONB_CONFIG_13 = /*$$(*/ "%s :\n Veuillez configurer un Code opération Prélèvement impayé CFONB pour la société %s" /*)*/;
	static final String CFONB_CONFIG_14 = /*$$(*/ "%s :\n Veuillez configurer un Code opération TIP impayé CFONB pour la société %s" /*)*/;
	static final String CFONB_CONFIG_15 = /*$$(*/ "%s :\n Veuillez configurer un Code opération TIP + chèque CFONB pour la société %s" /*)*/;
	static final String CFONB_CONFIG_16 = /*$$(*/ "%s :\n Veuillez configurer un Code opération TIP CFONB pour la société %s" /*)*/;

	/**
	 * Paybox config service
	 */
	static final String PAYBOX_CONFIG_1 = /*$$(*/ "%s :\n Veuillez configurer Paybox pour la société %s" /*)*/;
	static final String PAYBOX_CONFIG_2 = /*$$(*/ "%s :\n Veuillez paramétrer un Numéro de site pour la configuration Paybox %s" /*)*/;
	static final String PAYBOX_CONFIG_3 = /*$$(*/ "%s :\n Veuillez paramétrer un Numéro de rang pour la configuration Paybox %s" /*)*/;
	static final String PAYBOX_CONFIG_4 = /*$$(*/ "%s :\n Veuillez paramétrer une Devise des transactions pour la configuration Paybox %s" /*)*/;
	static final String PAYBOX_CONFIG_5 = /*$$(*/ "%s :\n Veuillez paramétrer une Liste des variables à retourner par Paybox pour la configuration Paybox %s" /*)*/;
	static final String PAYBOX_CONFIG_6 = /*$$(*/ "%s :\n Veuillez paramétrer une Url retourner par Paybox une fois le paiement effectué pour la configuration Paybox %s" /*)*/;
	static final String PAYBOX_CONFIG_7 = /*$$(*/ "%s :\n Veuillez paramétrer une Url retourner par Paybox une fois le paiement refusé pour la configuration Paybox %s" /*)*/;
	static final String PAYBOX_CONFIG_8 = /*$$(*/ "%s :\n Veuillez paramétrer une Url retourner par Paybox une fois le paiement annulé pour la configuration Paybox %s" /*)*/;
	static final String PAYBOX_CONFIG_9 = /*$$(*/ "%s :\n Veuillez paramétrer un Identifiant interne pour la configuration Paybox %s" /*)*/;
	static final String PAYBOX_CONFIG_10 = /*$$(*/ "%s :\n Veuillez selectionner un Type d'algorithme de hachage utilisé lors du calcul de l'empreinte pour la configuration Paybox %s" /*)*/;
	static final String PAYBOX_CONFIG_11 = /*$$(*/ "%s :\n Veuillez paramétrer une Signature calculée avec la clé secrète pour la configuration Paybox %s" /*)*/;
	static final String PAYBOX_CONFIG_12 = /*$$(*/ "%s :\n Veuillez paramétrer une Url de l'environnement pour la configuration Paybox %s" /*)*/;
	static final String PAYBOX_CONFIG_13 = /*$$(*/ "%s :\n Veuillez paramétrer un Chemin de la clé publique Paybox pour la configuration Paybox %s" /*)*/;
	static final String PAYBOX_CONFIG_14 = /*$$(*/ "%s :\n Veuillez paramétrer un Email de back-office Axelor pour Paybox pour la configuration Paybox %s" /*)*/;

	/**
	 * Payer quality service
	 */
	static final String PAYER_QUALITY_1 = /*$$(*/ "%s :\n Erreur : Veuillez configurer un tableau des poids dans l'administration générale" /*)*/;

	/**
	 * Reminder action service
	 */
	static final String REMINDER_ACTION_1 = /*$$(*/ "Méthode de relance absente." /*)*/;
	static final String REMINDER_ACTION_2 = /*$$(*/ "Ligne de relance absente." /*)*/;
	static final String REMINDER_ACTION_3 = /*$$(*/ "%s : Modèle de courrier absent pour la matrice de relance %s (Tiers %s, Niveau %s)." /*)*/;

	/**
	 * Reminder service
	 */
	static final String REMINDER_1 = /*$$(*/ "Pas de situation comptable." /*)*/;
	static final String REMINDER_2 = /*$$(*/ "Date de reference non determinée." /*)*/;
	static final String REMINDER_3 = /*$$(*/ "Méthode de relance absente pour la configuration." /*)*/;
	static final String REMINDER_4 = /*$$(*/ "Niveau de relance en attente de validation." /*)*/;

	/**
	 * Reminder session service
	 */
	static final String REMINDER_SESSION_1 = /*$$(*/ "Ligne de method de relance absente." /*)*/;

	/**
	 * Invoice batch service
	 */
	static final String INVOICE_BATCH_1 = /*$$(*/ "Statut %s inconnu pour le traitement %s" /*)*/;

	/**
	 * Invoice generator
	 */
	static final String INVOICE_GENERATOR_1 = /*$$(*/ "%s :\nLe type de facture n'est pas rempli %s" /*)*/;
	static final String INVOICE_GENERATOR_2 = /*$$(*/ "%s :\nAucun tiers selectionné" /*)*/;
	static final String INVOICE_GENERATOR_3 = /*$$(*/ "%s :\nCondition de paiement absent" /*)*/;
	static final String INVOICE_GENERATOR_4 = /*$$(*/ "%s :\nMode de paiement absent" /*)*/;
	static final String INVOICE_GENERATOR_5 = /*$$(*/ "%s :\nAdresse de facturation absente" /*)*/;
	static final String INVOICE_GENERATOR_6 = /*$$(*/ "%s :\nDevise absente" /*)*/;

	/**
	 * Invoice line generator
	 */
	static final String INVOICE_LINE_GENERATOR_1 = /*$$(*/ "Veuillez selectionner une devise pour le tiers %s (%s)" /*)*/;
	static final String INVOICE_LINE_GENERATOR_2 = /*$$(*/ "Veuillez selectionner une devise pour la société %s" /*)*/;

	/**
	 * Batch validation
	 */
	static final String BATCH_VALIDATION_1 = /*$$(*/ "Compte rendu de la validation de facture :\n" /*)*/;
	static final String BATCH_VALIDATION_2 = /*$$(*/ "facture(s) validée(s)" /*)*/;

	/**
	 * Batch ventilation
	 */
	static final String BATCH_VENTILATION_1 = /*$$(*/ "Compte rendu de la ventilation de facture :\n" /*)*/;
	static final String BATCH_VENTILATION_2 = /*$$(*/ "facture(s) ventilée(s)" /*)*/;

	/**
	 * Cancel state
	 */
	static final String MOVE_CANCEL_1 = /*$$(*/ "Move should be unreconcile before to cancel the invoice" /*)*/;
	static final String MOVE_CANCEL_2 = /*$$(*/ "Move is ventilated on a closed period, and can't be canceled" /*)*/;
	static final String MOVE_CANCEL_3 = /*$$(*/ "So many accounting operations are used on this move, so move can't be canceled" /*)*/;
	
	static final String INVOICE_CANCEL_1 = /*$$(*/ "Invoice is passed in doubfult debit, and can't be canceled" /*)*/;


	/**
	 * Ventilate state
	 */
	static final String VENTILATE_STATE_1 = /*$$(*/ "La date de facture ou d'avoir ne peut être antérieure à la date de la dernière facture ventilée" /*)*/;
	static final String VENTILATE_STATE_2 = /*$$(*/ "La date de facture ou d'avoir ne peut être antérieure à la date de la dernière facture ventilée sur le mois" /*)*/;
	static final String VENTILATE_STATE_3 = /*$$(*/ "La date de facture ou d'avoir ne peut être antérieure à la date de la dernière facture ventilée sur l'année" /*)*/;
	static final String VENTILATE_STATE_4 = /*$$(*/ "La société %s n'a pas de séquence de facture ou d'avoir" /*)*/;

	/**
	 * Paybox service and controller
	 */
	static final String PAYBOX_1 = /*$$(*/ "%s :\n Veuillez paramétrer un Montant réglé pour la saisie paiement %s." /*)*/;
	static final String PAYBOX_2 = /*$$(*/ "%s :\n Le montant réglé pour la saisie paiement par CB ne doit pas être supérieur au solde du payeur." /*)*/;
	static final String PAYBOX_3 = /*$$(*/ "%s :\n Attention - Vous ne pouvez pas régler un montant supérieur aux factures selectionnées." /*)*/;
	static final String PAYBOX_4 = /*$$(*/ "%s :\n Veuillez paramétrer un Email pour le tiers %s." /*)*/;
	static final String PAYBOX_5 = /*$$(*/ "Paiement par Paybox" /*)*/;
	static final String PAYBOX_6 = /*$$(*/ "Paiement réalisé" /*)*/;
	static final String PAYBOX_7 = /*$$(*/ "Paiement échoué" /*)*/;
	static final String PAYBOX_8 = /*$$(*/ "Paiement annulé" /*)*/;
	static final String PAYBOX_9 = /*$$(*/ "Retour d'information de Paybox erroné" /*)*/;

	/**
	 * Payment mode service
	 */
	static final String PAYMENT_MODE_1 = /*$$(*/ "Compte comptable associé non configuré" /*)*/;
	static final String PAYMENT_MODE_2 = /*$$(*/ "%s :\n Erreur : Veuillez configurer une séquence pour la société %s et le mode de paiement %s" /*)*/;
	static final String PAYMENT_MODE_3 = /*$$(*/ "%s :\n Erreur : Veuillez configurer un journal pour la société %s et le mode de paiement %s" /*)*/;

	/**
	 * Payment voucher control service
	 */
	static final String PAYMENT_VOUCHER_CONTROL_1 = /*$$(*/ "%s :\n Attention, saisie paiement n° %s, le total des montants imputés par ligne est supérieur au montant payé par le client" /*)*/;
	static final String PAYMENT_VOUCHER_CONTROL_2 = /*$$(*/ "%s :\n Aucune ligne à payer." /*)*/;
	static final String PAYMENT_VOUCHER_CONTROL_3 = /*$$(*/ "%s :\n Veuillez renseigner un journal et un compte de trésorerie dans le mode de règlement." /*)*/;
	static final String PAYMENT_VOUCHER_CONTROL_4 = /*$$(*/ "%s :\n Le montant de la saisie paiement (%s) est différent du montant encaissé par Paybox (%s)" /*)*/;

	/**
	 * Payment voucher load service
	 */
	static final String PAYMENT_VOUCHER_LOAD_1 = /*$$(*/ "%s :\n Merci de renseigner le montant payé svp." /*)*/;

	/**
	 * Payment voucher sequence service
	 */
	static final String PAYMENT_VOUCHER_SEQUENCE_1 = /*$$(*/ "%s :\n Veuillez configurer une séquence Numéro de reçu (Saisie paiement) pour la société %s" /*)*/;

	/**
	 * Payment voucher tool service
	 */
	static final String PAYMENT_VOUCHER_TOOL_1 = /*$$(*/ "Type de la saisie paiement absent de la saisie paiement %s" /*)*/;

	/**
	 * Account chart controller
	 */
	static final String ACCOUNT_CHART_1 = /*$$(*/ "The chart of account has been loaded successfully" /*)*/;
	static final String ACCOUNT_CHART_2 = /*$$(*/ "Error in account chart import please check the log" /*)*/;
	static final String ACCOUNT_CHART_3 = /*$$(*/ "A chart or chart structure of accounts already exists, please delete the hierarchy between accounts in order to import a new chart." /*)*/;

	/**
	 * Address controller
	 */
	static final String ADDRESS_1 = /*$$(*/ "Sales map" /*)*/;
	static final String ADDRESS_2 = /*$$(*/ "Not implemented for OSM" /*)*/;

	/**
	 * Invoice controller
	 */
	static final String INVOICE_1 = /*$$(*/ "Facture annulée" /*)*/;
	static final String INVOICE_2 = /*$$(*/ "Avoir créé" /*)*/;
	static final String INVOICE_3 = /*$$(*/ "Please select the invoice(s) to print." /*)*/;
	static final String INVOICE_4 = /*$$(*/ "Refunds from invoice %s" /*)*/;

	/**
	 * Move template controller
	 */
	static final String MOVE_TEMPLATE_1 = /*$$(*/ "Template move is not balanced" /*)*/;
	static final String MOVE_TEMPLATE_2 = /*$$(*/ "Error in move generation" /*)*/;
	static final String MOVE_TEMPLATE_3 = /*$$(*/ "Generated moves" /*)*/;
	static final String MOVE_TEMPLATE_4 = /*$$(*/ "Please fill input lines" /*)*/;


	/**
	 *  Expense service
	 */
	static final String EXPENSE_JOURNAL = /*$$(*/ "Veuillez configurer un journal pour les notes de frais (société : %s)" /*)*/;
	static final String EXPENSE_ACCOUNT = /*$$(*/ "Veuillez configurer un compte pour les notes de frais (société : %s)" /*)*/;
	static final String EXPENSE_ACCOUNT_TAX = /*$$(*/ "Veuillez configurer un compte pour les taxes des notes de frais (société : %s)" /*)*/;
	static final String EXPENSE_CANCEL_MOVE = /*$$(*/ "Ecriture déjà utilisée, merci de délettrer d'abord" /*)*/;

	static final String EXPENSE_TAX_PRODUCT = /*$$(*/ "No Tax for the product %s" /*)*/;

	static final String USER_PARTNER = /*$$(*/ "Veuillez créer un contact pour l'utilisateur %s" /*)*/;
	
}