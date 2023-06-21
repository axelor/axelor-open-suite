/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.gdpr.service.response;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.gdpr.db.GDPRRequest;
import com.axelor.apps.gdpr.db.GDPRResponse;
import com.axelor.apps.gdpr.db.repo.GDPRRequestRepository;
import com.axelor.apps.gdpr.db.repo.GDPRResponseRepository;
import com.axelor.apps.gdpr.exception.GdprExceptionMessage;
import com.axelor.apps.gdpr.service.app.AppGdprService;
import com.axelor.auth.db.AuditableModel;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.message.db.Message;
import com.axelor.message.db.Template;
import com.axelor.message.service.MessageService;
import com.axelor.message.service.TemplateMessageService;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.util.*;
import wslite.json.JSONException;

public class GdprResponseAccessServiceImpl implements GdprResponseAccessService {

  protected GDPRResponseRepository gdprResponseRepository;
  protected MetaModelRepository metaModelRepository;
  protected GdprResponseService gdprResponseService;
  protected MessageService messageService;
  protected AppBaseService appBaseService;
  protected AppGdprService appGdprService;
  protected TemplateMessageService templateMessageService;
  protected GdprGenerateFilesService generateFilesService;

  @Inject
  public GdprResponseAccessServiceImpl(
      GDPRResponseRepository gdprResponseRepository,
      MetaModelRepository metaModelRepository,
      GdprResponseService gdprResponseService,
      MessageService messageService,
      AppBaseService appBaseService,
      AppGdprService appGdprService,
      TemplateMessageService templateMessageService,
      GdprGenerateFilesService generateFilesService) {
    this.gdprResponseRepository = gdprResponseRepository;
    this.metaModelRepository = metaModelRepository;
    this.gdprResponseService = gdprResponseService;
    this.messageService = messageService;
    this.appBaseService = appBaseService;
    this.appGdprService = appGdprService;
    this.templateMessageService = templateMessageService;
    this.generateFilesService = generateFilesService;
  }

  @SuppressWarnings("unchecked")
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void generateAccessResponseDataFile(GDPRRequest gdprRequest)
      throws AxelorException, ClassNotFoundException, IOException {

    GDPRResponse gdprResponse = new GDPRResponse();

    Class<? extends AuditableModel> modelSelectKlass =
        (Class<? extends AuditableModel>) Class.forName(gdprRequest.getModelSelect());

    AuditableModel selectedModel =
        Query.of(modelSelectKlass).filter("id = " + gdprRequest.getModelId()).fetchOne();

    MetaModel metaModel = metaModelRepository.findByName(modelSelectKlass.getSimpleName());

    gdprResponseService
        .getEmailFromPerson(selectedModel)
        .ifPresent(gdprResponse::setResponseEmailAddress);

    MetaFile dataMetaFile =
        generateFilesService.generateAccessResponseFile(
            gdprRequest, modelSelectKlass, metaModel, selectedModel);

    gdprResponse.setDataFile(dataMetaFile);
    gdprRequest.setGdprResponse(gdprResponse);
    gdprRequest.setStatusSelect(GDPRRequestRepository.REQUEST_STATUS_CONFIRMED);

    gdprResponseRepository.save(gdprResponse);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void sendEmailResponse(GDPRResponse gdprResponse) throws AxelorException {
    Template template = appGdprService.getAppGDPR().getAccessResponseTemplate();

    if (template == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(GdprExceptionMessage.MISSING_ACCESS_REQUEST_RESPONSE_MAIL_TEMPLATE));
    }

    Message message = generateAndSendMessage(gdprResponse, template);
    gdprResponse.setSendingDateT(appBaseService.getTodayDateTime().toLocalDateTime());
    gdprResponse.setResponseMessage(message);
  }

  protected Message generateAndSendMessage(GDPRResponse gdprResponse, Template template)
      throws AxelorException {
    try {
      Message message;
      message = templateMessageService.generateMessage(gdprResponse, template);
      Set<MetaFile> metaFileList = Sets.newHashSet();
      metaFileList.add(gdprResponse.getDataFile());
      messageService.attachMetaFiles(message, metaFileList);
      messageService.sendMessage(message);
      return message;
    } catch (ClassNotFoundException | IOException | JSONException e) {
      throw new AxelorException(
          e,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          GdprExceptionMessage.SENDING_MAIL_ERROR,
          e.getMessage());
    }
  }
}
