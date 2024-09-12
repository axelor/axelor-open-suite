package com.axelor.apps.base.web;

import com.axelor.apps.base.db.Tag;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.common.StringUtils;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;

public class TagController {

  @ErrorException
  public void setConcernedModelsDomain(ActionRequest request, ActionResponse response) {

    Tag tag = request.getContext().asType(Tag.class);
    Object packageName = request.getContext().get("_packageName");
    if (tag == null || packageName == null) {
      return;
    }

    response.setAttr(
        "concernedModelSet", "domain", "self.packageName like '%" + packageName + "%'");
  }

  public void setDefaultConcernedModel(ActionRequest request, ActionResponse response) {

    Tag tag = request.getContext().asType(Tag.class);
    Context parentContext = request.getContext().getParent();

    if (tag == null) {
      return;
    }

    if (parentContext != null && parentContext.get("_model") != null) {
      String fullNameModel = parentContext.get("_model").toString();
      addMetaModelToTag(tag, fullNameModel);
    }
    if (request.getContext().get("_fieldModel") != null) {
      addMetaModelToTag(tag, request.getContext().get("_fieldModel").toString());
    }

    response.setValue("concernedModelSet", tag.getConcernedModelSet());
  }

  protected void addMetaModelToTag(Tag tag, String fullName) {
    if (!StringUtils.isEmpty(fullName)) {
      MetaModel metaModel =
          Beans.get(MetaModelRepository.class)
              .all()
              .filter("self.fullName = ?", fullName)
              .fetchOne();

      if (metaModel != null) {
        tag.addConcernedModelSetItem(metaModel);
      }
    }
  }
}
