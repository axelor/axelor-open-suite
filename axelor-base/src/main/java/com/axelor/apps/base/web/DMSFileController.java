package com.axelor.apps.base.web;

import com.axelor.apps.base.db.repo.dms.CustomDMSFileRepository;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.dms.db.DMSFile;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import java.lang.reflect.Method;

public class DMSFileController {

  @Inject private MetaModelRepository metaModelRepo;

  @Inject private CustomDMSFileRepository dmsFileRepo;

  /** Open the record related to a DMS file */
  public void viewRelatedRecord(ActionRequest request, ActionResponse response) {
    DMSFile fileFromContext = request.getContext().asType(DMSFile.class);
    Long fileId = fileFromContext != null ? fileFromContext.getId() : null;

    if (fileId == null) {
      response.setError("Invalid file - no ID found");
      return;
    }

    DMSFile file = dmsFileRepo.find(fileId);

    if (file == null) {
      response.setError("File not found");
      return;
    }

    // Don't allow viewing related record for directories
    if (Boolean.TRUE.equals(file.getIsDirectory())) {
      response.setInfo("Directories don't have related records");
      return;
    }

    if (file.getRelatedModel() == null || file.getRelatedId() == null || file.getRelatedId() == 0) {
      response.setInfo("This file has no related record");
      return;
    }

    String model = file.getRelatedModel();
    Long id = file.getRelatedId();

    MetaModel metaModel = metaModelRepo.findByName(model);
    String title = metaModel != null ? metaModel.getFullName() : "View Record";

    // Close the current popup before opening the related record
    response.setCanClose(true);

    ActionView.ActionViewBuilder builder =
        ActionView.define(title).model(model).context("_showRecord", id);

    // Check if it's a Project
    if ("com.axelor.apps.project.db.Project".equals(model)) {
      try {
        @SuppressWarnings("unchecked")
        JpaRepository<? extends Model> repo =
            JpaRepository.of((Class<? extends Model>) Class.forName(model));
        Model record = repo.find(id);

        if (record == null) {
          response.setError("Related record not found");
          return;
        }

        Method getter = record.getClass().getMethod("getIsBusinessProject");
        Boolean isBusinessProject = (Boolean) getter.invoke(record);

        if (Boolean.TRUE.equals(isBusinessProject)) {
          builder.add("form", "business-project-form");
          builder.add("grid", "business-project-grid");
        } else {
          builder.add("form", "project-form");
          builder.add("grid", "project-grid");
        }
      } catch (Exception e) {
        e.printStackTrace();
        builder.add("form");
        builder.add("grid");
      }
    } else {
      builder.add("form");
      builder.add("grid");
    }
    response.setView(builder.map());
  }
}
