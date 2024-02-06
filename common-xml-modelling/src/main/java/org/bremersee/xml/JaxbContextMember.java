/*
 * Copyright 2022 the original author or authors.
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

import static java.util.Objects.isNull;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * The jaxb context member.
 *
 * @author Christian Bremer
 */
public interface JaxbContextMember {

  /**
   * By class builder.
   *
   * @param clazz the clazz
   * @return the clazz builder
   */
  static ClazzBuilder byClass(Class<?> clazz) {
    return new ClazzBuilderImpl(clazz);
  }

  /**
   * By package builder.
   *
   * @param pakkage the pakkage
   * @return the pakkage builder
   */
  static PakkageBuilder byPackage(Package pakkage) {
    return new PakkageBuilderImpl(pakkage);
  }

  /**
   * Gets clazz.
   *
   * @return the clazz
   */
  Class<?> getClazz();

  /**
   * Gets clazz element schema location.
   *
   * @return the clazz element schema location
   */
  default String getClazzElementSchemaLocation() {
    return null;
  }

  /**
   * Gets clazz type schema location.
   *
   * @return the clazz type schema location
   */
  default String getClazzTypeSchemaLocation() {
    return null;
  }

  /**
   * Gets pakkage.
   *
   * @return the pakkage
   */
  default Package getPakkage() {
    return null;
  }

  /**
   * Gets pakkage schema location.
   *
   * @return the pakkage schema location
   */
  default String getPakkageSchemaLocation() {
    return null;
  }

  /**
   * The pakkage builder.
   */
  interface PakkageBuilder {

    /**
     * Schema location.
     *
     * @param schemaLocation the schema location
     * @return the pakkage builder
     */
    PakkageBuilder schemaLocation(String schemaLocation);

    /**
     * Build jaxb context member.
     *
     * @return the jaxb context member
     */
    JaxbContextMember build();
  }

  /**
   * The pakkage builder implementation.
   */
  @SuppressWarnings("SameNameButDifferent")
  @EqualsAndHashCode
  @ToString
  @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
  class PakkageBuilderImpl implements PakkageBuilder {

    private final Package pakkage;

    private String schemaLocation;

    @Override
    public PakkageBuilder schemaLocation(String schemaLocation) {
      this.schemaLocation = schemaLocation;
      return this;
    }

    @Override
    public JaxbContextMember build() {
      if (isNull(this.pakkage)) {
        throw new IllegalStateException("Package of jaxb context must be present.");
      }
      return new JaxbContextMemberImpl(this.pakkage, this.schemaLocation);
    }
  }

  /**
   * The clazz builder.
   */
  interface ClazzBuilder {

    /**
     * Clazz element schema location.
     *
     * @param clazzElementSchemaLocation the clazz element schema location
     * @return the clazz builder
     */
    ClazzBuilder clazzElementSchemaLocation(String clazzElementSchemaLocation);

    /**
     * Clazz type schema location.
     *
     * @param clazzTypeSchemaLocation the clazz type schema location
     * @return the clazz builder
     */
    ClazzBuilder clazzTypeSchemaLocation(String clazzTypeSchemaLocation);

    /**
     * Build jaxb context member.
     *
     * @return the jaxb context member
     */
    JaxbContextMember build();
  }

  /**
   * The clazz builder implementation.
   */
  @SuppressWarnings("SameNameButDifferent")
  @EqualsAndHashCode
  @ToString
  @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
  class ClazzBuilderImpl implements ClazzBuilder {

    private final Class<?> clazz;

    private String clazzElementSchemaLocation;

    private String clazzTypeSchemaLocation;

    @Override
    public ClazzBuilder clazzElementSchemaLocation(String clazzElementSchemaLocation) {
      this.clazzElementSchemaLocation = clazzElementSchemaLocation;
      return this;
    }

    @Override
    public ClazzBuilder clazzTypeSchemaLocation(String clazzTypeSchemaLocation) {
      this.clazzTypeSchemaLocation = clazzTypeSchemaLocation;
      return this;
    }

    @Override
    public JaxbContextMember build() {
      if (isNull(this.clazz)) {
        throw new IllegalStateException("Class of jaxb context must be present.");
      }
      return new JaxbContextMemberImpl(
          this.clazz,
          this.clazzElementSchemaLocation,
          this.clazzTypeSchemaLocation);
    }
  }

  /**
   * The jaxb context member implementation.
   */
  @SuppressWarnings("SameNameButDifferent")
  @Getter
  @EqualsAndHashCode
  @ToString
  class JaxbContextMemberImpl implements JaxbContextMember {

    private final Class<?> clazz;

    private final String clazzElementSchemaLocation;

    private final String clazzTypeSchemaLocation;

    private final Package pakkage;

    private final String pakkageSchemaLocation;

    /**
     * Instantiates a new jaxb context member.
     *
     * @param clazz the clazz
     * @param clazzElementSchemaLocation the clazz element schema location
     * @param clazzTypeSchemaLocation the clazz type schema location
     * @param pakkage the pakkage
     * @param pakkageSchemaLocation the pakkage schema location
     */
    JaxbContextMemberImpl(
        Class<?> clazz,
        String clazzElementSchemaLocation,
        String clazzTypeSchemaLocation,
        Package pakkage,
        String pakkageSchemaLocation) {
      this.clazz = clazz;
      this.clazzElementSchemaLocation = clazzElementSchemaLocation;
      this.clazzTypeSchemaLocation = clazzTypeSchemaLocation;
      this.pakkage = pakkage;
      this.pakkageSchemaLocation = pakkageSchemaLocation;
    }

    /**
     * Instantiates a new jaxb context member.
     *
     * @param clazz the clazz
     * @param clazzElementSchemaLocation the clazz element schema location
     * @param clazzTypeSchemaLocation the clazz type schema location
     */
    JaxbContextMemberImpl(
        Class<?> clazz,
        String clazzElementSchemaLocation,
        String clazzTypeSchemaLocation) {
      this(clazz, clazzElementSchemaLocation, clazzTypeSchemaLocation, null, null);
    }

    /**
     * Instantiates a new jaxb context member.
     *
     * @param pakkage the pakkage
     * @param schemaLocation the schema location
     */
    JaxbContextMemberImpl(Package pakkage, String schemaLocation) {
      this(null, null, null, pakkage, schemaLocation);
    }
  }

}
