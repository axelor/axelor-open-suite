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
package com.axelor.apps.gdpr.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Anonymizer;
import com.axelor.apps.base.db.AnonymizerLine;
import com.axelor.apps.base.service.AnonymizeService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.gdpr.db.GDPRProcessingRegister;
import com.axelor.apps.gdpr.db.GDPRProcessingRegisterLog;
import com.axelor.apps.gdpr.db.GDPRProcessingRegisterRule;
import com.axelor.apps.gdpr.db.repo.GDPRProcessingRegisterLogRepository;
import com.axelor.apps.gdpr.db.repo.GDPRProcessingRegisterRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.AuditableModel;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.service.MailMessageService;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.schema.views.Selection;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoper;
import com.google.inject.servlet.ServletScopes;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class GdprProcessingRegisterService implements Callable<List<GDPRProcessingRegister>> {

  protected AnonymizeService anonymizeService;
  protected GDPRProcessingRegisterLogRepository processingLogRepo;
  protected GDPRProcessingRegisterRepository processingRegisterRepository;
  protected AppBaseService appBaseService;
  protected GdprAnonymizeService gdprAnonymizeService;
  protected List<GDPRProcessingRegister> gdprProcessingRegisters;

  @Inject
  public GdprProcessingRegisterService(
      AnonymizeService anonymizeService,
      GDPRProcessingRegisterLogRepository processingLogRepo,
      GDPRProcessingRegisterRepository processingRegisterRepository,
      AppBaseService appBaseService,
      GdprAnonymizeService gdprAnonymizeService) {
    this.anonymizeService = anonymizeService;
    this.processingLogRepo = processingLogRepo;
    this.processingRegisterRepository = processingRegisterRepository;
    this.appBaseService = appBaseService;
    this.gdprAnonymizeService = gdprAnonymizeService;
  }

  public void setGdprProcessingRegister(List<GDPRProcessingRegister> gdprProcessingRegisters) {
    this.gdprProcessingRegisters = gdprProcessingRegisters;
  }

  @Override
  public List<GDPRProcessingRegister> call() throws Exception {
    final RequestScoper scope = ServletScopes.scopeRequest(Collections.emptyMap());
    try (RequestScoper.CloseableScope ignored = scope.open()) {
      gdprProcessingRegisters =
          gdprProcessingRegisters.stream()
              .filter(
                  gdprProcessingRegister ->
                      gdprProcessingRegister.getStatus()
                          == GDPRProcessingRegisterRepository.PROCESSING_REGISTER_STATUS_ACTIVE)
              .collect(Collectors.toList());

      for (GDPRProcessingRegister gdprProcessingRegister : gdprProcessingRegisters) {
        gdprProcessingRegister = processingRegisterRepository.find(gdprProcessingRegister.getId());
        launchProcessingRegister(gdprProcessingRegister);
        Beans.get(MailMessageService.class)
            .sendNotification(
                AuthUtils.getUser(),
                I18n.get("Processing register - Archiving"),
                I18n.get("Processed finished"),
                gdprProcessingRegister.getId(),
                gdprProcessingRegister.getClass());
      }

      return gdprProcessingRegisters;
    } catch (Exception e) {
      onRunnerException(e);
      throw e;
    }
  }

  public void launchProcessingRegister(GDPRProcessingRegister gdprProcessingRegister)
      throws ClassNotFoundException, AxelorException, IOException {

    List<GDPRProcessingRegisterRule> gdprProcessingRegisterRuleList =
        gdprProcessingRegister.getGdprProcessingRegisterRuleList();

    Anonymizer anonymizer = gdprProcessingRegister.getAnonymizer();

    LocalDate calculatedDate =
        LocalDate.now().minusMonths(gdprProcessingRegister.getRetentionPeriod());

    int count = 0;

    for (GDPRProcessingRegisterRule gdprProcessingRegisterRule : gdprProcessingRegisterRuleList) {
      gdprProcessingRegisterRule =
          JPA.find(GDPRProcessingRegisterRule.class, gdprProcessingRegisterRule.getId());
      MetaModel metaModel = gdprProcessingRegisterRule.getMetaModel();

      Class<? extends AuditableModel> entityKlass =
          (Class<? extends AuditableModel>) Class.forName(metaModel.getFullName());
      String filter = computeFilter(gdprProcessingRegisterRule.getRule(), metaModel);

      AuditableModel model;

      List<Map> idsMap =
          Query.of(entityKlass)
              .order("id")
              .filter(filter)
              .bind("minDate", calculatedDate)
              .select("id")
              .fetch(0, 0);

      List<Long> ids =
          idsMap.stream().map(map -> (Long) map.get("id")).collect(Collectors.toList());

      for (Long id : ids) {
        model = Query.of(entityKlass).filter("id = :id").bind("id", id).fetchOne();
        model.setArchived(true);
        anonymize(metaModel, model, anonymizer);
        count++;

        if (anonymizer != null) {
          gdprAnonymizeService.anonymizeTrackingDatas(model);
        }

        if (count % 10 == 0) {
          JPA.clear();
          // Need to find if there are more than 10 entities
          if (anonymizer != null) {
            anonymizer = JPA.find(Anonymizer.class, anonymizer.getId());
          }
          metaModel = JPA.find(MetaModel.class, metaModel.getId());
        }
      }
    }
    if (count > 0) {
      addProcessingLog(gdprProcessingRegister, count);
    }
  }

  protected String computeFilter(String rule, MetaModel metaModel) {
    StringBuilder stringBuilder = new StringBuilder();
    String fields = rule.replaceAll("\\s", "");
    List<String> fieldList = Arrays.asList(fields.split(","));
    Iterator<String> iterator = fieldList.iterator();
    stringBuilder.append("(self.archived is null or self.archived is false) AND ");
    while (iterator.hasNext()) {
      stringBuilder.append("self.");
      stringBuilder.append(iterator.next());
      stringBuilder.append(" < :minDate");
      if (iterator.hasNext()) {
        stringBuilder.append(" AND ");
      }
    }

    // Exclude admin user
    if (User.class.getName().equals(metaModel.getFullName())) {
      stringBuilder.append(" AND self.code != 'admin'");
    }

    return stringBuilder.toString();
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void anonymize(MetaModel metaModel, AuditableModel model, Anonymizer anonymizer)
      throws AxelorException {

    if (anonymizer == null) {
      return;
    }

    // get list of anonymizer lines for metaModel
    List<AnonymizerLine> anonymizerLines =
        anonymizer.getAnonymizerLineList().stream()
            .filter(anonymizerline -> metaModel.equals(anonymizerline.getMetaModel()))
            .collect(Collectors.toList());

    Mapper mapper = Mapper.of(model.getClass());
    Object newValue;

    for (AnonymizerLine anonymizerLine : anonymizerLines) {
      Object currentValue = mapper.get(model, anonymizerLine.getMetaField().getName());
      Property property = mapper.getProperty(anonymizerLine.getMetaField().getName());
      if (Objects.isNull(currentValue)) continue;

      if (StringUtils.isEmpty(property.getSelection())) {
        newValue =
            anonymizeService.anonymizeValue(
                currentValue, property, anonymizerLine.getFakerApiField());
      } else {
        Selection.Option option = MetaStore.getSelectionList(property.getSelection()).get(0);
        newValue = option.getValue();
      }
      mapper.set(model, anonymizerLine.getMetaField().getName(), newValue);
    }
    JPA.merge(model);
  }

  @Transactional
  protected void addProcessingLog(GDPRProcessingRegister gdprProcessingRegister, int nbProcessed) {

    GDPRProcessingRegisterLog processingLog = new GDPRProcessingRegisterLog();

    gdprProcessingRegister = processingRegisterRepository.find(gdprProcessingRegister.getId());

    processingLog.setGdprProcessingRegister(gdprProcessingRegister);
    processingLog.setProcessingDateT(appBaseService.getTodayDateTime().toLocalDateTime());

    processingLog.setNbProcessed(nbProcessed);

    processingLogRepo.save(processingLog);
  }

  @Transactional
  protected void onRunnerException(Exception e) {
    TraceBackService.trace(e);
    Beans.get(MailMessageService.class)
        .sendNotification(
            AuthUtils.getUser(),
            I18n.get("Processing register - Archiving"),
            I18n.get("Error occurred"));
  }
}
