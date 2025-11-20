package com.axelor.apps.base.db.repo.dms;

import com.axelor.common.Inflector;
import com.axelor.db.EntityHelper;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.dms.db.DMSFile;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.axelor.rpc.filter.Filter;
import com.axelor.rpc.filter.JPQLFilter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Custom DMS File Repository extending the standard AOP implementation. Adds hierarchical folder
 * structures and automatic organization for specific entity types.
 */
public class CustomDMSFileRepository extends DMSFileRepository {

  // Self-injection for transactional proxy support
  @Inject private DMSFileRepository self;

  private static final DMSFolderConfiguration.FolderStructureConfig[] FOLDER_CONFIGS =
      DMSFolderConfiguration.FOLDER_CONFIGS;

  /**
   * Finds or creates the appropriate home folder for an entity based on configured relationships
   * and folder structures.
   *
   * @param related the entity to find/create a home folder for
   * @return the home folder for the entity
   */
  @Override
  protected DMSFile findOrCreateHome(Model related) {
    // Check if the related IS one of the configured entity types
    DMSFolderConfiguration.FolderStructureConfig matchingConfig = findMatchingConfig(related);
    if (matchingConfig != null) {
      DMSFile entityHome = findOrCreateEntityHome(related, matchingConfig);
      return entityHome;
    }

    // Check if this model has relationships to any configured entity types
    for (DMSFolderConfiguration.FolderStructureConfig config : FOLDER_CONFIGS) {
      Model relatedEntity = getRelatedEntity(related, config);
      if (relatedEntity != null) {
        return findOrCreateNestedEntityHome(related, relatedEntity, config);
      }
    }

    // Fallback to standard structure
    return findOrCreateStandardHome(related);
  }

  /**
   * Finds the home directory for a related entity, checking configured parent relationships and
   * creating type-based folder structure when parent entities exist.
   *
   * @param related the entity to find the home directory for
   * @return the home DMSFile directory, or null if not found
   */
  @Override
  @Nullable
  public DMSFile findHomeByRelated(Model related) {
    // Check all configured entity relationships FIRST
    for (DMSFolderConfiguration.FolderStructureConfig config : FOLDER_CONFIGS) {
      Model relatedEntity = getRelatedEntity(related, config);
      if (relatedEntity != null) {
        DMSFile entityHome = findHomeByRelated(relatedEntity);
        if (entityHome != null) {
          final Inflector inflector = Inflector.getInstance();
          String typeFolderName =
              inflector.pluralize(inflector.humanize(related.getClass().getSimpleName()));

          DMSFile typeFolder =
              all()
                  .filter(
                      "COALESCE(self.isDirectory, FALSE) = TRUE "
                          + "AND self.parent = :parent "
                          + "AND self.fileName = :folderName "
                          + "AND self.relatedModel = :model "
                          + "AND COALESCE(self.relatedId, 0) = 0")
                  .bind("parent", entityHome)
                  .bind("folderName", typeFolderName)
                  .bind("model", related.getClass().getName())
                  .fetchOne();

          return typeFolder;
        }
      }
    }

    DMSFile home =
        all()
            .filter(
                "COALESCE(self.isDirectory, FALSE) = TRUE "
                    + "AND self.relatedId = :id "
                    + "AND self.relatedModel = :model "
                    + "AND self.parent.relatedModel = :model "
                    + "AND COALESCE(self.parent.relatedId, 0) = 0")
            .bind("id", related.getId())
            .bind("model", related.getClass().getName())
            .fetchOne();

    return home;
  }

  /**
   * Populates additional metadata for a DMSFile
   *
   * @param json the file data to populate
   * @param context the population context
   * @return the populated json with additional metadata
   */
  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    json = super.populate(json, context);

    if (context != null && context.get("_populate") == Boolean.FALSE) {
      return json;
    }

    DMSFile file = findFrom(json);
    if (file == null) {
      return json;
    }

    // Sync directory name with related entity if it changed
    if (Boolean.TRUE.equals(file.getIsDirectory()) && file.getRelatedId() != null) {
      try {
        boolean wasUpdated =
            ((CustomDMSFileRepository) self).syncDirectoryNameWithEntity(file.getId());

        if (wasUpdated) {
          file = find(file.getId());
          json.put("fileName", file.getFileName());
        }
      } catch (Exception ignored) {

      }
    }

    return json;
  }

  /**
   * Creates the standard two-level folder structure: /ModelType/RecordName/ Used when no special
   * folder structure is configured for the entity type.
   *
   * @param related the entity to create standard structure for
   * @return the entity's home folder
   */
  private DMSFile findOrCreateStandardHome(Model related) {
    final List<Filter> dmsRootFilters =
        Lists.newArrayList(
            new JPQLFilter(
                "COALESCE(self.isDirectory, FALSE) = TRUE "
                    + "AND self.relatedModel = :model "
                    + "AND COALESCE(self.relatedId, 0) = 0"));
    final DMSFile dmsRootParent = getRootParent(related);

    if (dmsRootParent == null) {
      // Explicitly enforce root-level if no specific root parent is defined
      // This ensures that when a file is uploaded with its related model,
      // we don't confuse an employee's folder (e.g., "Files") with the newly
      // uploaded file's root (e.g., "Files (root)") as they have the same name
      dmsRootFilters.add(new JPQLFilter("self.parent IS NULL"));
    } else {
      dmsRootFilters.add(new JPQLFilter("self.parent = :rootParent"));
    }

    DMSFile dmsRoot =
        Filter.and(dmsRootFilters)
            .build(DMSFile.class)
            .bind("model", related.getClass().getName())
            .bind("rootParent", dmsRootParent)
            .fetchOne();

    if (dmsRoot == null) {
      final Inflector inflector = Inflector.getInstance();
      dmsRoot = new DMSFile();
      String rootName = inflector.pluralize(inflector.humanize(related.getClass().getSimpleName()));
      if ("Employees".equals(rootName)) {
        rootName += " data";
      }
      dmsRoot.setFileName(rootName);
      dmsRoot.setRelatedModel(related.getClass().getName());
      dmsRoot.setIsDirectory(true);
      dmsRoot.setParent(dmsRootParent);
      dmsRoot = save(dmsRoot);
    }

    DMSFile dmsHome = findHomeByRelated(related);

    if (dmsHome == null) {
      String homeName = null;

      final Mapper mapper = Mapper.of(related.getClass());
      homeName = mapper.getNameField().get(related).toString();
      if (homeName == null) {
        homeName = Strings.padStart("" + related.getId(), 5, '0');
      }

      dmsHome = new DMSFile();
      dmsHome.setFileName(homeName);
      dmsHome.setRelatedId(related.getId());
      dmsHome.setRelatedModel(related.getClass().getName());
      dmsHome.setParent(dmsRoot);
      dmsHome.setIsDirectory(true);
      dmsHome = save(dmsHome);
    }

    return dmsHome;
  }

  /**
   * Creates or finds the entity's home folder within the configured root path structure. Uses the
   * entity's name field or padded ID for the folder name.
   *
   * @param entity the entity to create a home folder for
   * @param config the folder structure configuration
   * @return the entity's home folder
   */
  private DMSFile findOrCreateEntityHome(
      Model entity, DMSFolderConfiguration.FolderStructureConfig config) {
    DMSFile rootFolder = findOrCreateRootPath(config);

    DMSFile entityHome =
        all()
            .filter(
                "COALESCE(self.isDirectory, FALSE) = TRUE "
                    + "AND self.parent = :parent "
                    + "AND self.relatedModel = :model "
                    + "AND self.relatedId = :id")
            .bind("parent", rootFolder)
            .bind("model", config.entityClass)
            .bind("id", entity.getId())
            .fetchOne();

    if (entityHome == null) {
      String entityName = null;
      final Mapper mapper = Mapper.of(entity.getClass());
      Property nameField = mapper.getNameField();
      if (nameField != null) {
        Object nameValue = nameField.get(entity);
        if (nameValue != null) {
          entityName = nameValue.toString();
        }
      }

      if (entityName == null) {
        entityName = Strings.padStart("" + entity.getId(), 5, '0');
      }

      entityHome = new DMSFile();
      entityHome.setFileName(entityName);
      entityHome.setRelatedId(entity.getId());
      entityHome.setRelatedModel(config.entityClass);
      entityHome.setParent(rootFolder);
      entityHome.setIsDirectory(true);
      // Use persist() to bypass save() to avoid using the logic of the parent save.
      JpaRepository.of(DMSFile.class).persist(entityHome);
    }

    return entityHome;
  }

  /**
   * Creates or finds a type-based folder under a parent entity's home folder. Creates structure
   * like: /Projects/Project001/Tasks/
   *
   * @param related the entity that needs a nested home
   * @param parentEntity the parent entity to nest under
   * @param config the folder structure configuration for the parent
   * @return the type folder for the related entity
   */
  private DMSFile findOrCreateNestedEntityHome(
      Model related, Model parentEntity, DMSFolderConfiguration.FolderStructureConfig config) {
    DMSFile parentHome = findOrCreateEntityHome(parentEntity, config);

    final Inflector inflector = Inflector.getInstance();
    String typeFolderName =
        inflector.pluralize(inflector.humanize(related.getClass().getSimpleName()));

    DMSFile typeFolder =
        all()
            .filter(
                "COALESCE(self.isDirectory, FALSE) = TRUE "
                    + "AND self.parent = :parent "
                    + "AND self.fileName = :folderName "
                    + "AND self.relatedModel = :model "
                    + "AND COALESCE(self.relatedId, 0) = 0")
            .bind("parent", parentHome)
            .bind("folderName", typeFolderName)
            .bind("model", related.getClass().getName())
            .fetchOne();

    if (typeFolder == null) {
      typeFolder = new DMSFile();
      typeFolder.setFileName(typeFolderName);
      typeFolder.setRelatedModel(related.getClass().getName());
      typeFolder.setIsDirectory(true);
      typeFolder.setParent(parentHome);
      // Use persist() to bypass save() to avoid using the logic of the parent save.
      JpaRepository.of(DMSFile.class).persist(typeFolder);
    }

    return typeFolder;
  }

  /**
   * Creates or finds the folder hierarchy defined in the configuration. The last folder in the path
   * is marked with the entity's relatedModel.
   *
   * @param config the folder structure configuration with the path to create
   * @return the final folder in the path hierarchy
   */
  private DMSFile findOrCreateRootPath(DMSFolderConfiguration.FolderStructureConfig config) {
    DMSFile currentParent = null;

    for (int i = 0; i < config.rootPath.length; i++) {
      String folderName = config.rootPath[i];
      boolean isLastInPath = i == config.rootPath.length - 1;

      DMSFile folder =
          all()
              .filter(
                  "COALESCE(self.isDirectory, FALSE) = TRUE "
                      + "AND self.fileName = :name "
                      + (currentParent == null
                          ? "AND self.parent IS NULL"
                          : "AND self.parent = :parent")
                      + (isLastInPath
                          ? " AND self.relatedModel = :model AND COALESCE(self.relatedId, 0) = 0"
                          : " AND self.relatedModel IS NULL"))
              .bind("name", folderName)
              .bind("parent", currentParent)
              .bind("model", isLastInPath ? config.entityClass : null)
              .fetchOne();

      if (folder == null) {
        folder = new DMSFile();
        folder.setFileName(folderName);
        folder.setIsDirectory(true);
        folder.setParent(currentParent);

        if (isLastInPath) {
          folder.setRelatedModel(config.entityClass);
        }

        // Use persist() to bypass save() to avoid using the logic of the parent save.
        JpaRepository.of(DMSFile.class).persist(folder);
      }

      currentParent = folder;
    }

    return currentParent;
  }

  /**
   * Finds the configuration that matches the given model's entity type.
   *
   * @param related the model to check
   * @return the matching configuration, or null if none found
   */
  @Nullable
  private DMSFolderConfiguration.FolderStructureConfig findMatchingConfig(Model related) {
    if (related == null) {
      return null;
    }

    Class<?> entityClass = EntityHelper.getEntityClass(related);
    String entityClassName = entityClass.getName();

    for (DMSFolderConfiguration.FolderStructureConfig config : FOLDER_CONFIGS) {
      if (config.entityClass.equals(entityClassName)) {
        return config;
      }
    }

    return null;
  }

  /**
   * Finds a property in the model that references the configured entity type, checking priority
   * fields first, then falling back to any matching reference field.
   *
   * @param related the model to search for the property
   * @param config the folder structure configuration containing target entity and field priorities
   * @return the matching Property, or null if not found
   */
  @Nullable
  private Property findRelatedEntityField(
      Model related, DMSFolderConfiguration.FolderStructureConfig config) {
    final Mapper mapper = Mapper.of(EntityHelper.getEntityClass(related));

    // Search for fields in priority order
    for (String priorityFieldName : config.fieldPriority) {
      for (Property prop : mapper.getProperties()) {
        if (!prop.isReference() || prop.getTarget() == null || prop.isCollection()) {
          continue;
        }

        if (config.entityClass.equals(prop.getTarget().getName())
            && priorityFieldName.equalsIgnoreCase(prop.getName())) {
          return prop;
        }
      }
    }

    // If no priority field found, look for any field of the target type
    for (Property prop : mapper.getProperties()) {
      if (!prop.isReference() || prop.getTarget() == null || prop.isCollection()) {
        continue;
      }

      if (config.entityClass.equals(prop.getTarget().getName())) {
        return prop;
      }
    }

    return null;
  }

  /**
   * Retrieves the related entity instance from the model using the configured field.
   *
   * @param related the model containing the relationship
   * @param config the folder structure configuration
   * @return the related entity instance, or null if not found
   */
  @Nullable
  @SuppressWarnings("all")
  private Model getRelatedEntity(
      Model related, DMSFolderConfiguration.FolderStructureConfig config) {
    Property field = findRelatedEntityField(related, config);
    if (field == null) {
      return null;
    }

    try {
      Object entityObj = field.get(related);
      if (entityObj == null) {
        return null;
      }

      Model entity = EntityHelper.getEntity((Model) entityObj);
      return entity;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Moves an entity's DMS file to the correct home folder based on current relationships. Cleans up
   * empty folders after moving.
   *
   * @param entityId the ID of the entity
   * @param entityModel the fully qualified class name of the entity
   * @return true if the file was moved, false otherwise
   */
  @Transactional
  public boolean moveFileToCorrectHome(Long entityId, String entityModel) {
    try {
      // Re-fetch the entity with its current relationships
      @SuppressWarnings("unchecked")
      Class<? extends Model> klass = (Class<? extends Model>) Class.forName(entityModel);
      Model entity = JpaRepository.of(klass).find(entityId);

      if (entity == null) {
        return false;
      }

      // Get the DMSFile from the entity
      Mapper mapper = Mapper.of(entity.getClass());
      Property dmsFileProperty = mapper.getProperty("dmsFile");

      if (dmsFileProperty == null) {
        return false;
      }

      DMSFile dmsFile = (DMSFile) dmsFileProperty.get(entity);
      if (dmsFile == null) {
        return false;
      }

      DMSFile oldParent = dmsFile.getParent();

      // Calculate where this file SHOULD be based on current entity state
      DMSFile newParent = findOrCreateHome(entity);

      if (oldParent != null && oldParent.getId().equals(newParent.getId())) {
        return false;
      }

      dmsFile.setParent(newParent);
      save(dmsFile);

      // Clean up old parent if it's now empty
      if (oldParent != null) {
        cleanupEmptyFolderChain(oldParent);
      }

      return true;

    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Recursively cleans up empty folders, walking up the parent chain. This prevents orphaned empty
   * folder structures after moving files.
   *
   * @param folder the folder to check and potentially delete
   */
  private void cleanupEmptyFolderChain(DMSFile folder) {
    if (folder == null) {
      return;
    }

    // Check if folder is empty
    long childCount = all().filter("self.parent = :parent").bind("parent", folder).count();

    if (childCount == 0) {
      // Remember parent before deleting (for recursive cleanup)
      DMSFile parentFolder = folder.getParent();

      remove(folder);

      // Recursively clean up parent folders if they're also empty now
      if (parentFolder != null) {
        cleanupEmptyFolderChain(parentFolder);
      }
    }
  }

  /**
   * Syncs a directory's name with its related entity's current name. This ensures entity home
   * folders reflect current entity names.
   *
   * @param fileId the DMSFile ID to sync
   * @return true if the name was updated, false otherwise
   */
  @Transactional
  public boolean syncDirectoryNameWithEntity(Long fileId) {
    DMSFile file = find(fileId);
    if (file == null) {
      return false;
    }

    // Only process directories with related entities
    if (!Boolean.TRUE.equals(file.getIsDirectory())) {
      return false;
    }

    if (file.getRelatedId() == null || file.getRelatedModel() == null) {
      return false;
    }

    // Get the related entity
    Model relatedEntity = findRelatedFromFile(file);
    if (relatedEntity == null) {
      return false;
    }

    // Get what the directory name SHOULD be
    String expectedName = getExpectedDirectoryName(relatedEntity);
    if (expectedName == null || expectedName.trim().isEmpty()) {
      return false;
    }

    // Check if name needs updating
    String currentName = file.getFileName();
    if (expectedName.equals(currentName)) {
      return false;
    }

    file.setFileName(expectedName);

    // Use super.save to avoid triggering DMS folder creation logic
    super.save(file);

    return true;
  }

  /**
   * Determines what a directory name should be based on its related entity. Tries to use the
   * entity's name field, falls back to padded ID. This matches the logic in
   * findOrCreateEntityHome() to ensure consistency.
   *
   * @param entity the related entity
   * @return the expected directory name
   */
  private String getExpectedDirectoryName(Model entity) {
    if (entity == null) {
      return null;
    }

    final Mapper mapper = Mapper.of(EntityHelper.getEntityClass(entity));
    Property nameField = mapper.getNameField();

    if (nameField != null) {
      Object nameValue = nameField.get(entity);
      if (nameValue != null) {
        String name = nameValue.toString();
        if (name != null && !name.trim().isEmpty()) {
          return name;
        }
      }
    }

    // Fallback: Use padded ID (same logic as in findOrCreateEntityHome)
    if (entity.getId() != null) {
      return Strings.padStart("" + entity.getId(), 5, '0');
    }

    return null;
  }

  /**
   * Helper method to find the related entity from a DMSFile. This is a convenience method that
   * wraps the logic from the original findRelated.
   *
   * @param file the DMSFile to get the related entity from
   * @return the related entity, or null if not found
   */
  @SuppressWarnings("all")
  private Model findRelatedFromFile(DMSFile file) {
    if (file == null || file.getRelatedId() == null || file.getRelatedModel() == null) {
      return null;
    }

    Class<? extends Model> klass = null;
    try {
      klass = (Class) Class.forName(file.getRelatedModel());
    } catch (Exception e) {
      return null;
    }

    final Model entity = JpaRepository.of(klass).find(file.getRelatedId());
    return EntityHelper.getEntity(entity);
  }

  /**
   * Helper method to find DMSFile from JSON map. Uses the same logic as the parent class for
   * consistency.
   *
   * @param json the JSON map containing file data
   * @return the DMSFile, or null if not found
   */
  private DMSFile findFrom(Map<String, Object> json) {
    if (json == null || json.get("id") == null) {
      return null;
    }

    try {
      final Long id = Long.parseLong(json.get("id").toString());
      return find(id);
    } catch (Exception e) {
      return null;
    }
  }
}
