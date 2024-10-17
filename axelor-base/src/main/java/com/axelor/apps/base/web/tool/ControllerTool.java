package com.axelor.apps.base.web.tool;

import com.axelor.apps.base.db.Tag;
import com.axelor.apps.base.db.repo.TagRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.schema.actions.ActionView;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ControllerTool {

  public static Map<String, Object> createTagActionView(String packageName, String fieldModel) {
    ActionView.ActionViewBuilder builder =
        ActionView.define(I18n.get("Tags"))
            .model(Tag.class.getName())
            .add("grid", "tag-simplified-grid")
            .add("form", "tag-form");
    if (ObjectUtils.notEmpty(packageName)) {
      builder.domain(computeTagDomain(packageName)).context("_packageName", packageName);
    }
    if (ObjectUtils.notEmpty(fieldModel)) {
      builder.context("_fieldModel", fieldModel);
    }

    return builder.map();
  }

  protected static String computeTagDomain(String packageName) {
    List<MetaModel> metaModelList =
        Beans.get(MetaModelRepository.class).all().fetch().stream()
            .filter(mm -> mm.getPackageName().contains(packageName))
            .collect(Collectors.toList());
    List<Tag> tagList = Beans.get(TagRepository.class).all().fetch();
    String tagIds = "";

    for (MetaModel metaModel : metaModelList) {
      tagIds =
          tagIds.concat(
              tagList.stream()
                  .filter(tag -> tag.getConcernedModelSet().contains(metaModel))
                  .map(Tag::getId)
                  .map(Objects::toString)
                  .collect(Collectors.joining(",")));
    }

    return String.format("self.id IN (%s)", tagIds);
  }
}
