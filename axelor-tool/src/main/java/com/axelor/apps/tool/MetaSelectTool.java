package com.axelor.apps.tool;

import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaSelectItem;
import com.axelor.meta.db.repo.MetaSelectItemRepository;
import com.google.inject.Inject;
import java.util.Optional;

public class MetaSelectTool {
  protected MetaSelectItemRepository metaSelectItemRepo;

  @Inject
  public MetaSelectTool(MetaSelectItemRepository metaSelectItemRepo) {
    this.metaSelectItemRepo = metaSelectItemRepo;
  }

  public String getSelectTitle(String selection, int value) {
    return Optional.of(
            metaSelectItemRepo
                .all()
                .filter("self.select.name = :selection AND self.value = :value")
                .bind("selection", selection)
                .bind("value", value)
                .fetchOne())
        .map(MetaSelectItem::getTitle)
        .map(I18n::get)
        .orElse("");
  }
}
