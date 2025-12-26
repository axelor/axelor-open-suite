package com.axelor.apps.base.db.repo.dms;

/**
 * Class holding configurations for defining custom folder structures in the DMS for specific entity
 * types.
 */
public final class DMSFolderConfiguration {

  private DMSFolderConfiguration() {}

  /** Configuration for defining custom folder structures in the DMS for specific entity types. */
  public static class FolderStructureConfig {
    public final String entityClass;

    /** The class name of the entity this configuration applies to */
    public final String[] rootPath; /* The hierarchical folder path to create */

    /**
     * Ordered list of field names to check for establishing parent-child relationships. Fields are
     * checked in priority order until a valid relation is found
     */
    public final String[] fieldPriority;

    public FolderStructureConfig(String entityClass, String[] rootPath, String[] fieldPriority) {
      this.entityClass = entityClass;
      this.rootPath = rootPath;
      this.fieldPriority = fieldPriority;
    }
  }

  public static final FolderStructureConfig EMPLOYEE_CONFIG =
      new FolderStructureConfig(
          "com.axelor.apps.hr.db.Employee",
          new String[] {"Employee data"},
          new String[] {"employee", "manager", "managerUser"});

  public static final FolderStructureConfig PROJECT_CONFIG =
      new FolderStructureConfig(
          "com.axelor.apps.project.db.Project",
          new String[] {"Project Management", "Project Data"},
          new String[] {"project", "parentProject"});

  public static final FolderStructureConfig[] FOLDER_CONFIGS = {EMPLOYEE_CONFIG, PROJECT_CONFIG};

  /** Check if a field target is one of our configured entity types */
  public static boolean isConfiguredEntity(String targetClassName) {
    if (targetClassName == null) {
      return false;
    }
    for (FolderStructureConfig config : FOLDER_CONFIGS) {
      if (config.entityClass.equals(targetClassName)) {
        return true;
      }
    }
    return false;
  }
}
