/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.quality.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.birt.template.BirtTemplateService;
import com.axelor.apps.quality.db.QIActionDistribution;
import com.axelor.apps.quality.db.QIAnalysis;
import com.axelor.apps.quality.db.QITask;
import com.axelor.apps.quality.db.QualityConfig;
import com.axelor.apps.quality.db.repo.QIActionDistributionRepository;
import com.axelor.apps.quality.db.repo.QIAnalysisRepository;
import com.axelor.apps.quality.service.app.QIActionDistributionService;
import com.axelor.apps.quality.service.config.QualityConfigService;
import com.axelor.db.Model;
import com.axelor.dms.db.DMSFile;
import com.axelor.i18n.I18n;
import com.axelor.message.db.Message;
import com.axelor.message.db.Template;
import com.axelor.message.service.MessageService;
import com.axelor.message.service.TemplateMessageService;
import com.axelor.meta.MetaFiles;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.mail.MessagingException;
import org.apache.commons.collections.CollectionUtils;

public class QIAnalysisServiceImpl implements QIAnalysisService {

  protected QIActionDistributionRepository qiActionDistributionRepository;
  protected QualityConfigService qualityConfigService;
  protected QIActionDistributionService qiActionDistributionService;
  protected QIAnalysisRepository qiAnalysisRepository;
  protected BirtTemplateService birtTemplateService;
  protected TemplateMessageService templateMessageService;
  protected MessageService messageService;
  protected MetaFiles metaFiles;

  @Inject
  public QIAnalysisServiceImpl(
      QIActionDistributionRepository qiActionDistributionRepository,
      QualityConfigService qualityConfigService,
      QIActionDistributionService qiActionDistributionService,
      QIAnalysisRepository qiAnalysisRepository,
      BirtTemplateService birtTemplateService,
      TemplateMessageService templateMessageService,
      MessageService messageService,
      MetaFiles metaFiles) {
    this.qiActionDistributionRepository = qiActionDistributionRepository;
    this.qualityConfigService = qualityConfigService;
    this.qiActionDistributionService = qiActionDistributionService;
    this.qiAnalysisRepository = qiAnalysisRepository;
    this.birtTemplateService = birtTemplateService;
    this.templateMessageService = templateMessageService;
    this.messageService = messageService;
    this.metaFiles = metaFiles;
  }

  @Override
  public int setAdvancement(QIAnalysis qiAnalysis) {
    List<QITask> qiTasksList = qiAnalysis.getQiTasksList();
    if (CollectionUtils.isEmpty(qiTasksList)) {
      return 0;
    }
    int totalAdvancement =
        qiTasksList.stream().map(QITask::getAdvancement).reduce(Integer::sum).orElse(0);
    return totalAdvancement / qiTasksList.size();
  }

  @Override
  public List<QIActionDistribution> generateQIActionDistribution(QIAnalysis qiAnalysis)
      throws AxelorException, IOException {

    Company company = qiAnalysis.getQi().getCompany();

    List<QITask> qiTasksList =
        qiAnalysis.getQiTasksList().stream().filter(Model::isSelected).collect(Collectors.toList());

    Map<Partner, List<QITask>> qiTasksGroupedByPartner =
        qiTasksList.stream().collect(Collectors.groupingBy(QITask::getResponsiblePartner));
    qiAnalysis = qiAnalysisRepository.find(qiAnalysis.getId());

    List<QIActionDistribution> generatedQiActionDistributionList = new ArrayList<>();
    for (Map.Entry<Partner, List<QITask>> entry : qiTasksGroupedByPartner.entrySet()) {
      Partner responsiblePartner = entry.getKey();
      List<QITask> qiTasks = entry.getValue();
      createQIActionDistribution(
          qiAnalysis, company, generatedQiActionDistributionList, responsiblePartner, qiTasks);
    }
    return generatedQiActionDistributionList;
  }

  @Override
  public List<QIActionDistribution> generateQIActionDistributionForOthers(
      QIAnalysis qiAnalysis, Integer recepient, Partner recepientPartner)
      throws AxelorException, IOException {

    qiAnalysis = qiAnalysisRepository.find(qiAnalysis.getId());
    Company company = qiAnalysis.getQi().getCompany();

    List<QIActionDistribution> generatedQiActionDistributionListForOthers = new ArrayList<>();
    createQIActionDistributionForOthers(
        qiAnalysis,
        recepient,
        recepientPartner,
        company,
        generatedQiActionDistributionListForOthers);
    return generatedQiActionDistributionListForOthers;
  }

  @Transactional(rollbackOn = Exception.class)
  protected void createQIActionDistribution(
      QIAnalysis qiAnalysis,
      Company company,
      List<QIActionDistribution> generatedQiActionDistributionList,
      Partner responsiblePartner,
      List<QITask> qiTasks)
      throws AxelorException, IOException {
    QIActionDistribution qiActionDistribution =
        qiActionDistributionService.createQIActionDistribution(
            qiAnalysis, company, responsiblePartner, qiTasks);
    qiActionDistributionRepository.save(qiActionDistribution);
    generatedQiActionDistributionList.add(qiActionDistribution);
    qiAnalysis.addQiActionDistributionListItem(qiActionDistribution);
    qiActionDistribution.setGeneratedFile(getGeneratedFile(company, qiActionDistribution));
  }

  @Transactional(rollbackOn = Exception.class)
  protected void createQIActionDistributionForOthers(
      QIAnalysis qiAnalysis,
      Integer recipient,
      Partner recipientPartner,
      Company company,
      List<QIActionDistribution> generatedQiActionDistributionListForOthers)
      throws AxelorException, IOException {
    QIActionDistribution qiActionDistribution =
        qiActionDistributionService.createQIActionDistribution(
            company, recipient, recipientPartner);
    qiActionDistributionRepository.save(qiActionDistribution);
    generatedQiActionDistributionListForOthers.add(qiActionDistribution);
    qiAnalysis.addQiActionDistributionListItem(qiActionDistribution);
    qiActionDistribution.setGeneratedFile(getGeneratedFile(company, qiActionDistribution));
  }

  protected DMSFile getGeneratedFile(Company company, QIActionDistribution qiActionDistribution)
      throws AxelorException, IOException {
    QualityConfig qualityConfig = qualityConfigService.getQualityConfig(company);
    BirtTemplate qiActionDistributionBirtTemplate =
        qualityConfig.getQiActionDistributionBirtTemplate();
    if (qiActionDistributionBirtTemplate == null) {
      return null;
    }
    String fileName = getFileName(qiActionDistribution);
    File file =
        birtTemplateService.generateBirtTemplateFile(
            qiActionDistributionBirtTemplate, qiActionDistribution, fileName);
    DMSFile generatedFile = null;
    if (file != null) {
      try (InputStream is = new FileInputStream(file)) {
        generatedFile = metaFiles.attach(is, file.getName(), qiActionDistribution);
      }
    }
    return generatedFile;
  }

  protected String getFileName(QIActionDistribution qiActionDistribution) {
    return I18n.get("QI Action distribution") + " " + qiActionDistribution.getSequence();
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public void sendQIActionDistributions(
      List<QIActionDistribution> qiActionDistributionList,
      Template qiActionDistributionMessageTemplate)
      throws ClassNotFoundException, MessagingException {
    for (QIActionDistribution qiActionDistribution : qiActionDistributionList) {
      qiActionDistribution = qiActionDistributionRepository.find(qiActionDistribution.getId());
      if (qiActionDistribution.getGeneratedFile() == null) {
        continue;
      }
      sendEmail(qiActionDistributionMessageTemplate, qiActionDistribution);
      qiActionDistribution.setDistributionSent(true);
      qiActionDistributionRepository.save(qiActionDistribution);
    }
  }

  protected void sendEmail(Template template, QIActionDistribution qiActionDistribution)
      throws ClassNotFoundException, MessagingException {
    Message message = templateMessageService.generateMessage(qiActionDistribution, template);
    message.addToEmailAddressSetItem(qiActionDistribution.getRecipientPartner().getEmailAddress());
    messageService.attachMetaFiles(
        message, Set.of(qiActionDistribution.getGeneratedFile().getMetaFile()));
    messageService.sendByEmail(message);
  }
}
