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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.File;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

final class TypedStoreContext<T> {

  private final JAXBContext jaxbContext;
  private final TypedStoreParameters<T> parameters;

  private TypedStoreContext(JAXBContext jaxbContext, TypedStoreParameters<T> parameters) {
    this.jaxbContext = jaxbContext;
    this.parameters = parameters;
  }

  static <T> TypedStoreContext<T> of(TypedStoreParameters<T> parameters) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(parameters.getType());
      return new TypedStoreContext<>(jaxbContext, parameters);
    } catch (JAXBException e) {
      throw new StoreException("failed to create context for store");
    }
  }

  T unmarshall(File file) {
    AtomicReference<T> ref = new AtomicReference<>();
    withUnmarshaller(unmarshaller -> {
      T value = parameters.getType().cast(unmarshaller.unmarshal(file));
      ref.set(value);
    });
    return ref.get();
  }

  void marshal(Object object, File file) {
    withMarshaller(marshaller -> marshaller.marshal(object, file));
  }

  void withMarshaller(ThrowingConsumer<Marshaller> consumer) {
    Marshaller marshaller = createMarshaller();
    ClassLoader contextClassLoader = null;
    Optional<ClassLoader> classLoader = parameters.getClassLoader();
    if (classLoader.isPresent()) {
      contextClassLoader = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(classLoader.get());
    }
    try {
      consumer.consume(marshaller);
    } catch (Exception e) {
      throw new StoreException("failure during work with marshaller", e);
    } finally {
      if (contextClassLoader != null) {
        Thread.currentThread().setContextClassLoader(contextClassLoader);
      }
    }
  }

  void withUnmarshaller(ThrowingConsumer<Unmarshaller> consumer) {
    Unmarshaller unmarshaller = createUnmarshaller();
    ClassLoader contextClassLoader = null;
    Optional<ClassLoader> classLoader = parameters.getClassLoader();
    if (classLoader.isPresent()) {
      contextClassLoader = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(classLoader.get());
    }
    try {
      consumer.consume(unmarshaller);
    } catch (Exception e) {
      throw new StoreException("failure during work with unmarshaller", e);
    } finally {
      if (contextClassLoader != null) {
        Thread.currentThread().setContextClassLoader(contextClassLoader);
      }
    }
  }

  private Marshaller createMarshaller() {
    try {
      Marshaller marshaller = jaxbContext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      for (XmlAdapter<?, ?> adapter : parameters.getAdapters()) {
        marshaller.setAdapter(adapter);
      }
      return marshaller;
    } catch (JAXBException e) {
      throw new StoreException("could not create marshaller", e);
    }
  }

  private Unmarshaller createUnmarshaller() {
    try {
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      for (XmlAdapter<?, ?> adapter : parameters.getAdapters()) {
        unmarshaller.setAdapter(adapter);
      }
      return unmarshaller;
    } catch (JAXBException e) {
      throw new StoreException("could not create unmarshaller", e);
    }
  }

  @FunctionalInterface
  interface ThrowingConsumer<T> {
    @SuppressWarnings("java:S112") // we need to throw Exception here
    void consume(T item) throws Exception;
  }

}
