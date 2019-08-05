package com.axelor.apps.hr.service.dpae;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import com.axelor.apps.hr.db.DPAE;
import com.google.common.base.Strings;

public class DPAEServiceImpl implements DPAEService {

  protected static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("ddMMyyyy");

  protected static final DateTimeFormatter HOUR_FORMATTER =
      DateTimeFormatter.ofPattern("HHmm");

  @Override
  public void exportSingle(DPAE dpae) {

    
    
    String textString = generateTextString(dpae);
    
    // write in file
    
    System.out.println(textString);
    
    

    
    //dpae.setMetaFile( file );

  }
  
  
  public List<String> checkDPAEValidity(DPAE dpae){
    
    List<String> errors = new ArrayList<String>();
    
    if (Strings.isNullOrEmpty( dpae.getRegistrationCode() ) ) {
      errors.add("Registration code");
    }
    if (Strings.isNullOrEmpty( dpae.getCompany().getName() )) {
      errors.add("Company name");
    }
    
    
    return errors;
  }

  @Override
  public void exportMultiple(List<Long> dpaeIdList) {}
  
  

  protected String generateTextString(DPAE dpae) {

    StringBuilder builder = new StringBuilder(990);

    // L_IDENT_EMT
    builder.append(fillText("", 14)); // ??

    // C_APPLICATION
    builder.append(fillText("DUE ", 4));

    // "Réservé"
    builder.append(fillText("", 15));
    builder.append(fillText("U", 1));
    builder.append(fillText("", 30));

    // N_LG_MESS
    builder.append(fillText("912", 8));

    // C_VERSION
    builder.append(fillText("TST", 3)); // "TST" pour test, "120" pour exploitation

    // C_RET_AR_LOT
    builder.append(fillText("", 30)); // retour

    // D_CREATION
    builder.append(fillText(LocalDate.now().format(DATE_FORMATTER), 8));

    // H_CREATION
    builder.append(fillText(LocalDateTime.now().format(HOUR_FORMATTER), 4));

    // C_URSSAF
    builder.append(fillText("", 3)); // ?? voir list des codes URSSAF 

    // N_SIRET
    builder.append(fillText(dpae.getRegistrationCode(), 14));

    // "Réservé"
    builder.append(fillText("", 1));

    // L_RAISON_SOC_1
    builder.append(fillText(dpae.getCompany().getName(), 32));

    // "Réservé"
    builder.append(fillText("", 4));

    // L_ADR_EMP_1
    builder.append(fillText(dpae.getCompanyAddressL1(), 32));

    // L_ADR_EMP_2
    builder.append(fillText(dpae.getCompanyAddressL2(), 32));

    // C_POSTAL_EMP
    builder.append(fillText(dpae.getCompanyZipCode(), 5));

    // L_BUR_DIST_EMP
    builder.append(fillText(dpae.getCompanyCity(), 27));

    // L_NOM_PATRO_SAL
    builder.append(fillText(dpae.getFirstName(), 32));
    // L_PRENOMS_SAL
    builder.append(fillText(dpae.getLastName(), 32));
    // N_SECU_SOC
    builder.append(fillText(dpae.getSocialSecurityNumber(), 13));
    // D_NAISSANCE_SAL
    builder.append(fillText(dpae.getDateOfBirth().format(DATE_FORMATTER), 8));
    // L_LIEU_NAISS_SAL
    builder.append(fillText(dpae.getCountryOfBirth(), 24));
    // "Réservé"
    builder.append(fillText("", 20));
    // D_EMBAUCHE
    builder.append(fillText(dpae.getDateOfHire().format(DATE_FORMATTER), 8));
    // H_EMBAUCHE
    builder.append(fillText(dpae.getTimeOfHire().format(HOUR_FORMATTER), 4));
    // R_DOSSIER
    builder.append(fillText("", 5)); // retour
    // C_RETOUR_AR
    builder.append(fillText("", 2)); // retour

    // "Réservé"
    builder.append(fillText("", 78));
    // L_RAISON_SOC_2
    builder.append(fillText("", 32));
    // N_TEL_EMP
    builder.append(fillText(dpae.getCompanyFixedPhone(), 11));
    // "Réservé"
    builder.append(fillText("", 107));
    // C_ORIG_SAISIE
    builder.append(fillText("", 1)); // retour
    // C_NAF_NN
    builder.append(fillText(dpae.getMainActivityCode(), 5));
    // "Réservé"
    builder.append(fillText("", 33));
    // L_NOM_EPX_SAL
    builder.append(fillText(Optional.ofNullable(dpae.getMaritalName()).orElse(""), 32));
    // C_SEXE_SAL
    builder.append(fillText(dpae.getSexSelect(), 1));
    // "Réservé"
    builder.append(fillText("", 96));
    // C_DEPT_NAISS_SAL
    builder.append(fillText(dpae.getDepartmentOfBirth(), 2));
    // "Réservé"
    builder.append(fillText("", 35));

    // C_TYPE_CONTRAT
    builder.append(fillText(dpae.getContractTypeSelect().toString(), 1));
    // D_FIN_CDD
    builder.append(
        fillText(
            dpae.getContractTypeSelect().equals(1)
                ? dpae.getEndDateOfContract().format(DATE_FORMATTER)
                : "",
            8));
    // "Réservé"
    builder.append(fillText("", 11));
    // N_JJ_ESSAI
    builder.append(fillText(dpae.getTrialPeriodDuration(), 3));
    // "Réservé"
    builder.append(fillText("", 50));

    // C_MT_DCL
    builder.append(fillText("", 10)); // CODE CENTRE MEDECINE

    // "Réservé"
    builder.append(fillText("", 23));
    // I_CERTIF
    builder.append(fillText("", 1));
    // "Réservé"
    builder.append(fillText("", 2));
    // I_IMMA
    builder.append(fillText("", 1));
    // I_MT
    builder.append(fillText("", 1));
    // I_PMF5
    builder.append(fillText("", 1));

    // "Réservé"
    builder.append(fillText("", 13));

    return builder.toString();
  }

  protected String fillText(String data, int length) {
    
    if (Strings.isNullOrEmpty(data) ) {
      return StringUtils.repeat(' ', length);
    }

    if (data.length() > length) {
      return data.substring(0, length);
    }

    int diff = length - data.length();
    if (diff > 0) {
      data += StringUtils.repeat(' ', diff);
    }

    return data;
  }
}
