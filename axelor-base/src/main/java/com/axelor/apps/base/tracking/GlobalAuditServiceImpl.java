package com.axelor.apps.base.tracking;

import com.axelor.apps.base.db.GlobalTrackingConfigurationLine;
import com.axelor.apps.base.db.GlobalTrackingLog;
import com.axelor.apps.base.db.GlobalTrackingLogLine;
import com.axelor.apps.base.db.repo.GlobalTrackingLogRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.EntityHelper;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptBindings;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

public class GlobalAuditServiceImpl implements GlobalAuditService {

  protected final MetaModelRepository metaModelRepository;
  protected final GlobalTrackingLogRepository globalTrackingLogRepository;

  @Inject
  public GlobalAuditServiceImpl(
      MetaModelRepository metaModelRepository,
      GlobalTrackingLogRepository globalTrackingLogRepository) {
    this.metaModelRepository = metaModelRepository;
    this.globalTrackingLogRepository = globalTrackingLogRepository;
  }

  @Override
  public GlobalTrackingLog addLog(Model entity, int type) {

    GlobalTrackingLog log = new GlobalTrackingLog();
    log.setDateT(LocalDateTime.now());
    log.setMetaModelName(entity.getClass().getSimpleName());
    log.setMetaModel(metaModelRepository.findByName(entity.getClass().getSimpleName()));
    log.setTypeSelect(type);
    log.setUser(AuthUtils.getUser());
    log.setRelatedId(entity.getId());
    log.setRelatedReference(this.getRelatedReference(entity));
    log.setGlobalTrackingLogLineList(new ArrayList<>());

    return log;
  }

  protected String getRelatedReference(Model entity) {
    Mapper classMapper = Mapper.of(EntityHelper.getEntityClass(entity));

    if (classMapper.getNameField() != null && classMapper.getNameField().getName() != null) {
      String fieldName = classMapper.getNameField().getName();
      return (String) Mapper.toMap(entity).get(fieldName);
    }

    return "";
  }

  @Override
  public GlobalTrackingLog completeLog(
      GlobalTrackingLog log, User user, GlobalTrackingConfigurationLine configLine) {
    List<GlobalTrackingLogLine> logLinesToSave = new ArrayList<>();
    saveLogLines(log, logLinesToSave, configLine);
    persistLog(log, user, logLinesToSave);
    return log;
  }

  protected void saveLogLines(
      GlobalTrackingLog log,
      List<GlobalTrackingLogLine> logLinesToSave,
      GlobalTrackingConfigurationLine configLine) {
    ScriptBindings bindings = null;
    if ((CollectionUtils.isNotEmpty(log.getGlobalTrackingLogLineList()))) {
      try {
        bindings =
            new ScriptBindings(
                this.getContext(
                    JPA.find(
                        (Class<Model>) Class.forName(log.getMetaModel().getFullName()),
                        log.getRelatedId())));
      } catch (Exception e) {
      }
      persistLogLine(log, bindings, logLinesToSave, configLine);
    }
  }

  protected void persistLogLine(
      GlobalTrackingLog log,
      ScriptBindings bindings,
      List<GlobalTrackingLogLine> logLinesToSave,
      GlobalTrackingConfigurationLine configLine) {

    for (GlobalTrackingLogLine line : log.getGlobalTrackingLogLineList()) {
      if (configLine == null
          || !this.canTrack(configLine, log.getTypeSelect())
          || (!Strings.isNullOrEmpty(configLine.getTrackingCondition())
              && !Boolean.TRUE.equals(
                  new GroovyScriptHelper(bindings).eval(configLine.getTrackingCondition())))) {
        continue;
      }
      line.setMetaField(configLine.getMetaField());
      logLinesToSave.add(line);
    }
  }

  protected void persistLog(
      GlobalTrackingLog log, User user, List<GlobalTrackingLogLine> logLinesToSave) {
    if (!logLinesToSave.isEmpty()
        || GlobalTrackingLogRepository.TYPE_DELETE == log.getTypeSelect()) {
      log.getGlobalTrackingLogLineList().stream().forEach(l -> l.setGlobalTrackingLog(null));
      logLinesToSave.stream().forEach(l -> l.setGlobalTrackingLog(log));
      log.setUser(user);
      globalTrackingLogRepository.save(log);
    }
  }

  private Map<String, Object> getContext(Object obj)
      throws IntrospectionException,
          InvocationTargetException,
          IllegalAccessException,
          IllegalArgumentException {
    Map<String, Object> result = new HashMap<>();
    BeanInfo info = Introspector.getBeanInfo(obj.getClass());
    Method reader = null;
    for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
      reader = pd.getReadMethod();
      if (reader != null) {
        result.put(pd.getName(), reader.invoke(obj));
      }
    }
    return result;
  }

  protected boolean canTrack(GlobalTrackingConfigurationLine confLine, int typeSelect) {
    return switch (typeSelect) {
      case GlobalTrackingLogRepository.TYPE_CREATE -> confLine.getTrackCreation();
      case GlobalTrackingLogRepository.TYPE_READ -> confLine.getTrackReading();
      case GlobalTrackingLogRepository.TYPE_UPDATE -> confLine.getTrackUpdate();
      case GlobalTrackingLogRepository.TYPE_DELETE -> confLine.getTrackDeletion();
      case GlobalTrackingLogRepository.TYPE_EXPORT -> confLine.getTrackExport();
      default -> false;
    };
  }
}
