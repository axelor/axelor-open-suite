package com.axelor.apps.base.service.research;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ResearchParameter;
import com.axelor.apps.base.db.ResearchParameterConfig;
import com.axelor.apps.base.db.ResearchRequest;
import com.axelor.apps.base.db.ResearchResultLine;
import com.axelor.apps.base.db.repo.ResearchParameterConfigRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.auth.db.AuditableModel;
import com.axelor.common.ObjectUtils;
import com.axelor.db.Query;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.google.inject.Inject;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public class ResearchRequestServiceImpl implements ResearchRequestService {

  protected ResearchParameterConfigRepository researchParameterConfigRepository;
  protected MetaModelRepository metaModelRepo;

  @Inject
  public ResearchRequestServiceImpl(
      ResearchParameterConfigRepository researchParameterConfigRepository,
      MetaModelRepository metaModelRepo) {
    this.researchParameterConfigRepository = researchParameterConfigRepository;
    this.metaModelRepo = metaModelRepo;
  }

  @Override
  public List<ResearchResultLine> searchObject(
      Map<String, Object> searchParams, ResearchRequest researchRequest) throws AxelorException {
    String models = researchRequest.getModels();
    List<ResearchResultLine> researchResultLineList = new ArrayList<>();
    String[] modelList = models.split(",");
    for (String modelStr : modelList) {

      try {
        modelStr = modelStr.trim();
        ResearchParameterConfig researchParameterConfig =
            researchParameterConfigRepository.all().filter("self.model = ?1 ", modelStr).fetchOne();
        if (researchParameterConfig == null) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(BaseExceptionMessage.ERROR_MISSING_RESEARCH_PARAMETER_CONFIGURATION));
        }
        researchResultLineList.addAll(
            bindDataUsingSearchConfig(searchParams, researchParameterConfig, researchRequest));

      } catch (ClassNotFoundException e) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get("Can not find object."));
      }
    }
    return researchResultLineList;
  }

  protected List<ResearchResultLine> bindDataUsingSearchConfig(
      Map<String, Object> searchParams,
      ResearchParameterConfig researchParameterConfig,
      ResearchRequest researchRequest)
      throws ClassNotFoundException {

    List<ResearchResultLine> results = new ArrayList<>();

    MetaModel metaModel = researchParameterConfig.getMetaModel();
    Class<? extends AuditableModel> modelClass =
        (Class<? extends AuditableModel>) Class.forName(metaModel.getFullName());
    Map<String, Object> searchParamsQuery = new HashMap<>();
    String query = buildSearchQuery(searchParamsQuery, searchParams, researchParameterConfig);

    //       apply search config query
    List<? extends AuditableModel> models =
        Query.of(modelClass).filter(query).bind(searchParamsQuery).fetch();

    models.forEach(
        model -> results.add(convertResultToLine(researchParameterConfig, model, researchRequest)));

    return results;
  }

  protected ResearchResultLine convertResultToLine(
      ResearchParameterConfig researchParameterConfig,
      AuditableModel model,
      ResearchRequest researchRequest) {
    ResearchResultLine researchResultLine = new ResearchResultLine();
    Map<String, Object> resultToDisplayMap =
        convertResultToDisplayMap(researchParameterConfig, model);
    researchResultLine.setOriginId((Long) resultToDisplayMap.get("objectId"));
    researchResultLine.setOriginTypeSelect(researchParameterConfig.getModel());
    researchResultLine.setOrigin((String) resultToDisplayMap.get("type"));
    if (researchRequest.getResearch1() != null) {
      researchResultLine.setResearch1Value(
          (String) resultToDisplayMap.get(researchRequest.getResearch1().getCode()));
    }
    if (researchRequest.getResearch2() != null) {
      researchResultLine.setResearch2Value(
          (String) resultToDisplayMap.get(researchRequest.getResearch2().getCode()));
    }
    if (researchRequest.getResearch3() != null) {
      researchResultLine.setResearch3Value(
          (String) resultToDisplayMap.get(researchRequest.getResearch3().getCode()));
    }
    if (researchRequest.getResearch4() != null) {
      researchResultLine.setResearch4Value(
          (String) resultToDisplayMap.get(researchRequest.getResearch4().getCode()));
    }
    if (researchRequest.getDateResearch1() != null) {
      researchResultLine.setDateResearch1Value(
          Date.valueOf(
                  (String) resultToDisplayMap.get(researchRequest.getDateResearch1().getCode()))
              .toLocalDate());
    }
    return researchResultLine;
  }

  protected String buildSearchQuery(
      Map<String, Object> searchParamsQuery,
      Map<String, Object> searchParams,
      ResearchParameterConfig searchConfig) {
    StringBuilder query = new StringBuilder();
    List<ResearchParameter> searchConfigLines = searchConfig.getResearchParameterList();

    for (ResearchParameter searchConfigLine : searchConfigLines) {
      String researchPrimaryKeyCode = searchConfigLine.getResearchPrimaryKey().getCode();
      Object param = Optional.ofNullable(searchParams.get(researchPrimaryKeyCode)).orElse(null);
      if (ObjectUtils.isEmpty(param)) {
        continue;
      }
      searchParamsQuery.put(searchConfigLine.getBinding(), param);
      query.append(searchConfigLine.getQuery());
      query.append(" AND ");
    }

    query.append(" 1 = 1");
    return query.toString();
  }

  /**
   * @param searchConfig
   * @param reference
   * @return
   */
  protected Map<String, Object> convertResultToDisplayMap(
      ResearchParameterConfig searchConfig, AuditableModel reference) {
    Context scriptContext = new Context(Mapper.toMap(reference), reference.getClass());
    Map<String, Object> mappedObject = new HashMap<>();

    mappedObject.put("type", I18n.get(reference.getClass().getSimpleName()));
    mappedObject.put("typeClass", reference.getClass().getName());
    mappedObject.put("objectId", reference.getId());

    for (ResearchParameter searchConfigLine : searchConfig.getResearchParameterList()) {
      mappedObject.put(
          searchConfigLine.getResearchPrimaryKey().getCode(),
          evalField(scriptContext, searchConfigLine.getMapping()));
    }

    return mappedObject;
  }

  protected String evalField(Context context, String fieldName) {
    String[] fields = fieldName.split("\\.");
    int count = 0;
    StringBuilder fieldToTest = new StringBuilder();
    Optional<Object> value = Optional.empty();

    while (count < fields.length) {
      if (fieldToTest.length() > 0) {
        fieldToTest.append(".");
      }
      fieldToTest.append(fields[count]);
      value = Optional.ofNullable(new GroovyScriptHelper(context).eval(fieldToTest.toString()));

      if (!value.isPresent()) {
        break;
      }
      count++;
    }

    return value.map(Object::toString).orElse("");
  }

  /**
   * get string value for given field name
   *
   * @param metaModel
   * @param mapper
   * @param reference
   * @param fieldName
   * @return
   * @throws AxelorException
   * @throws ClassNotFoundException
   */
  protected String getFieldValue(
      MetaModel metaModel, Mapper mapper, AuditableModel reference, String fieldName)
      throws AxelorException, ClassNotFoundException {

    String[] fields = fieldName.split("\\.", 2);

    Optional<Object> value = Optional.ofNullable(mapper.get(reference, fields[0]));

    if (fields.length == 1 || !value.isPresent()) {
      // simple case
      return value.map(Object::toString).orElse(StringUtils.EMPTY);
    } else {
      // handle subobject case
      MetaField metaField =
          metaModel.getMetaFields().stream()
              .filter(mf -> fields[0].equals(mf.getName()))
              .findFirst()
              .orElseThrow(
                  () ->
                      new AxelorException(
                          TraceBackRepository.CATEGORY_INCONSISTENCY,
                          I18n.get(BaseExceptionMessage.FIELD_NOT_FOUND)));

      String fullName = metaField.getPackageName() + "." + metaField.getTypeName();
      Class<? extends AuditableModel> subObjectClass =
          (Class<? extends AuditableModel>) Class.forName(fullName);

      MetaModel subMetaModel =
          metaModelRepo.all().filter("self.fullName = '" + fullName + "'").fetchOne();
      AuditableModel model = subObjectClass.cast(mapper.get(reference, fields[0]));
      Mapper subMapper = Mapper.of(subObjectClass);

      return getFieldValue(subMetaModel, subMapper, model, fields[1]);
    }
  }
}
