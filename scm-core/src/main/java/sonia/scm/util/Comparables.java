package sonia.scm.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

public final class Comparables {

  private static final CacheLoader<Class, BeanInfo> beanInfoCacheLoader = new CacheLoader<Class, BeanInfo>() {
    @Override
    public BeanInfo load(Class type) throws IntrospectionException {
      return Introspector.getBeanInfo(type);
    }
  };

  private static final LoadingCache<Class, BeanInfo> beanInfoCache = CacheBuilder.newBuilder()
    .maximumSize(50) // limit the cache to avoid consuming to much memory on miss usage
    .build(beanInfoCacheLoader);

  private Comparables() {
  }

  public static <T> Comparator<T> comparator(Class<T> type, String sortBy) {
    BeanInfo info = createBeanInfo(type);
    PropertyDescriptor propertyDescriptor = findPropertyDescriptor(sortBy, info);

    Method readMethod = propertyDescriptor.getReadMethod();
    checkIfPropertyIsComparable(readMethod, sortBy);

    return new MethodComparator<>(readMethod);
  }

  private static void checkIfPropertyIsComparable(Method readMethod, String sortBy) {
    checkArgument(isReturnTypeComparable(readMethod), "property %s is not comparable", sortBy);
  }

  private static boolean isReturnTypeComparable(Method readMethod) {
    return Comparable.class.isAssignableFrom(readMethod.getReturnType());
  }

  private static PropertyDescriptor findPropertyDescriptor(String sortBy, BeanInfo info) {
    PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();

    Optional<PropertyDescriptor> sortByPropertyDescriptor = Arrays.stream(propertyDescriptors)
      .filter(p -> p.getName().equals(sortBy))
      .findFirst();

    return sortByPropertyDescriptor.orElseThrow(() -> new IllegalArgumentException("could not find property " + sortBy));
  }

  private static <T> BeanInfo createBeanInfo(Class<T> type) {
    return beanInfoCache.getUnchecked(type);
  }

  private static class MethodComparator<T> implements Comparator<T> {

    private final Method readMethod;

    private MethodComparator(Method readMethod) {
      this.readMethod = readMethod;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int compare(T left, T right) {
      try {
        Comparable leftResult = (Comparable) readMethod.invoke(left);
        Comparable rightResult = (Comparable) readMethod.invoke(right);
        return leftResult.compareTo(rightResult);
      } catch (IllegalAccessException | InvocationTargetException ex) {
        throw new IllegalArgumentException("failed to invoke read method", ex);
      }
    }
  }

}