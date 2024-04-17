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
package com.axelor.apps.quality.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PrintingTemplate;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.printing.template.PrintingTemplatePrintService;
import com.axelor.apps.base.service.printing.template.model.PrintingGenFactoryContext;
import com.axelor.apps.quality.db.QIDecision;
import com.axelor.apps.quality.db.QIDecisionDistribution;
import com.axelor.apps.quality.db.QIResolution;
import com.axelor.apps.quality.db.QIResolutionDecision;
import com.axelor.apps.quality.db.QualityConfig;
import com.axelor.apps.quality.db.repo.QIDecisionDistributionRepository;
import com.axelor.apps.quality.service.config.QualityConfigService;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.mail.MessagingException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

public class QIResolutionServiceImpl implements QIResolutionService {

  protected QIDecisionDistributionRepository qiDecisionDistributionRepository;
  protected TemplateMessageService templateMessageService;
  protected MessageService messageService;
  protected PrintingTemplatePrintService printingTemplatePrintService;
  protected SequenceService sequenceService;
  protected MetaFiles metaFiles;
  protected QualityConfigService qualityConfigService;

  @Inject
  public QIResolutionServiceImpl(
      QIDecisionDistributionRepository qiDecisionDistributionRepository,
      TemplateMessageService templateMessageService,
      MessageService messageService,
      PrintingTemplatePrintService printingTemplatePrintService,
      SequenceService sequenceService,
      MetaFiles metaFiles,
      QualityConfigService qualityConfigService) {
    this.qiDecisionDistributionRepository = qiDecisionDistributionRepository;
    this.templateMessageService = templateMessageService;
    this.messageService = messageService;
    this.printingTemplatePrintService = printingTemplatePrintService;
    this.sequenceService = sequenceService;
    this.metaFiles = metaFiles;
    this.qualityConfigService = qualityConfigService;
  }

  @Override
  public List<QIDecisionDistribution> generateQIDecisionDistributions(QIResolution qiResolution)
      throws AxelorException, FileNotFoundException, IOException {

    Company company = qiResolution.getQi().getCompany();
    QualityConfig qualityConfig = qualityConfigService.getQualityConfig(company);
    Sequence qiDecisionDistributionSequence =
        qualityConfigService.getQiDecisionDistributionSequence(qualityConfig);

    List<QIResolutionDecision> qiResolutionDecisionList =
        qiResolution.getQiResolutionDecisionsList().stream()
            .filter(line -> line.isSelected())
            .collect(Collectors.toList());

    Map<Pair<QIDecision, Partner>, List<QIResolutionDecision>> map =
        qiResolutionDecisionList.stream()
            .collect(
                Collectors.groupingBy(
                    line -> Pair.of(line.getQiDecision(), line.getResponsiblePartner())));

    List<QIDecisionDistribution> qiDecisionDistributionList = new ArrayList<>();

    for (Map.Entry<Pair<QIDecision, Partner>, List<QIResolutionDecision>> entry : map.entrySet()) {
      Pair<QIDecision, Partner> pair = entry.getKey();
      List<QIResolutionDecision> qiResolutionDecisions = entry.getValue();
      qiDecisionDistributionList.add(
          createQIDecisionDistribution(
              company, pair, qiResolutionDecisions, qiResolution, qiDecisionDistributionSequence));
    }
    return qiDecisionDistributionList;
  }

  @Transactional(rollbackOn = Exception.class)
  protected QIDecisionDistribution createQIDecisionDistribution(
      Company company,
      Pair<QIDecision, Partner> pair,
      List<QIResolutionDecision> qiResolutionDecisions,
      QIResolution qiResolution,
      Sequence qiDecisionDistributionSequence)
      throws FileNotFoundException, AxelorException, IOException {
    QIDecisionDistribution qiDecisionDistribution = new QIDecisionDistribution();
    qiDecisionDistribution.setSequence(
        sequenceService.getSequenceNumber(
            qiDecisionDistributionSequence,
            QIDecisionDistribution.class,
            "sequence",
            qiDecisionDistribution));
    qiDecisionDistribution.setQiDecision(pair.getLeft());
    qiDecisionDistribution.setRecipientPartner(pair.getRight());
    qiDecisionDistribution.setQiResolution(qiResolution);

    if (!CollectionUtils.isEmpty(qiResolutionDecisions)) {
      qiDecisionDistribution.setRecipient(qiResolutionDecisions.get(0).getResponsible());
      qiDecisionDistribution.setQiResolutionDecisionSet(new HashSet<>(qiResolutionDecisions));
    }
    qiDecisionDistribution = qiDecisionDistributionRepository.save(qiDecisionDistribution);
    qiDecisionDistribution.setGeneratedFile(getGeneratedFile(company, qiDecisionDistribution));
    return qiDecisionDistribution;
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public void sendQIDecisionDistributions(
      List<QIDecisionDistribution> qiDecisionDistributionList,
      Template qiDecisionDistributionMessageTemplate)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException,
          AxelorException, IOException, MessagingException {
    for (QIDecisionDistribution qiDecisionDistribution : qiDecisionDistributionList) {
      qiDecisionDistribution =
          qiDecisionDistributionRepository.find(qiDecisionDistribution.getId());
      if (qiDecisionDistribution.getGeneratedFile() == null) {
        continue;
      }
      sendEmail(qiDecisionDistributionMessageTemplate, qiDecisionDistribution);
      qiDecisionDistribution.setDistributionSent(true);
      qiDecisionDistributionRepository.save(qiDecisionDistribution);
    }
  }

  public void sendEmail(Template template, QIDecisionDistribution qiDecisionDistribution)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException,
          AxelorException, IOException, MessagingException {
    Message message = templateMessageService.generateMessage(qiDecisionDistribution, template);
    message.addToEmailAddressSetItem(
        qiDecisionDistribution.getRecipientPartner().getEmailAddress());
    messageService.attachMetaFiles(
        message, Set.of(qiDecisionDistribution.getGeneratedFile().getMetaFile()));
    messageService.sendByEmail(message);
  }

  protected DMSFile getGeneratedFile(Company company, QIDecisionDistribution qiDecisionDistribution)
      throws AxelorException, FileNotFoundException, IOException {
    QualityConfig qualityConfig = qualityConfigService.getQualityConfig(company);
    PrintingTemplate qiDecisionDistributionPrintTemplate =
        qualityConfig.getQiDecisionDistributionPrintTemplate();

    if (qiDecisionDistributionPrintTemplate == null) {
      return null;
    }
    String fileName = getFileName(qiDecisionDistribution);
    File file =
        printingTemplatePrintService.getPrintFile(
            qiDecisionDistributionPrintTemplate,
            new PrintingGenFactoryContext(qiDecisionDistribution),
            fileName);

    DMSFile generatedFile = null;
    if (file != null) {
      try (InputStream is = new FileInputStream(file)) {
        generatedFile = metaFiles.attach(is, fileName, qiDecisionDistribution);
      }
    }
    return generatedFile;
  }

  protected String getFileName(QIDecisionDistribution qiDecisionDistribution) {
    return I18n.get("QI Decision distribution") + " " + qiDecisionDistribution.getSequence();
  }
}
