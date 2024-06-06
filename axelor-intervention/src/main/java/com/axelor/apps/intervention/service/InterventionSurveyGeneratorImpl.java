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
package com.axelor.apps.intervention.service;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.intervention.db.AnswerType;
import com.axelor.apps.intervention.db.AnswerValue;
import com.axelor.apps.intervention.db.Equipment;
import com.axelor.apps.intervention.db.EquipmentFamily;
import com.axelor.apps.intervention.db.Intervention;
import com.axelor.apps.intervention.db.InterventionQuestion;
import com.axelor.apps.intervention.db.InterventionRange;
import com.axelor.apps.intervention.db.InterventionType;
import com.axelor.apps.intervention.db.Question;
import com.axelor.apps.intervention.db.Range;
import com.axelor.apps.intervention.db.RangeQuestion;
import com.axelor.apps.intervention.db.RangeType;
import com.axelor.apps.intervention.db.repo.AnswerValueRepository;
import com.axelor.apps.intervention.db.repo.InterventionQuestionRepository;
import com.axelor.apps.intervention.db.repo.InterventionRangeRepository;
import com.axelor.apps.intervention.db.repo.InterventionRepository;
import com.axelor.apps.intervention.db.repo.RangeQuestionRepository;
import com.axelor.apps.intervention.db.repo.RangeRepository;
import com.axelor.apps.intervention.db.repo.RangeTypeRepository;
import com.axelor.apps.intervention.exception.InterventionExceptionMessage;
import com.axelor.apps.intervention.repo.EquipmentRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.db.JPA;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.mail.MailConstants;
import com.axelor.mail.db.MailFlags;
import com.axelor.mail.db.MailFollower;
import com.axelor.mail.db.MailMessage;
import com.axelor.mail.db.repo.MailFollowerRepository;
import com.axelor.mail.db.repo.MailMessageRepository;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoper;
import com.google.inject.servlet.ServletScopes;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.LongConsumer;
import java.util.stream.Collectors;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterventionSurveyGeneratorImpl implements InterventionSurveyGenerator {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected final RangeQuestionRepository rangeQuestionRepository;
  protected final InterventionQuestionRepository interventionQuestionRepository;
  protected final RangeRepository rangeRepository;
  protected final EquipmentRepository equipmentRepository;
  protected final AnswerValueRepository answerValueRepository;
  protected final MailMessageRepository mailMessageRepository;
  protected final MailFollowerRepository mailFollowerRepository;
  protected final UserRepository userRepository;
  protected final InterventionRepository interventionRepository;
  protected static final int BATCH_SIZE = 20;

  protected Long rangeOrderSeq;
  protected Long questionOrderSeq;
  protected Long interventionId;
  protected Long interventionTypeId;
  protected List<Long> equipmentIds;
  protected List<Long> equipmentFamilyIds;
  protected List<Long> interventionQuestionIds;
  protected Long userId;
  protected final StockMoveRepository stockMoveRepository;
  protected final StockConfigService stockConfigService;
  protected final InterventionRangeRepository interventionRangeRepository;
  protected final InterventionRangeService interventionRangeService;
  protected final InterventionService interventionService;

  @Inject
  public InterventionSurveyGeneratorImpl(
      RangeQuestionRepository rangeQuestionRepository,
      InterventionQuestionRepository interventionQuestionRepository,
      RangeRepository rangeRepository,
      EquipmentRepository equipmentRepository,
      AnswerValueRepository answerValueRepository,
      MailMessageRepository mailMessageRepository,
      MailFollowerRepository mailFollowerRepository,
      UserRepository userRepository,
      StockMoveRepository stockMoveRepository,
      StockConfigService stockConfigService,
      InterventionRangeRepository interventionRangeRepository,
      InterventionRangeService interventionRangeService,
      InterventionService interventionService,
      InterventionRepository interventionRepository) {
    this.rangeQuestionRepository = rangeQuestionRepository;
    this.interventionQuestionRepository = interventionQuestionRepository;
    this.rangeRepository = rangeRepository;
    this.equipmentRepository = equipmentRepository;
    this.answerValueRepository = answerValueRepository;
    this.mailMessageRepository = mailMessageRepository;
    this.mailFollowerRepository = mailFollowerRepository;
    this.userRepository = userRepository;
    this.stockMoveRepository = stockMoveRepository;
    this.stockConfigService = stockConfigService;
    this.interventionRangeRepository = interventionRangeRepository;
    this.interventionRangeService = interventionRangeService;
    this.interventionService = interventionService;
    this.interventionRepository = interventionRepository;
  }

  @Override
  public Integer call() throws Exception {
    final RequestScoper scope = ServletScopes.scopeRequest(Collections.emptyMap());
    try (RequestScoper.CloseableScope ignored = scope.open()) {
      return process();
    }
  }

  @Override
  public void configure(Intervention intervention) {
    interventionId = intervention.getId();
    interventionTypeId = intervention.getInterventionType().getId();
    equipmentIds =
        intervention.getEquipmentSet().stream().map(Equipment::getId).collect(Collectors.toList());
    equipmentFamilyIds =
        intervention.getEquipmentSet().stream()
            .filter(Objects::nonNull)
            .map(Equipment::getEquipmentFamily)
            .filter(Objects::nonNull)
            .map(EquipmentFamily::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    this.userId = AuthUtils.getUser().getId();
  }

  protected Integer process() {
    try {
      start();
      generateSurvey();
      sendNotification(
          userId,
          I18n.get("Survey generation finished"),
          String.format(
              I18n.get("The survey of the intervention ID %s has been generated."), interventionId),
          interventionId,
          Intervention.class);
    } catch (Exception e) {
      onRunnerException(e);
    } finally {
      end();
    }
    return 0;
  }

  @Transactional(rollbackOn = Exception.class)
  protected void start() {
    Intervention intervention = interventionRepository.find(interventionId);
    intervention.setIsSurveyGenerationRunning(true);
    interventionRepository.save(intervention);
  }

  @Transactional(rollbackOn = Exception.class)
  protected void end() {
    Intervention intervention = interventionRepository.find(interventionId);
    intervention.setIsSurveyGenerationRunning(false);
    interventionRepository.save(intervention);
  }

  @Transactional
  protected void onRunnerException(Exception e) {
    TraceBackService.trace(e);
    sendNotification(
        AuthUtils.getUser().getId(),
        I18n.get(InterventionExceptionMessage.MESSAGE_ON_EXCEPTION),
        e.getMessage(),
        interventionId,
        Intervention.class);
  }

  @Transactional
  public void sendNotification(
      Long userId,
      String subject,
      String body,
      Long relatedId,
      Class<? extends Model> relatedModel) {
    User user = userRepository.find(userId);
    MailMessage message = new MailMessage();

    message.setSubject(subject);
    message.setBody(body);

    message.setAuthor(user);
    message.setType(MailConstants.MESSAGE_TYPE_COMMENT);

    if (relatedId != null && relatedModel != null) {
      message.setRelatedId(relatedId);
      message.setRelatedModel(relatedModel.getName());
    }

    MailFlags flags = new MailFlags();
    flags.setMessage(message);
    flags.setUser(user);
    flags.setIsRead(Boolean.FALSE);
    message.addFlag(flags);

    mailMessageRepository.save(message);

    if (relatedId == null || relatedModel == null) {
      return;
    }

    MailFollower follower = mailFollowerRepository.findOne(JPA.find(relatedModel, relatedId), user);
    if (follower != null && Boolean.FALSE.equals(follower.getArchived())) {
      return;
    }

    if (follower == null) {
      follower = new MailFollower();
    }

    follower.setArchived(false);
    follower.setRelatedId(relatedId);
    follower.setRelatedModel(relatedModel.getName());
    follower.setUser(user);

    mailFollowerRepository.save(follower);
  }

  protected void generateSurvey() {
    rangeOrderSeq = 0L;

    InterventionType type = JpaRepository.of(InterventionType.class).find(interventionTypeId);

    if (type == null) {
      return;
    }
    InterventionTypeRanges typeRanges = InterventionTypeRanges.of(type);

    generateQuestionsForRange(
        typeRanges.getAdvancedStartupMonitoringRangeId(), this::generateQuestionsForRange);
    generateQuestionsForRanges(typeRanges.getHeaderRangeIds(), this::generateQuestionsForRange);
    generateQuestionsForRanges(typeRanges.getEquipmentRangeIds(), this::generateQuestionsForRange);
    generateQuestionsForRanges(typeRanges.getFooterRangeIds(), this::generateQuestionsForRange);

    removeOrphanedInterventionEquipmentRanges();
    removeOrphanedInterventionFamilyRanges();
    interventionService.computeTag(interventionId);
  }

  protected void generateQuestionsForRanges(Collection<Long> rangeIds, LongConsumer consumer) {
    if (CollectionUtils.isNotEmpty(rangeIds)) {
      for (Long id : rangeIds) {
        generateQuestionsForRange(id, consumer);
      }
    }
  }

  protected void generateQuestionsForRange(Long rangeId, LongConsumer consumer) {
    if (rangeId != null) {
      questionOrderSeq = 0L;
      consumer.accept(rangeId);
      rangeOrderSeq++;
    }
  }

  protected void removeOrphanedInterventionEquipmentRanges() {
    CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
    CriteriaQuery<InterventionRange> cr = cb.createQuery(InterventionRange.class);
    Root<InterventionRange> root = cr.from(InterventionRange.class);
    cr.select(root);

    Predicate belongToIntervention = cb.equal(root.get("intervention").get("id"), interventionId);

    Predicate isRangeTypeEquipment =
        cb.equal(
            root.get("rangeVal").get("rangeType").get("rangeTypeSelect"),
            RangeTypeRepository.TYPE_BY_EQUIPMENT);
    Predicate hasNotEquipment;

    if (CollectionUtils.isNotEmpty(equipmentIds)) {
      hasNotEquipment =
          cb.and(
              root.get("equipment").isNotNull(),
              root.get("equipment").get("id").in(equipmentIds).not());
    } else {
      hasNotEquipment = root.get("equipment").isNotNull();
    }

    cr.where(cb.and(belongToIntervention, hasNotEquipment, isRangeTypeEquipment));

    deleteOrphanInterventionRange(cr);
  }

  protected void removeOrphanedInterventionFamilyRanges() {
    CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
    CriteriaQuery<InterventionRange> cr = cb.createQuery(InterventionRange.class);
    Root<InterventionRange> root = cr.from(InterventionRange.class);
    cr.select(root);

    Predicate belongToIntervention = cb.equal(root.get("intervention").get("id"), interventionId);
    Predicate isRangeTypeFamily =
        cb.equal(
            root.get("rangeVal").get("rangeType").get("rangeTypeSelect"),
            RangeTypeRepository.TYPE_BY_FAMILY);

    Predicate rangeFamily = cb.and(belongToIntervention, isRangeTypeFamily);

    if (CollectionUtils.isNotEmpty(equipmentFamilyIds)) {

      Predicate hasNotEquipmentFamily =
          root.join("rangeVal")
              .join("rangeType")
              .join("equipmentFamilySet")
              .get("id")
              .in(equipmentFamilyIds)
              .not();
      rangeFamily = cb.and(rangeFamily, hasNotEquipmentFamily);
    }
    cr.where(rangeFamily);

    deleteOrphanInterventionRange(cr);
  }

  protected void deleteOrphanInterventionRange(CriteriaQuery<InterventionRange> cr) {
    List<Long> interventionRangeIds =
        Optional.ofNullable(JPA.em().createQuery(cr).getResultList())
            .orElse(Collections.emptyList()).stream()
            .map(InterventionRange::getId)
            .collect(Collectors.toList());

    if (CollectionUtils.isNotEmpty(interventionRangeIds)) {
      long total = interventionRangeIds.size();
      log.info("Removing {} orphaned intervention ranges...", total);
      interventionRangeService.deleteInterventionRanges(interventionRangeIds);
    }
  }

  protected void generateQuestionsForRange(Long rangeId) {
    RangeType rangeType = rangeRepository.find(rangeId).getRangeType();

    List<Long> ids =
        rangeQuestionRepository
            .all()
            .filter("self.rangeVal.id = :rangeId")
            .bind("rangeId", rangeId)
            .fetchStream()
            .map(RangeQuestion::getId)
            .collect(Collectors.toList());

    long total = ids.size();
    long counter = 0L;

    if (rangeType != null
        && rangeType.getEquipmentFamilySet() != null
        && RangeTypeRepository.TYPE_BY_FAMILY.equals(rangeType.getRangeTypeSelect())) {
      List<Long> equipmentFamilyIds =
          equipmentIds.stream()
              .map(equipmentRepository::find)
              .map(Equipment::getEquipmentFamily)
              .filter(it -> rangeType.getEquipmentFamilySet().contains(it))
              .map(EquipmentFamily::getId)
              .distinct()
              .collect(Collectors.toList());
      if (CollectionUtils.isNotEmpty(equipmentFamilyIds)) {
        for (Long ignored : equipmentFamilyIds) {
          counter = generateQuestions(null, ids, total, counter);
        }
      }
    } else if (rangeType != null
        && rangeType.getEquipmentFamily() != null
        && RangeTypeRepository.TYPE_BY_EQUIPMENT.equals(rangeType.getRangeTypeSelect())) {
      List<Long> filteredEquipmentIds =
          equipmentIds.stream()
              .map(equipmentRepository::find)
              .filter(it -> rangeType.getEquipmentFamily().equals(it.getEquipmentFamily()))
              .map(Equipment::getId)
              .distinct()
              .collect(Collectors.toList());
      if (CollectionUtils.isNotEmpty(filteredEquipmentIds)) {
        for (Long equipmentId : filteredEquipmentIds) {
          counter = generateQuestions(equipmentId, ids, total, counter);
        }
      }
    } else {
      generateQuestions(null, ids, total, counter);
    }
  }

  protected long generateQuestions(Long equipmentId, List<Long> ids, long total, long counter) {
    for (List<Long> rangeQuestionIds : Lists.partition(ids, BATCH_SIZE)) {
      try {
        generateQuestions(equipmentId, rangeQuestionIds);
      } finally {
        JPA.clear();
        counter += rangeQuestionIds.size();
        log.info("Progress :: {}/{}", counter, total);
      }
    }
    return counter;
  }

  @Transactional(rollbackOn = Exception.class)
  protected void generateQuestions(Long equipmentId, List<Long> rangeQuestionIds) {
    Intervention intervention =
        Query.of(Intervention.class)
            .autoFlush(false)
            .filter("self.id = :id")
            .bind("id", interventionId)
            .fetchOne();
    Equipment equipment =
        equipmentId == null || equipmentId == 0L ? null : equipmentRepository.find(equipmentId);
    List<RangeQuestion> rangeQuestions =
        rangeQuestionRepository
            .all()
            .filter("self.id in :ids")
            .bind("ids", rangeQuestionIds)
            .fetch();
    if (CollectionUtils.isNotEmpty(rangeQuestions)) {
      for (RangeQuestion rangeQuestion :
          rangeQuestions.stream()
              .sorted(Comparator.comparing(RangeQuestion::getOrderSeq))
              .collect(Collectors.toList())) {
        generateOrUpdateInterventionQuestion(equipment, intervention, rangeQuestion);
      }
    }
  }

  @Transactional(rollbackOn = Exception.class)
  protected InterventionQuestion generateOrUpdateInterventionQuestion(
      Equipment equipment, Intervention intervention, RangeQuestion rangeQuestion) {
    Question question = rangeQuestion.getQuestion();
    AnswerType type = question.getAnswerType();
    Range range = rangeQuestion.getRangeVal();

    InterventionRange interventionRange =
        interventionRangeRepository.find(intervention, range, equipment);
    if (interventionRange == null) {
      interventionRange = new InterventionRange();
      interventionRange.setOrderSeq(rangeOrderSeq);
      interventionRange.setRangeVal(range);
      interventionRange.setIntervention(intervention);
      if (equipment != null) {
        interventionRange.setEquipment(equipment);
      }
      interventionRangeRepository.save(interventionRange);
    }

    InterventionQuestion interventionQuestion =
        interventionQuestionRepository.find(interventionRange, rangeQuestion.getSequence());
    if (interventionQuestion != null) {
      return interventionQuestion;
    }
    interventionQuestion = new InterventionQuestion();

    if (interventionRange.getInterventionQuestionList() == null
        || !interventionRange.getInterventionQuestionList().contains(interventionQuestion)) {
      interventionRange.addInterventionQuestionListItem(interventionQuestion);
    }

    interventionQuestion.setRangeQuestionSequence(rangeQuestion.getSequence());
    interventionQuestion.setOrderSeq(questionOrderSeq);
    questionOrderSeq++;
    interventionQuestion.setTitle(question.getTitle());
    interventionQuestion.setIndication(question.getIndication());
    interventionQuestion.setAnswerTypeSelect(type.getAnswerTypeSelect());
    for (AnswerValue answerValue : type.getAnswerValueList()) {
      interventionQuestion.addAnswerValueListItem(answerValueRepository.copy(answerValue, false));
    }
    interventionQuestion.setDefaultTextValue(type.getDefaultTextValue());
    interventionQuestion.setDesiredUnit(type.getDesiredUnit());
    interventionQuestion.setCheckboxName(type.getCheckboxName());
    interventionQuestion.setIndicationText(type.getIndicationText());
    interventionQuestion.setIsRequired(rangeQuestion.getIsRequired());
    interventionQuestion.setIsPrivate(rangeQuestion.getIsPrivate());
    interventionQuestion.setIsConditional(rangeQuestion.getIsConditional());
    interventionQuestion.setSummary(rangeQuestion.getSummary());

    if (Boolean.TRUE.equals(rangeQuestion.getIsConditional())) {
      if (rangeQuestion.getConditionalRangeQuestion() != null) {
        InterventionQuestion conditionalInterventionQuestion =
            interventionQuestionRepository.find(
                interventionRange, rangeQuestion.getConditionalRangeQuestion().getSequence());
        if (conditionalInterventionQuestion == null) {
          conditionalInterventionQuestion =
              generateOrUpdateInterventionQuestion(
                  equipment, intervention, rangeQuestion.getConditionalRangeQuestion());
        }
        interventionQuestion.setConditionalInterventionQuestion(conditionalInterventionQuestion);
      }
      if (rangeQuestion.getConditionalAnswerValueSet() != null) {
        for (AnswerValue answerValue : rangeQuestion.getConditionalAnswerValueSet()) {
          findInterventionQuestionAnswerValue(
                  interventionQuestion.getConditionalInterventionQuestion(), answerValue)
              .ifPresent(interventionQuestion::addConditionalAnswerValueSetItem);
        }
      }
    }
    return interventionQuestionRepository.save(interventionQuestion);
  }

  protected Optional<AnswerValue> findInterventionQuestionAnswerValue(
      InterventionQuestion conditionalInterventionQuestion, AnswerValue answerValue) {
    if (conditionalInterventionQuestion == null
        || conditionalInterventionQuestion.getAnswerValueList() == null) {
      return Optional.empty();
    }
    return conditionalInterventionQuestion.getAnswerValueList().stream()
        .filter(it -> it.getName().equals(answerValue.getName()))
        .findFirst();
  }
}
