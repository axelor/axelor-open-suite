package com.axelor.apps.quality.service.app;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.quality.db.QIActionDistribution;
import com.axelor.apps.quality.db.QIAnalysis;
import com.axelor.apps.quality.db.QITask;
import com.axelor.apps.quality.service.config.QualityConfigService;
import com.google.inject.Inject;
import java.util.HashSet;
import java.util.List;

public class QIActionDistributionServiceImpl implements QIActionDistributionService {

  protected QualityConfigService qualityConfigService;
  protected SequenceService sequenceService;

  @Inject
  public QIActionDistributionServiceImpl(
      QualityConfigService qualityConfigService, SequenceService sequenceService) {
    this.qualityConfigService = qualityConfigService;
    this.sequenceService = sequenceService;
  }

  @Override
  public QIActionDistribution createQIActionDistribution(
      QIAnalysis qiAnalysis, Company company, Partner responsiblePartner, List<QITask> qiTasks)
      throws AxelorException {
    QIActionDistribution qiActionDistribution =
        this.createQIActionDistribution(
            company, qiTasks.get(0).getResponsible(), responsiblePartner);
    qiActionDistribution.setQiDecision(qiTasks.get(0).getQiDecision());
    qiActionDistribution.setQiTaskSet(new HashSet<>(qiTasks));
    return qiActionDistribution;
  }

  @Override
  public QIActionDistribution createQIActionDistribution(
      Company company, Integer recipient, Partner recipientPartner) throws AxelorException {
    QIActionDistribution qiActionDistribution = new QIActionDistribution();
    Sequence qiActionDistributionSequence =
        qualityConfigService.getQiActionDistributionSequence(
            qualityConfigService.getQualityConfig(company));
    qiActionDistribution.setSequence(
        sequenceService.getSequenceNumber(
            qiActionDistributionSequence,
            QIActionDistribution.class,
            "sequence",
            qiActionDistribution));
    qiActionDistribution.setRecipient(recipient);
    qiActionDistribution.setRecipientPartner(recipientPartner);
    return qiActionDistribution;
  }
}
