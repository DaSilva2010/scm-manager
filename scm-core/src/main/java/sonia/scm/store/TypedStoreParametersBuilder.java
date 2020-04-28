/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.store;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import sonia.scm.repository.Repository;
import java.util.function.Function;

/**
 * Builder for {@link TypedStoreParameters}.
 *
 * @param <T> type of data
 * @param <S> type of store
 */
public final class TypedStoreParametersBuilder<T,S> {

  private final TypedStoreParametersImpl<T> parameters;
  private final Function<TypedStoreParameters<T>, S> factory;

  TypedStoreParametersBuilder(Class<T> type, Function<TypedStoreParameters<T>, S> factory) {
    this.factory = factory;
    this.parameters = new TypedStoreParametersImpl<>(type);
  }

  /**
   * Use this to set the name for the store.
   * @param name The name for the store.
   * @return Floating API to either specify a repository or directly build a global store.
   */
  public OptionalRepositoryBuilder withName(String name) {
    parameters.setName(name);
    return new OptionalRepositoryBuilder();
  }

  @Getter
  @Setter(AccessLevel.PRIVATE)
  @RequiredArgsConstructor
  private static class TypedStoreParametersImpl<T> implements TypedStoreParameters<T> {
    private final Class<T> type;
    private String name;
    private String repositoryId;
  }

  public class OptionalRepositoryBuilder {

    /**
     * Use this to create or get a store for a specific repository. This step is optional. If you
     * want to have a global store, omit this.
     * @param repository The optional repository for the store.
     * @return Floating API to finish the call.
     */
    public OptionalRepositoryBuilder forRepository(Repository repository) {
      parameters.setRepositoryId(repository.getId());
      return this;
    }

    /**
     * Use this to create or get a store for a specific repository. This step is optional. If you
     * want to have a global store, omit this.
     * @param repositoryId The id of the optional repository for the store.
     * @return Floating API to finish the call.
     */
    public OptionalRepositoryBuilder forRepository(String repositoryId) {
      parameters.setRepositoryId(repositoryId);
      return this;
    }

    /**
     * Creates or gets the store with the given name and (if specified) the given repository. If no
     * repository is given, the store will be global.
     */
    public S build(){
      return factory.apply(parameters);
    }
  }

}
