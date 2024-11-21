/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.config;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.CfonbConfig;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.servlet.RequestScoped;

@RequestScoped
public class CfonbConfigService extends AccountConfigService {

  public CfonbConfig getCfonbConfig(AccountConfig accountConfig) throws AxelorException {
    CfonbConfig cfonbConfig = accountConfig.getCfonbConfig();
    if (cfonbConfig == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.CFONB_CONFIG_1),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          accountConfig.getCompany().getName());
    }
    return cfonbConfig;
  }

  public CfonbConfig getCfonbConfig(Company company) throws AxelorException {

    AccountConfig accountConfig = super.getAccountConfig(company);

    return this.getCfonbConfig(accountConfig);
  }

  /** ****************************** EXPORT CFONB ******************************************* */
  public String getSenderRecordCodeExportCFONB(CfonbConfig cfonbConfig) throws AxelorException {
    String senderRecordCodeExportCFONB = cfonbConfig.getSenderRecordCodeExportCFONB();
    if (senderRecordCodeExportCFONB == null || senderRecordCodeExportCFONB.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.CFONB_CONFIG_2),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          cfonbConfig.getName());
    }
    return senderRecordCodeExportCFONB;
  }

  public void getSenderNumExportCFONB(CfonbConfig cfonbConfig) throws AxelorException {
    if (cfonbConfig.getSenderNumExportCFONB() == null
        || cfonbConfig.getSenderNumExportCFONB().isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.CFONB_CONFIG_3),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          cfonbConfig.getName());
    }
  }

  public void getSenderNameCodeExportCFONB(CfonbConfig cfonbConfig) throws AxelorException {
    if (cfonbConfig.getSenderNameCodeExportCFONB() == null
        || cfonbConfig.getSenderNameCodeExportCFONB().isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.CFONB_CONFIG_4),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          cfonbConfig.getName());
    }
  }

  public void getRecipientRecordCodeExportCFONB(CfonbConfig cfonbConfig) throws AxelorException {
    if (cfonbConfig.getRecipientRecordCodeExportCFONB() == null
        || cfonbConfig.getRecipientRecordCodeExportCFONB().isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.CFONB_CONFIG_5),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          cfonbConfig.getName());
    }
  }

  public void getTotalRecordCodeExportCFONB(CfonbConfig cfonbConfig) throws AxelorException {
    if (cfonbConfig.getTotalRecordCodeExportCFONB() == null
        || cfonbConfig.getTotalRecordCodeExportCFONB().isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.CFONB_CONFIG_6),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          cfonbConfig.getName());
    }
  }

  public void getTransferOperationCodeExportCFONB(CfonbConfig cfonbConfig) throws AxelorException {
    if (cfonbConfig.getTransferOperationCodeExportCFONB() == null
        || cfonbConfig.getTransferOperationCodeExportCFONB().isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.CFONB_CONFIG_7),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          cfonbConfig.getName());
    }
  }

  public void getDirectDebitOperationCodeExportCFONB(CfonbConfig cfonbConfig)
      throws AxelorException {
    if (cfonbConfig.getDirectDebitOperationCodeExportCFONB() == null
        || cfonbConfig.getDirectDebitOperationCodeExportCFONB().isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.CFONB_CONFIG_8),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          cfonbConfig.getName());
    }
  }

  /** ****************************** IMPORT CFONB ******************************************* */
  public void getHeaderRecordCodeImportCFONB(CfonbConfig cfonbConfig) throws AxelorException {
    if (cfonbConfig.getHeaderRecordCodeImportCFONB() == null
        || cfonbConfig.getHeaderRecordCodeImportCFONB().isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.CFONB_CONFIG_9),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          cfonbConfig.getName());
    }
  }

  public void getDetailRecordCodeImportCFONB(CfonbConfig cfonbConfig) throws AxelorException {
    if (cfonbConfig.getDetailRecordCodeImportCFONB() == null
        || cfonbConfig.getDetailRecordCodeImportCFONB().isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.CFONB_CONFIG_10),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          cfonbConfig.getName());
    }
  }

  public void getEndingRecordCodeImportCFONB(CfonbConfig cfonbConfig) throws AxelorException {
    if (cfonbConfig.getEndingRecordCodeImportCFONB() == null
        || cfonbConfig.getEndingRecordCodeImportCFONB().isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.CFONB_CONFIG_11),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          cfonbConfig.getName());
    }
  }

  public void getTransferOperationCodeImportCFONB(CfonbConfig cfonbConfig) throws AxelorException {
    if (cfonbConfig.getTransferOperationCodeImportCFONB() == null
        || cfonbConfig.getTransferOperationCodeImportCFONB().isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.CFONB_CONFIG_12),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          cfonbConfig.getName());
    }
  }

  public void getDirectDebitOperationCodeImportCFONB(CfonbConfig cfonbConfig)
      throws AxelorException {
    if (cfonbConfig.getDirectDebitOperationCodeImportCFONB() == null
        || cfonbConfig.getDirectDebitOperationCodeImportCFONB().isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.CFONB_CONFIG_13),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          cfonbConfig.getName());
    }
  }
}
