package com.axelor.apps.businessproject.service;

import com.axelor.dms.db.DMSFile;
import com.axelor.meta.db.MetaFile;

public interface ProjectFilesService {
  /**
   * Attaches a MetaFile to any model instance by creating a DMSFile link.
   *
   * @param modelId The ID of the model to attach the file to
   * @param modelClassName Fully qualified class name of the model
   * @param metaFile The MetaFile to attach
   * @throws IllegalArgumentException if the model is not found
   * @throws IllegalStateException if the MetaFile is not persisted
   */
  void attachMetaFileToModel(Long modelId, String modelClassName, MetaFile metaFile);

  /**
   * Checks whether a file with the given name already exists in the project files folder.
   *
   * @param projectFilesHome the DMS home folder of the project
   * @param filename the filename to check for
   * @return true if a file with that name already exists, false otherwise
   */
  boolean fileExistsInProjectFiles(DMSFile projectFilesHome, String filename);

  /**
   * Renames a MetaFile to a non-colliding name within the project files folder. Appends an
   * incrementing suffix based on existing files.
   *
   * @param metaFile the MetaFile to rename
   * @param projectFilesHome the home folder of the project to check for existing files
   * @return the saved MetaFile with the new name
   */
  MetaFile renameMetaFileToAvailableName(MetaFile metaFile, DMSFile projectFilesHome);

  /**
   * Deletes an orphaned MetaFile that was uploaded but not attached to any model. Generally called
   * when the user cancels a duplicate file upload.
   *
   * @param metaFile the MetaFile to delete
   */
  void cancelFileUpload(MetaFile metaFile);
}
