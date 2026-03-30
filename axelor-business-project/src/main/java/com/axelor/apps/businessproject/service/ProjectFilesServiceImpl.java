package com.axelor.apps.businessproject.service;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.project.db.Project;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.dms.db.DMSFile;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectFilesServiceImpl implements ProjectFilesService {
  private final Logger log = LoggerFactory.getLogger(ProjectFilesServiceImpl.class);

  protected DMSFileRepository dmsFileRepository;
  protected MetaFileRepository metaFileRepository;

  @Inject
  public ProjectFilesServiceImpl(
      DMSFileRepository dmsFileRepository, MetaFileRepository metaFileRepository) {
    this.dmsFileRepository = dmsFileRepository;
    this.metaFileRepository = metaFileRepository;
  }

  @Override
  @Transactional
  public void attachMetaFileToModel(Long modelId, String modelClassName, MetaFile metaFile) {
    log.debug("Got request from model {}", modelClassName);
    Model model = findModel(modelClassName, modelId);
    if (model == null) {
      throw new IllegalArgumentException(
          String.format("Model not found - Class: %s, ID: %s", modelClassName, modelId));
    }

    metaFile = Beans.get(MetaFileRepository.class).find(metaFile.getId());

    if (metaFile == null) {
      throw new IllegalStateException("Metafile not persisted yet");
    }

    Beans.get(MetaFiles.class).attach(metaFile, metaFile.getFileName(), model);
    log.debug("Attached file '{}' to {} [ID: {}]", metaFile.getFileName(), modelClassName, modelId);
  }

  public void uploadToProjectFiles(
      Project project, Long modelId, String modelClassName, MetaFile metaFile) {
    attachMetaFileToModel(modelId, modelClassName, metaFile);

    // if the model the file is uploaded to is related to an employee,
    // the file will be uploaded to the employee's folder and not to the project files
    // we then decide to attach the file directly to the project but since the file is needed
    // to appear in the employee's data reason we do the double upload
    if (modelHasField(modelClassName, Employee.class, "employee")) {
      attachMetaFileToModel(project.getId(), project.getClass().getName(), metaFile);
    }
  }

  @Override
  public boolean fileExistsInProjectFiles(DMSFile projectFilesHome, String filename) {
    return dmsFileRepository
            .all()
            .filter(
                "self.isDirectory = false "
                    + "AND (self.parent.id = :homeId OR self.parent.parent.id = :homeId) "
                    + "AND self.metaFile.fileName = :fileName")
            .bind("homeId", projectFilesHome.getId())
            .bind("fileName", filename)
            .count()
        > 0;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public MetaFile renameMetaFileToAvailableName(MetaFile metaFile, DMSFile projectFilesHome) {
    String newFileName =
        computeAvailableFileNameInProjectFolder(metaFile.getFileName(), projectFilesHome);

    metaFile.setFileName(newFileName);
    return metaFileRepository.save(metaFile);
  }

  @Override
  @Transactional
  public void cancelFileUpload(MetaFile metaFile) {
    if (metaFile != null) {
      metaFileRepository.remove(metaFile);
    }
  }

  @Override
  @SuppressWarnings("all")
  public boolean modelHasField(String modelClassName, Class<?> fieldType, String fieldName) {
    try {
      Class<? extends Model> clazz = (Class<? extends Model>) Class.forName(modelClassName);
      Mapper mapper = Mapper.of(clazz);
      Property property = mapper.getProperty(fieldName);

      if (property == null) {
        return false;
      }

      return fieldType.isAssignableFrom(property.getJavaType());
    } catch (ClassNotFoundException e) {
      log.error("Model class not found: {}", modelClassName, e);
      return false;
    }
  }

  /**
   * Finds and returns a model instance by its class name and ID.
   *
   * @param modelClassName Fully qualified class name of the model
   * @param modelId The ID of the model instance to find
   * @return The model instance, or null if not found or class doesn't exist
   */
  @SuppressWarnings("all")
  protected Model findModel(String modelClassName, Long modelId) {
    log.debug("Currently working with model {}", modelClassName);
    try {
      Class<? extends Model> clazz = (Class<? extends Model>) Class.forName(modelClassName);
      return JpaRepository.of(clazz).find(modelId);
    } catch (ClassNotFoundException e) {
      log.error("Model Class not found: {}", modelClassName, e);
      return null;
    }
  }

  protected String computeAvailableFileNameInProjectFolder(
      String fileName, DMSFile projectFilesHome) {

    // Split file name into base and extension
    int dotIndex = fileName.lastIndexOf('.');
    String baseName = dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    String extension = dotIndex > 0 ? fileName.substring(dotIndex) : "";

    List<DMSFile> existingFiles = getAllMatchingFiles(projectFilesHome, baseName);

    int max = 0;
    for (DMSFile dmsFile : existingFiles) {
      String existingName = dmsFile.getFileName();

      // Strip extension
      int extensionIndex = existingName.lastIndexOf('.');
      String existingBaseName =
          extensionIndex > 0 ? existingName.substring(0, extensionIndex) : existingName;

      // if it matches baseName_number pattern, use the appended number
      // to determine the appended number for the next file uploaded
      if (existingBaseName.startsWith(baseName + "_")) {
        String suffix = existingBaseName.substring(baseName.length() + 1);

        try {
          int num = Integer.parseInt(suffix);
          max = Integer.max(max, num);
        } catch (NumberFormatException ignored) {
          // Ignored
        }
      }
    }

    return baseName + "_" + (max + 1) + extension;
  }

  protected List<DMSFile> getAllMatchingFiles(DMSFile projectFilesHome, String baseName) {

    if (projectFilesHome == null) return List.of();

    return dmsFileRepository
        .all()
        .filter(
            "self.isDirectory = false "
                + "AND (self.parent.id = :homeId OR self.parent.parent.id = :homeId) "
                + "AND self.metaFile.fileName LIKE :pattern")
        .bind("homeId", projectFilesHome.getId())
        .bind("pattern", baseName + "%")
        .fetch();
  }
}
