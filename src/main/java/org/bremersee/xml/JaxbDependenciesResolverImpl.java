/*
 * Copyright 2020-2022  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.xml;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.lang.reflect.Modifier.isTransient;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttachmentRef;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.ReflectionUtils.FieldFilter;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.ReflectionUtils.MethodFilter;

/**
 * The jaxb dependencies resolver implementation.
 *
 * @author Christian Bremer
 */
@NoArgsConstructor(access = AccessLevel.PACKAGE)
class JaxbDependenciesResolverImpl implements JaxbDependenciesResolver {

  @SuppressWarnings("unchecked")
  private static final Class<? extends Annotation>[] EXPLICIT_XML_ANNOTATIONS = new Class[]{
      XmlAnyAttribute.class,
      XmlAnyElement.class,
      XmlAttachmentRef.class,
      XmlAttribute.class,
      XmlElement.class,
      XmlElementRef.class,
      XmlElementRefs.class,
      XmlElements.class,
      XmlElementWrapper.class,
      XmlID.class,
      XmlIDREF.class,
      XmlList.class,
      XmlMixed.class
  };

  @Override
  public Class<?>[] resolveClasses(final Object value) {
    final Set<ScanResult> scanResults = new HashSet<>();
    if (value instanceof Class<?>[]) {
      for (Class<?> clazz : ((Class<?>[]) value)) {
        resolveClasses(clazz, scanResults);
      }
    } else {
      resolveClasses(value, scanResults);
    }
    return ClassUtils.toClassArray(scanResults
        .stream()
        .map(ScanResult::getClazz)
        .collect(Collectors.toSet()));
  }

  private boolean resolveClasses(final Object value, final Set<ScanResult> scanResults) {
    if (isEmpty(value)
        || stopResolving(ClassUtils.getUserClass(value), value, scanResults)) {
      return false;
    }
    if (value instanceof Class) {
      resolveClasses((Class<?>) value, scanResults);
      return true;
    }
    if (value instanceof Collection) {
      Collection<?> collection = (Collection<?>) value;
      if (!collection.isEmpty()) {
        for (Object v : collection) {
          if (!resolveClasses(v, scanResults)) {
            return true;
          }
        }
      }
      return true;
    }
    Class<?> clazz = ClassUtils.getUserClass(value);
    resolveSuperClasses(clazz, value, scanResults);
    ReflectionUtils.doWithFields(
        clazz,
        new XmlFieldCallback(value, scanResults),
        new XmlFieldFilter(clazz));
    ReflectionUtils.doWithMethods(
        clazz,
        new XmlMethodCallback(value, scanResults),
        new XmlMethodFilter(clazz));
    Optional.ofNullable(AnnotationUtils.findAnnotation(clazz, XmlSeeAlso.class))
        .ifPresent(seeAlso -> Arrays.stream(seeAlso.value())
            .forEach(seeAlsoClass -> resolveClasses(seeAlsoClass, scanResults)));
    return true;
  }

  private void resolveClasses(final Class<?> clazz, final Set<ScanResult> scanResults) {
    if (stopResolving(clazz, null, scanResults)) {
      return;
    }
    resolveSuperClasses(clazz, null, scanResults);
    ReflectionUtils.doWithFields(
        clazz,
        new XmlFieldCallback(null, scanResults),
        new XmlFieldFilter(clazz));
    ReflectionUtils.doWithMethods(
        clazz,
        new XmlMethodCallback(null, scanResults),
        new XmlMethodFilter(clazz));
    Optional.ofNullable(AnnotationUtils.findAnnotation(clazz, XmlSeeAlso.class))
        .ifPresent(seeAlso -> Arrays.stream(seeAlso.value())
            .forEach(seeAlsoClass -> resolveClasses(seeAlsoClass, scanResults)));
  }

  private void resolveSuperClasses(
      final Class<?> clazz,
      final Object source,
      final Set<ScanResult> scanResults) {

    if (!stopResolving(clazz, source, scanResults)) {
      scanResults.add(new ScanResult(clazz, source));
      resolveSuperClasses(clazz.getSuperclass(), null, scanResults);
    }
  }

  private boolean stopResolving(final Class<?> clazz, final Object source,
      final Set<ScanResult> scanResults) {
    return isEmpty(clazz)
        || (!isAnnotatedWithXml(clazz) && !Collection.class.isAssignableFrom(clazz))
        || scanResults.contains(new ScanResult(clazz, source));
  }

  private boolean isAnnotatedWithXml(final Class<?> clazz) {
    return clazz.isAnnotationPresent(XmlRootElement.class)
        || clazz.isAnnotationPresent(XmlType.class);
  }

  private void processXmlAnnotations(final AnnotatedElement element,
      final Set<ScanResult> scanResults) {
    processXmlElement(AnnotationUtils.findAnnotation(element, XmlElement.class), scanResults);
    Optional
        .ofNullable(AnnotationUtils.findAnnotation(element, XmlElements.class))
        .map(XmlElements::value)
        .ifPresent(a -> Arrays.stream(a).forEach(e -> processXmlElement(e, scanResults)));

    processXmlElementRef(AnnotationUtils.findAnnotation(element, XmlElementRef.class), scanResults);
    Optional
        .ofNullable(AnnotationUtils.findAnnotation(element, XmlElementRefs.class))
        .map(XmlElementRefs::value)
        .ifPresent(a -> Arrays.stream(a).forEach(e -> processXmlElementRef(e, scanResults)));
  }

  private void processXmlElement(final XmlElement annotation, final Set<ScanResult> scanResults) {
    Optional.ofNullable(annotation)
        .map(XmlElement::type)
        .filter(type -> !type.equals(XmlElement.DEFAULT.class))
        .ifPresent(type -> resolveClasses(type, scanResults));
  }

  private void processXmlElementRef(final XmlElementRef annotation,
      final Set<ScanResult> scanResults) {
    Optional.ofNullable(annotation)
        .map(XmlElementRef::type)
        .filter(type -> !type.equals(XmlElementRef.DEFAULT.class))
        .ifPresent(type -> resolveClasses(type, scanResults));
  }

  private class XmlFieldCallback implements FieldCallback {

    private final Object value;

    private final Set<ScanResult> scanResults;

    /**
     * Instantiates a new xml field callback.
     *
     * @param value the value
     * @param scanResults the scan results
     */
    XmlFieldCallback(final Object value, final Set<ScanResult> scanResults) {
      this.value = value;
      this.scanResults = scanResults;
    }

    @Override
    public void doWith(final Field field) throws IllegalArgumentException {
      ReflectionUtils.makeAccessible(field);
      processXmlAnnotations(field, scanResults);
      if (isEmpty(value)) {
        if (Collection.class.isAssignableFrom(field.getType())) {
          for (ResolvableType rt : ResolvableType.forField(field).getGenerics()) {
            resolveClasses(rt.resolve(), scanResults);
          }
        } else {
          resolveClasses(field.getType(), scanResults);
        }
      } else {
        final Object fieldValue = ReflectionUtils.getField(field, value);
        if (!isEmpty(fieldValue)) {
          if (fieldValue instanceof Collection && ((Collection<?>) fieldValue).isEmpty()) {
            for (ResolvableType rt : ResolvableType.forField(field).getGenerics()) {
              resolveClasses(rt.resolve(), scanResults);
            }
          } else {
            resolveClasses(fieldValue, scanResults);
          }
        } else {
          if (Collection.class.isAssignableFrom(field.getType())) {
            for (ResolvableType rt : ResolvableType.forField(field).getGenerics()) {
              resolveClasses(rt.resolve(), scanResults);
            }
          } else {
            resolveClasses(field.getType(), scanResults);
          }
        }
      }
    }
  }

  private class XmlMethodCallback implements MethodCallback {

    private final Object value;

    private final Set<ScanResult> scanResults;

    /**
     * Instantiates a new xml method callback.
     *
     * @param value the value
     * @param scanResults the scan results
     */
    XmlMethodCallback(final Object value, final Set<ScanResult> scanResults) {
      this.value = value;
      this.scanResults = scanResults;
    }

    @Override
    public void doWith(final Method method) throws IllegalArgumentException {
      ReflectionUtils.makeAccessible(method);
      processXmlAnnotations(method, scanResults);
      if (isEmpty(value)) {
        if (Collection.class.isAssignableFrom(method.getReturnType())) {
          for (ResolvableType rt : ResolvableType.forMethodReturnType(method).getGenerics()) {
            resolveClasses(rt.resolve(), scanResults);
          }
        } else {
          resolveClasses(method.getReturnType(), scanResults);
        }
      } else {
        final Object methodValue = ReflectionUtils.invokeMethod(method, value);
        if (!isEmpty(methodValue)) {
          if (methodValue instanceof Collection && ((Collection<?>) methodValue).isEmpty()) {
            for (ResolvableType rt : ResolvableType
                .forMethodReturnType(method, ClassUtils.getUserClass(value))
                .getGenerics()) {
              resolveClasses(rt.resolve(), scanResults);
            }
          } else {
            resolveClasses(methodValue, scanResults);
          }
        } else {
          resolveClasses(method.getReturnType(), scanResults);
        }
      }
    }
  }

  private static boolean anyXmlAnnotationPresent(final AnnotatedElement element) {
    return Arrays.stream(EXPLICIT_XML_ANNOTATIONS).anyMatch(element::isAnnotationPresent);
  }

  private static class XmlFieldFilter implements FieldFilter {

    private final XmlAccessType accessType;

    /**
     * Instantiates a new xml field filter.
     *
     * @param clazz the class
     */
    XmlFieldFilter(final Class<?> clazz) {
      this.accessType = Optional
          .ofNullable(AnnotationUtils.findAnnotation(clazz, XmlAccessorType.class))
          .map(XmlAccessorType::value)
          .orElseGet(() -> Optional
              .ofNullable(clazz.getPackage().getAnnotation(XmlAccessorType.class))
              .map(XmlAccessorType::value)
              .orElse(XmlAccessType.PUBLIC_MEMBER));
    }

    @Override
    public boolean matches(final Field field) {
      int modifiers = field.getModifiers();
      if (isStatic(modifiers)
          || isTransient(modifiers)
          || field.isAnnotationPresent(XmlTransient.class)) {
        return false;
      }
      switch (accessType) {
        case FIELD:
          return true;
        case PUBLIC_MEMBER:
          return isPublic(modifiers) || anyXmlAnnotationPresent(field);
        default: // PROPERTY, NONE
          return anyXmlAnnotationPresent(field);
      }
    }

  }

  private static class XmlMethodFilter implements MethodFilter {

    private final XmlAccessType accessType;

    /**
     * Instantiates a new xml method filter.
     *
     * @param clazz the class
     */
    XmlMethodFilter(final Class<?> clazz) {
      this.accessType = Optional
          .ofNullable(AnnotationUtils.findAnnotation(clazz, XmlAccessorType.class))
          .map(XmlAccessorType::value)
          .orElseGet(() -> Optional
              .ofNullable(clazz.getPackage().getAnnotation(XmlAccessorType.class))
              .map(XmlAccessorType::value)
              .orElse(XmlAccessType.PUBLIC_MEMBER));
    }

    @Override
    public boolean matches(final Method method) {
      int modifiers = method.getModifiers();
      if (isStatic(modifiers)
          || !(method.getName().startsWith("get") || method.getName().startsWith("is"))
          || method.getParameterCount() > 0
          || method.getReturnType().equals(Void.class)
          || method.isAnnotationPresent(XmlTransient.class)) {
        return false;
      }
      switch (accessType) {
        case PROPERTY:
          return true;
        case PUBLIC_MEMBER:
          return isPublic(modifiers) || anyXmlAnnotationPresent(method);
        default:
          return anyXmlAnnotationPresent(method);
      }
    }
  }

  @EqualsAndHashCode
  private static class ScanResult {

    private final Class<?> clazz;

    private final Object source;

    /**
     * Instantiates a new scan result.
     *
     * @param clazz the class
     * @param source the source
     */
    ScanResult(final Class<?> clazz, final Object source) {
      Assert.notNull(clazz, "Class must be present.");
      this.clazz = clazz;
      this.source = source;
    }

    /**
     * Gets clazz.
     *
     * @return the class
     */
    Class<?> getClazz() {
      return clazz;
    }
  }

}
