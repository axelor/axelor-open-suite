package com.axelor.apps.hr.service.dpae;

import com.axelor.app.AppSettings;
import com.axelor.apps.hr.db.DPAE;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.repo.DPAERepository;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.service.sendeddpae.SendedDPAEService;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.TemplateRepository;
import com.axelor.apps.message.service.MessageService;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.dms.db.DMSFile;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.threeten.bp.LocalDate;

public class DPAEServiceImpl implements DPAEService {

  protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("ddMMyyyy");

  protected static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("HHmm");

  @Inject protected MessageService messageService;

  @Inject protected SendedDPAEService sendedDPAEService;

  public void sendSingle(DPAE dpae) throws AxelorException {
    if (exportSingle(dpae) == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.DPAE_ALREADY_SEND),
          dpae);
    }
    Message message = sendMessageDPAE(dpae.getMetaFile());
    sendedDPAEService.createSendedDPAE(
        message, dpae.getMetaFile(), Arrays.asList(dpae.getEmployee()));
  }

  protected String exportSingle(DPAE dpae) throws AxelorException {
    if (dpae.getIsSended()) {
      return null;
    }
    List<String> listErrors = checkDPAEValidity(dpae);
    if (!listErrors.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          String.format(I18n.get(IExceptionMessage.DPAE_FIELD_INVALID), listErrors.toString()),
          dpae);
    }
    String textString = generateTextString(dpae);

    try {
      AppSettings appSettings = AppSettings.get();
      String exportDir = appSettings.get("data.export.dir");
      if (exportDir == null) {
        throw new IllegalArgumentException(I18n.get("Export directory is not configured."));
      }
      File file = new File(exportDir + "/" + dpae.getRegistrationDPAE() + ".txt");
      try (PrintWriter writer = new PrintWriter(file.getAbsoluteFile(), "UTF-8")) {
        writer.print(textString);
      }

      // Attach Meta File to DPAE
      try (InputStream is = new FileInputStream(file)) {
        DMSFile dmsFile = Beans.get(MetaFiles.class).attach(is, file.getName(), dpae);
        dpae.setMetaFile(dmsFile.getMetaFile());
      }
      dpae.setIsSended(true);
      return textString;
    } catch (Exception e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage(), dpae);
    }
  }

  protected Message sendMessageDPAE(MetaFile fileToSend) throws AxelorException {
    try {
      Template templateDPAE =
          Beans.get(AppHumanResourceService.class).getAppEmployee().getTemplateDPAE();
      templateDPAE = Beans.get(TemplateRepository.class).find(templateDPAE.getId());
      Message message =
          Beans.get(TemplateMessageService.class)
              .generateMessage(
                  1L, DPAE.class.getCanonicalName(), DPAE.class.getSimpleName(), templateDPAE);
      messageService.attachMetaFiles(message, ImmutableSet.of(fileToSend));
      message = Beans.get(MessageService.class).sendByEmail(message);
      return message;
    } catch (Exception e) {
      throw new AxelorException(
          DPAE.class, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
    }
  }

  public List<String> checkDPAEValidity(DPAE dpae) {

    List<String> errors = new ArrayList<>();

    if (Strings.isNullOrEmpty(dpae.getRegistrationCode())) {
      errors.add(I18n.get("Registration code"));
    }
    if (Strings.isNullOrEmpty(dpae.getMainActivityCode())) {
      errors.add(I18n.get("Main activity code"));
    }
    if (Strings.isNullOrEmpty(dpae.getCompanyFixedPhone())) {
      errors.add(I18n.get("Company fixed phone"));
    }
    if (Strings.isNullOrEmpty(dpae.getCompanyAddressL1())) {
      errors.add(I18n.get("Company address"));
    }
    if (Strings.isNullOrEmpty(dpae.getCompany().getName())) {
      errors.add(I18n.get("Company name"));
    }
    if (dpae.getDateOfHire() == null) {
      errors.add(I18n.get("Date of hire"));
    }
    if (dpae.getTimeOfHire() == null) {
      errors.add(I18n.get("Time of hire"));
    }
    if (dpae.getDepartmentOfBirth() == null) {
      errors.add(I18n.get("Department of birth"));
    }
    if (dpae.getDateOfBirth() == null) {
      errors.add(I18n.get("Date of birth"));
    }
    if (dpae.getSocialSecurityNumber() == null) {
      errors.add(I18n.get("Social security number"));
    }
    if (dpae.getSocialSecurityNumber() == null) {
      errors.add(I18n.get("Social security number"));
    }
    if (dpae.getFirstName() == null) {
      errors.add(I18n.get("First name"));
    }
    if (dpae.getLastName() == null) {
      errors.add(I18n.get("Last name"));
    }
    if (dpae.getContractTypeSelect() == DPAERepository.CDD && dpae.getEndDateOfContract() == null) {
      errors.add(I18n.get("End date of contract"));
    }
    if (dpae.getContractTypeSelect() == DPAERepository.CTT && dpae.getHealthServiceCode() == null) {
      errors.add(I18n.get("Health service code"));
    }

    return errors;
  }

  @Override
  public MetaFile sendMultiple(List<Long> dpaeIdList) throws AxelorException, IOException {
    DPAERepository dpaeRepo = Beans.get(DPAERepository.class);
    StringBuilder sb = new StringBuilder();
    String dpaeMessage;
    List<DPAE> dpaeList = new ArrayList<>();
    List<Employee> employeeList = new ArrayList<>();

    for (Long id : dpaeIdList) {
      DPAE dpae = dpaeRepo.find(id);
      dpaeList.add(dpae);
      dpaeMessage = exportSingle(dpae);
      if (dpaeMessage != null) {
        sb.append(dpaeMessage);
      }
    }
    // Empty String because all DPAE already send
    if (sb.length() == 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.DPAE_ALREADY_SEND),
          dpaeIdList);
    }
    AppSettings appSettings = AppSettings.get();
    String exportDir = appSettings.get("data.export.dir");
    if (exportDir == null) {
      throw new IllegalArgumentException(I18n.get("Export directory is not configured."));
    }
    File file = new File(exportDir + "/" + "dpae-" + LocalDate.now() + ".txt");
    try (PrintWriter writer = new PrintWriter(file.getAbsoluteFile(), "UTF-8")) {
      writer.print(sb.toString());
    }
    MetaFile metaFile = Beans.get(MetaFiles.class).upload(file);
    Message message = sendMessageDPAE(metaFile);
    employeeList = dpaeList.stream().map(DPAE::getEmployee).distinct().collect(Collectors.toList());
    sendedDPAEService.createSendedDPAE(message, metaFile, employeeList);
    return metaFile;
  }

  protected String generateTextString(DPAE dpae) {

    StringBuilder mainBuilder = new StringBuilder(990);
    StringBuilder tmpBuilder = new StringBuilder(917);

    // CATEGORIE ENTETE
    // L_IDENT_EMT
    mainBuilder.append(fillText(dpae.getRegistrationDPAE(), 14));
    // C_APPLICATION
    mainBuilder.append(fillText("DUE ", 4));
    // "Réservé"
    mainBuilder.append(fillText("", 16));
    // C_ADR_RET_AR
    mainBuilder.append(fillText("j.chrun@axelor.com", 30));

    // C_VERSION
    tmpBuilder.append(fillText("120", 3));
    // C_RET_AR_LOT
    tmpBuilder.append(fillText("", 2)); // retour

    // CATEGORIE DPAE
    // LIBRE
    tmpBuilder.append(fillText("", 15));
    tmpBuilder.append(fillText("", 15));
    tmpBuilder.append(fillText("", 14));
    // D_CREATION
    tmpBuilder.append(fillText("", 8));
    // H_CREATION
    tmpBuilder.append(fillText("", 4));
    // C_URSSAF
    tmpBuilder.append(fillText("", 3));
    // N_SIRET
    tmpBuilder.append(fillText(dpae.getRegistrationCode(), 14));
    // "Réservé"
    tmpBuilder.append(fillText("", 1));
    // L_RAISON_SOC_1
    tmpBuilder.append(fillText(dpae.getCompany().getName(), 32));
    // "Réservé"
    tmpBuilder.append(fillText("", 4));
    // L_ADR_EMP_1
    tmpBuilder.append(fillText(dpae.getCompanyAddressL1(), 32));
    // L_ADR_EMP_2
    tmpBuilder.append(fillText(dpae.getCompanyAddressL2(), 32));
    // C_POSTAL_EMP
    tmpBuilder.append(fillText(dpae.getCompanyZipCode(), 5));
    // L_BUR_DIST_EMP
    tmpBuilder.append(fillText(dpae.getCompanyCity(), 27));
    // L_NOM_PATRO_SAL
    tmpBuilder.append(fillText(dpae.getFirstName(), 32));
    // L_PRENOMS_SAL
    tmpBuilder.append(fillText(dpae.getLastName(), 32));
    // N_SECU_SOC
    tmpBuilder.append(fillText(dpae.getSocialSecurityNumber(), 13));
    // D_NAISSANCE_SAL
    tmpBuilder.append(fillText(dpae.getDateOfBirth().format(DATE_FORMATTER), 8));
    // L_LIEU_NAISS_SAL
    tmpBuilder.append(fillText(dpae.getCountryOfBirth(), 24));
    // "Réservé"
    tmpBuilder.append(fillText("", 20));
    // D_EMBAUCHE
    if (dpae.getDateOfHire() != null) {
      tmpBuilder.append(fillText(dpae.getDateOfHire().format(DATE_FORMATTER), 8));
    } else {
      tmpBuilder.append(fillText("", 8));
    }

    // H_EMBAUCHE
    if (dpae.getTimeOfHire() != null) {
      tmpBuilder.append(fillText(dpae.getTimeOfHire().format(HOUR_FORMATTER), 4));
    } else {
      tmpBuilder.append(fillText("", 4));
    }

    // R_DOSSIER
    tmpBuilder.append(fillText("", 5)); // retour
    // C_RETOUR_AR
    tmpBuilder.append(fillText("", 2)); // retour

    // CATEGORIE EMPLOYEUR
    // "Réservé"
    tmpBuilder.append(fillText("", 78));
    // L_RAISON_SOC_2
    tmpBuilder.append(fillText("", 32));
    // N_TEL_EMP
    tmpBuilder.append(fillText(dpae.getCompanyFixedPhone(), 11));
    // "Réservé"
    tmpBuilder.append(fillText("", 107));
    // C_ORIG_SAISIE
    tmpBuilder.append(fillText("I", 1)); // retour
    // C_NAF_NN
    tmpBuilder.append(fillText(dpae.getMainActivityCode(), 5));
    // "Réservé"
    tmpBuilder.append(fillText("", 33));

    // CATEGORIE SALARIE
    // L_NOM_EPX_SAL
    tmpBuilder.append(fillText(Optional.ofNullable(dpae.getMaritalName()).orElse(""), 32));
    // C_SEXE_SAL
    tmpBuilder.append(fillText(dpae.getSexSelect(), 1));
    // "Réservé"
    tmpBuilder.append(fillText("", 96));
    // C_DEPT_NAISS_SAL
    tmpBuilder.append(fillText(dpae.getDepartmentOfBirth(), 2));
    // "Réservé"
    tmpBuilder.append(fillText("", 35));

    // CATEGORIE CONTRAT
    // C_TYPE_CONTRAT
    tmpBuilder.append(fillText(dpae.getContractTypeSelect().toString(), 1));
    // D_FIN_CDD
    tmpBuilder.append(
        fillText(
            dpae.getContractTypeSelect().equals(1)
                ? dpae.getEndDateOfContract().format(DATE_FORMATTER)
                : "",
            8));
    // "Réservé"
    tmpBuilder.append(fillText("", 11));
    // N_JJ_ESSAI
    tmpBuilder.append(fillText(dpae.getTrialPeriodDuration(), 3));
    // "Réservé"
    tmpBuilder.append(fillText("", 50));
    // C_MT_DCL
    tmpBuilder.append(fillText(dpae.getHealthServiceCode(), 10)); // CODE CENTRE MEDECINE

    // CATEGORIE FORMALITE
    // "Réservé"
    tmpBuilder.append(fillText("", 23));
    // I_CERTIF
    tmpBuilder.append(fillText("", 1));
    // "Réservé"
    tmpBuilder.append(fillText("", 2));
    // I_IMMA
    tmpBuilder.append(fillText("", 1));
    // I_MT
    tmpBuilder.append(fillText("", 1));
    // I_PMF5
    tmpBuilder.append(fillText("", 1));

    // "Réservé"
    tmpBuilder.append(fillText("", 13));

    tmpBuilder.append("\n");

    // N_LG_MESS
    mainBuilder.append(
        fillText(String.valueOf(tmpBuilder.toString().replaceAll("\\s", "").length()), 8));
    mainBuilder.append(tmpBuilder.toString());

    return mainBuilder.toString();
  }

  protected String fillText(String data, int length) {

    if (Strings.isNullOrEmpty(data)) {
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
