/*
 * Copyright 2019 the original author or authors.
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

  @Override
  public Set<String> resolvePackages(final Object value) {
    final Set<String> packages = ConcurrentHashMap.newKeySet();
    resolvePackages(value, packages);
    return packages;
  }

  private boolean resolvePackages(final Object value, final Set<String> packages) {
    if (value == null || stopResolving(value.getClass())) {
      return false;
    }
    if (value instanceof Class) {
      resolvePackages((Class<?>) value, packages);
      return true;
    }
    if (value instanceof Collection) {
      Collection<?> collection = (Collection<?>) value;
      if (!collection.isEmpty()) {
        for (Object v : collection) {
          if (!resolvePackages(v, packages)) {
            return true;
          }
        }
      }
      return true;
    }
    packages.add(value.getClass().getPackage().getName());
    ReflectionUtils.doWithFields(
        value.getClass(),
        new XmlFieldCallback(value, packages),
        new XmlFieldFilter(value.getClass()));
    ReflectionUtils.doWithMethods(
        value.getClass(),
        new XmlMethodCallback(value, packages),
        new XmlMethodFilter(value.getClass()));
    final XmlSeeAlso seeAlso = AnnotationUtils.findAnnotation(value.getClass(), XmlSeeAlso.class);
    if (seeAlso != null) {
      for (Class<?> clazz : seeAlso.value()) {
        resolvePackages(clazz, packages);
      }
    }
    return true;
  }

  private void resolvePackages(final Class<?> clazz, final Set<String> packages) {
    if (stopResolving(clazz)) {
      return;
    }
    packages.add(clazz.getPackage().getName());
    ReflectionUtils.doWithFields(
        clazz,
        new XmlFieldCallback(null, packages),
        new XmlFieldFilter(clazz));
    ReflectionUtils.doWithMethods(
        clazz,
        new XmlMethodCallback(null, packages),
        new XmlMethodFilter(clazz));
    final XmlSeeAlso seeAlso = AnnotationUtils.findAnnotation(clazz, XmlSeeAlso.class);
    if (seeAlso != null) {
      for (Class<?> c : seeAlso.value()) {
        resolvePackages(c, packages);
      }
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

  private class XmlFieldCallback implements FieldCallback {

    private final Object value;

    private final Set<String> packages;

    /**
     * Instantiates a new Xml field callback.
     *
     * @param value the value
     * @param packages the packages
     */
    public XmlFieldCallback(final Object value, final Set<String> packages) {
      this.value = value;
      this.packages = packages;
    }

    @Override
    public void doWith(final Field field) throws IllegalArgumentException {
      if (!field.isAccessible()) {
        ReflectionUtils.makeAccessible(field);
      }
      if (value == null) {
        if (Collection.class.isAssignableFrom(field.getType())) {
          for (ResolvableType rt : ResolvableType.forField(field).getGenerics()) {
            resolvePackages(rt.resolve(), packages);
          }
        } else {
          resolvePackages(field.getType(), packages);
        }
      } else {
        final Object fieldValue = ReflectionUtils.getField(field, value);
        if (fieldValue != null) {
          if (fieldValue instanceof Collection && ((Collection<?>) fieldValue).isEmpty()) {
            for (ResolvableType rt : ResolvableType.forField(field).getGenerics()) {
              resolvePackages(rt.resolve(), packages);
            }
          } else {
            resolvePackages(fieldValue, packages);
          }
        } else {
          resolvePackages(field.getType(), packages);
        }
      }
    }
  }

  private class XmlMethodCallback implements MethodCallback {

    private final Object value;

    private final Set<String> packages;

    /**
     * Instantiates a new Xml method callback.
     *
     * @param value the value
     * @param packages the packages
     */
    public XmlMethodCallback(final Object value, final Set<String> packages) {
      this.value = value;
      this.packages = packages;
    }

    @Override
    public void doWith(final Method method) throws IllegalArgumentException {
      if (!method.isAccessible()) {
        ReflectionUtils.makeAccessible(method);
      }
      if (value == null) {
        if (Collection.class.isAssignableFrom(method.getReturnType())) {
          for (ResolvableType rt : ResolvableType.forMethodReturnType(method).getGenerics()) {
            resolvePackages(rt.resolve(), packages);
          }
        } else {
          resolvePackages(method.getReturnType(), packages);
        }
      } else {
        final Object methodValue = ReflectionUtils.invokeMethod(method, value);
        if (methodValue != null) {
          if (methodValue instanceof Collection && ((Collection<?>) methodValue).isEmpty()) {
            for (ResolvableType rt : ResolvableType.forMethodReturnType(method, value.getClass())
                .getGenerics()) {
              resolvePackages(rt.resolve(), packages);
            }
          } else {
            resolvePackages(methodValue, packages);
          }
        } else {
          resolvePackages(method.getReturnType(), packages);
        }
      }
    }
  }

  private static class XmlFieldFilter implements FieldFilter {

    private XmlAccessType accessType;

    /**
     * Instantiates a new Xml field filter.
     *
     * @param clazz the clazz
     */
    public XmlFieldFilter(final Class<?> clazz) {
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
     * Instantiates a new Xml method filter.
     *
     * @param clazz the clazz
     */
    public XmlMethodFilter(final Class<?> clazz) {
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

}
