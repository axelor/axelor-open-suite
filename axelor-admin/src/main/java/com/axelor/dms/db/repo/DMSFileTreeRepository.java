package com.axelor.dms.db.repo;

import com.axelor.auth.AuthUtils;
import com.axelor.common.Inflector;
import com.axelor.db.EntityHelper;
import com.axelor.db.JPA;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.db.annotations.Track;
import com.axelor.db.mapper.Mapper;
import com.axelor.dms.db.DMSFile;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.mail.db.MailMessage;
import com.axelor.mail.db.repo.MailMessageRepository;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaAttachment;
import com.axelor.meta.db.repo.MetaAttachmentRepository;
import com.axelor.rpc.filter.Filter;
import com.axelor.rpc.filter.JPQLFilter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

public class DMSFileTreeRepository extends DMSFileRepository {
  @Inject protected MetaFiles metaFiles;
  @Inject protected MetaAttachmentRepository attachments;

  protected Model findRelated(DMSFile file) {
    if (file.getRelatedId() == null || file.getRelatedModel() == null) {
      return null;
    }

    try {
      @SuppressWarnings("unchecked")
      final Class<? extends Model> klass =
          (Class<? extends Model>) Class.forName(file.getRelatedModel());
      final Model entity = JpaRepository.of(klass).find(file.getRelatedId());
      return EntityHelper.getEntity(entity);
    } catch (Exception e) {
      return null;
    }
  }

  protected void createMessage(DMSFile file, boolean delete) {
    final Model related = findRelated(file);
    if (related == null || related.getId() == null || file.getMetaFile() == null) {
      return;
    }
    final Class<?> klass = EntityHelper.getEntityClass(related);
    final Track track = klass.getAnnotation(Track.class);
    if (track == null || !track.files()) {
      return;
    }
    final ObjectMapper objectMapper = Beans.get(ObjectMapper.class);
    final MailMessageRepository messages = Beans.get(MailMessageRepository.class);
    final MailMessage message = new MailMessage();

    message.setRelatedId(related.getId());
    message.setRelatedModel(klass.getName());
    message.setAuthor(AuthUtils.getUser());

    message.setSubject(delete ? I18n.get("File removed") : I18n.get("File added"));

    final Map<String, Object> json = new HashMap<>();
    final Map<String, Object> attrs = new HashMap<>();

    attrs.put("id", file.getMetaFile().getId());
    attrs.put("fileName", file.getFileName());
    attrs.put("fileIcon", metaFiles.fileTypeIcon(file.getMetaFile()));

    json.put("files", Arrays.asList(attrs));
    try {
      message.setBody(objectMapper.writeValueAsString(json));
    } catch (JsonProcessingException e) {
    }

    messages.save(message);
  }

  @Override
  public DMSFile save(DMSFile entity) {
    DMSFile parent = entity.getParent();
    Model related = findRelated(entity);
    if (related == null && parent != null) {
      related = findRelated(parent);
    }

    final boolean isAttachment = related != null && entity.getMetaFile() != null;

    if (related != null) {
      entity.setRelatedId(related.getId());
      entity.setRelatedModel(related.getClass().getName());
    }

    // if new attachment, save attachment reference
    if (isAttachment) {
      // remove old attachment if file is moved
      MetaAttachment attachmentOld =
          attachments.all().filter("self.metaFile.id = ?", entity.getMetaFile().getId()).fetchOne();
      if (attachmentOld != null) {
        attachments.remove(attachmentOld);
      }

      MetaAttachment attachment =
          attachments
              .all()
              .filter(
                  "self.metaFile.id = ? AND self.objectId = ? AND self.objectName = ?",
                  entity.getMetaFile().getId(),
                  related.getId(),
                  related.getClass().getName())
              .fetchOne();
      if (attachment == null) {
        attachment = metaFiles.attach(entity.getMetaFile(), related);
        attachments.save(attachment);
      }

      // generate track message
      createMessage(entity, false);
    }

    // if not an attachment or has parent, do nothing
    if (parent == null && related != null) {
      // create parent folders
      final DMSFile dmsHome = findOrCreateHome(related);
      entity.setParent(dmsHome);
    }

    return JPA.save(entity);
  }

  /**
   * Finds or creates parent folders.
   *
   * @param related model
   * @return home parent
   */
  protected DMSFile findOrCreateHome(Model related) {
    final DMSFile dmsRootParent = getRootParent(related);
    final List<Filter> dmsRootFilters =
        Lists.newArrayList(
            new JPQLFilter(
                ""
                    + "self.isDirectory = TRUE "
                    + "AND self.relatedModel = :model "
                    + "AND COALESCE(self.relatedId, 0) = 0"));

    if (dmsRootParent != null) {
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
      dmsRoot.setFileName(
          inflector.pluralize(inflector.humanize(related.getClass().getSimpleName())));
      dmsRoot.setRelatedModel(related.getClass().getName());
      dmsRoot.setIsDirectory(true);
      dmsRoot.setParent(dmsRootParent);
      dmsRoot = JPA.save(dmsRoot); // Should get id before its child.
    }

    DMSFile dmsHome =
        all()
            .filter(
                ""
                    + "self.isDirectory = TRUE "
                    + "AND self.relatedId = :id "
                    + "AND self.relatedModel = :model "
                    + "AND self.parent.relatedModel = :model "
                    + "AND COALESCE(self.parent.relatedId, 0) = 0")
            .bind("id", related.getId())
            .bind("model", related.getClass().getName())
            .fetchOne();

    if (dmsHome == null) {
      String homeName = null;

      try {
        final Mapper mapper = Mapper.of(related.getClass());
        homeName = mapper.getNameField().get(related).toString();
      } catch (Exception e) {
        // Ignore
      }

      if (homeName == null) {
        homeName = Strings.padStart("" + related.getId(), 5, '0');
      }

      dmsHome = new DMSFile();
      dmsHome.setFileName(homeName);
      dmsHome.setRelatedId(related.getId());
      dmsHome.setRelatedModel(related.getClass().getName());
      dmsHome.setParent(dmsRoot);
      dmsHome.setIsDirectory(true);
      dmsHome = JPA.save(dmsHome); // Should get id before its child.
    }

    return dmsHome;
  }

  /**
   * Gets root parent folder
   *
   * @param related model
   * @return root parent folder
   */
  @Nullable
  protected DMSFile getRootParent(Model related) {
    DMSFile dmsRootParent = null;

    final Optional<Model> parentRelatedOpt =
        Arrays.stream(related.getClass().getDeclaredFields())
            .filter(
                field ->
                    field.isAnnotationPresent(ManyToOne.class)
                        && field.getType() != related.getClass()
                        && Arrays.stream(field.getType().getDeclaredFields())
                            .anyMatch(
                                parentField -> {
                                  final OneToMany oneToMany =
                                      parentField.getAnnotation(OneToMany.class);
                                  return oneToMany != null
                                      && field.getName().equals(oneToMany.mappedBy())
                                      && getFieldGenericType(parentField, 0) == related.getClass();
                                }))
            .map(field -> (Model) getPropertyOrNull(related, field.getName()))
            .filter(Objects::nonNull)
            .findFirst();

    if (parentRelatedOpt.isPresent()) {
      final Model parentRelated = parentRelatedOpt.get();
      dmsRootParent = findOrCreateHome(parentRelated);
    }

    return dmsRootParent;
  }

  /**
   * Gets the value of the specified property of the specified bean. Returns null if any error
   * occurred.
   *
   * @param bean
   * @param name
   * @return
   */
  @Nullable
  protected Object getPropertyOrNull(Model bean, String name) {
    try {
      return getProperty(bean, name);
    } catch (IllegalAccessException
        | InvocationTargetException
        | NoSuchMethodException
        | IntrospectionException e) {
      return null;
    }
  }

  /**
   * Gets the value of the specified property of the specified bean.
   *
   * @param bean
   * @param name
   * @return property value
   * @throws IntrospectionException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   * @throws NoSuchMethodException
   */
  protected Object getProperty(Model bean, String name)
      throws IntrospectionException, IllegalAccessException, InvocationTargetException,
          NoSuchMethodException {
    final BeanInfo info = Introspector.getBeanInfo(bean.getClass());
    final Optional<PropertyDescriptor> pdOpt =
        Arrays.stream(info.getPropertyDescriptors())
            .filter(pd -> pd.getName().equals(name))
            .findFirst();

    if (pdOpt.isPresent()) {
      return EntityHelper.getEntity(pdOpt.get().getReadMethod().invoke(bean));
    }

    throw new NoSuchMethodException(name);
  }

  /**
   * Gets generic type of the specified field at the specified index.
   *
   * @param field
   * @param index
   * @return field generic class
   */
  protected Class<?> getFieldGenericType(Field field, int index) {
    Type type = field.getGenericType();

    if (type instanceof ParameterizedType) {
      final ParameterizedType ptype = (ParameterizedType) type;
      type = ptype.getActualTypeArguments()[index];

      if (type instanceof ParameterizedType) {
        return (Class<?>) ((ParameterizedType) type).getRawType();
      }
    }

    return (Class<?>) type;
  }
}
