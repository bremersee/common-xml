/*
 * Copyright 2018-2020 the original author or authors.
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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
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
    final Set<Class<?>> classes = ConcurrentHashMap.newKeySet();
    if (value instanceof Class<?>[]) {
      for (Class<?> clazz : ((Class<?>[]) value)) {
        resolveClasses(clazz, classes);
      }
    } else {
      resolveClasses(value, classes);
    }
    return classes.toArray(new Class[0]);
  }

  private boolean resolveClasses(final Object value, final Set<Class<?>> classes) {
    if (value == null || stopResolving(value.getClass())) {
      return false;
    }
    if (value instanceof Class) {
      resolveClasses((Class<?>) value, classes);
      return true;
    }
    if (value instanceof Collection) {
      Collection<?> collection = (Collection<?>) value;
      if (!collection.isEmpty()) {
        for (Object v : collection) {
          if (!resolveClasses(v, classes)) {
            return true;
          }
        }
      }
      return true;
    }
    resolveSuperClasses(value.getClass(), classes);
    ReflectionUtils.doWithFields(
        value.getClass(),
        new XmlFieldCallback(value, classes),
        new XmlFieldFilter(value.getClass()));
    ReflectionUtils.doWithMethods(
        value.getClass(),
        new XmlMethodCallback(value, classes),
        new XmlMethodFilter(value.getClass()));
    final XmlSeeAlso seeAlso = AnnotationUtils.findAnnotation(value.getClass(), XmlSeeAlso.class);
    if (seeAlso != null) {
      for (Class<?> clazz : seeAlso.value()) {
        resolveClasses(clazz, classes);
      }
    }
    return true;
  }

  private void resolveClasses(final Class<?> clazz, final Set<Class<?>> classes) {
    if (stopResolving(clazz)) {
      return;
    }
    resolveSuperClasses(clazz, classes);
    ReflectionUtils.doWithFields(
        clazz,
        new XmlFieldCallback(null, classes),
        new XmlFieldFilter(clazz));
    ReflectionUtils.doWithMethods(
        clazz,
        new XmlMethodCallback(null, classes),
        new XmlMethodFilter(clazz));
    final XmlSeeAlso seeAlso = AnnotationUtils.findAnnotation(clazz, XmlSeeAlso.class);
    if (seeAlso != null) {
      for (Class<?> c : seeAlso.value()) {
        resolveClasses(c, classes);
      }
    }
  }

  private void resolveSuperClasses(final Class<?> clazz, final Set<Class<?>> classes) {
    if (!stopResolving(clazz)) {
      classes.add(clazz);
      resolveSuperClasses(clazz.getSuperclass(), classes);
    }
  }

  private boolean stopResolving(final Class<?> clazz) {
    return clazz == null
        || (!isAnnotatedWithXml(clazz) && !Collection.class.isAssignableFrom(clazz));
  }

  private boolean isAnnotatedWithXml(final Class<?> clazz) {
    return clazz.isAnnotationPresent(XmlRootElement.class)
        || clazz.isAnnotationPresent(XmlType.class);
  }

  private void processXmlAnnotations(final AnnotatedElement element, final Set<Class<?>> classes) {
    processXmlElement(AnnotationUtils.findAnnotation(element, XmlElement.class), classes);
    Optional
        .ofNullable(AnnotationUtils.findAnnotation(element, XmlElements.class))
        .map(XmlElements::value)
        .ifPresent(a -> Arrays.stream(a).forEach(e -> processXmlElement(e, classes)));

    processXmlElementRef(AnnotationUtils.findAnnotation(element, XmlElementRef.class), classes);
    Optional
        .ofNullable(AnnotationUtils.findAnnotation(element, XmlElementRefs.class))
        .map(XmlElementRefs::value)
        .ifPresent(a -> Arrays.stream(a).forEach(e -> processXmlElementRef(e, classes)));
  }

  private void processXmlElement(final XmlElement annotation, final Set<Class<?>> classes) {
    Optional.ofNullable(annotation)
        .map(XmlElement::type)
        .filter(type -> !type.equals(XmlElement.DEFAULT.class))
        .ifPresent(type -> resolveClasses(type, classes));
  }

  private void processXmlElementRef(final XmlElementRef annotation, final Set<Class<?>> classes) {
    Optional.ofNullable(annotation)
        .map(XmlElementRef::type)
        .filter(type -> !type.equals(XmlElementRef.DEFAULT.class))
        .ifPresent(type -> resolveClasses(type, classes));
  }

  private class XmlFieldCallback implements FieldCallback {

    private final Object value;

    private final Set<Class<?>> classes;

    /**
     * Instantiates a new xml field callback.
     *
     * @param value the value
     * @param classes the classes
     */
    XmlFieldCallback(final Object value, final Set<Class<?>> classes) {
      this.value = value;
      this.classes = classes;
    }

    @Override
    public void doWith(final Field field) throws IllegalArgumentException {
      if (!field.isAccessible()) {
        ReflectionUtils.makeAccessible(field);
      }
      processXmlAnnotations(field, classes);
      if (value == null) {
        if (Collection.class.isAssignableFrom(field.getType())) {
          for (ResolvableType rt : ResolvableType.forField(field).getGenerics()) {
            resolveClasses(rt.resolve(), classes);
          }
        } else {
          resolveClasses(field.getType(), classes);
        }
      } else {
        final Object fieldValue = ReflectionUtils.getField(field, value);
        if (fieldValue != null) {
          if (fieldValue instanceof Collection && ((Collection<?>) fieldValue).isEmpty()) {
            for (ResolvableType rt : ResolvableType.forField(field).getGenerics()) {
              resolveClasses(rt.resolve(), classes);
            }
          } else {
            resolveClasses(fieldValue, classes);
          }
        } else {
          if (Collection.class.isAssignableFrom(field.getType())) {
            for (ResolvableType rt : ResolvableType.forField(field).getGenerics()) {
              resolveClasses(rt.resolve(), classes);
            }
          } else {
            resolveClasses(field.getType(), classes);
          }
        }
      }
    }
  }

  private class XmlMethodCallback implements MethodCallback {

    private final Object value;

    private final Set<Class<?>> classes;

    /**
     * Instantiates a new xml method callback.
     *
     * @param value the value
     * @param classes the classes
     */
    XmlMethodCallback(final Object value, final Set<Class<?>> classes) {
      this.value = value;
      this.classes = classes;
    }

    @Override
    public void doWith(final Method method) throws IllegalArgumentException {
      if (!method.isAccessible()) {
        ReflectionUtils.makeAccessible(method);
      }
      processXmlAnnotations(method, classes);
      if (value == null) {
        if (Collection.class.isAssignableFrom(method.getReturnType())) {
          for (ResolvableType rt : ResolvableType.forMethodReturnType(method).getGenerics()) {
            resolveClasses(rt.resolve(), classes);
          }
        } else {
          resolveClasses(method.getReturnType(), classes);
        }
      } else {
        final Object methodValue = ReflectionUtils.invokeMethod(method, value);
        if (methodValue != null) {
          if (methodValue instanceof Collection && ((Collection<?>) methodValue).isEmpty()) {
            for (ResolvableType rt : ResolvableType.forMethodReturnType(method, value.getClass())
                .getGenerics()) {
              resolveClasses(rt.resolve(), classes);
            }
          } else {
            resolveClasses(methodValue, classes);
          }
        } else {
          resolveClasses(method.getReturnType(), classes);
        }
      }
    }
  }

  private static class XmlFieldFilter implements FieldFilter {

    private XmlAccessType accessType;

    /**
     * Instantiates a new xml field filter.
     *
     * @param clazz the clazz
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

    private XmlAccessType accessType;

    /**
     * Instantiates a new xml method filter.
     *
     * @param clazz the clazz
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

  private static boolean anyXmlAnnotationPresent(final AnnotatedElement element) {
    return Arrays.stream(EXPLICIT_XML_ANNOTATIONS).anyMatch(element::isAnnotationPresent);
  }

}
