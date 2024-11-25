package org.ykolokoltsev.codeunitdfa.core.analysis;

import java.util.stream.Collector;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CollectionUtils {

  /**
   * Collector that allows to reduce object stream to a single element,
   * checking that a result is unique.
   * @param <T> - any type
   * @return - object of type T
   */
  public static <T> Collector<T, ?, T> toSingleton() {
    return Collectors.collectingAndThen(
        Collectors.toList(),
        list -> {
          if (list.size() != 1) {
            throw new IllegalStateException();

          }
          return list.get(0);
        }
    );
  }

  /**
   * Collector that allows to reduce object stream to a single element,
   * reducing all same elements into single element.
   * @param <T> - any type
   * @return - object of type T
   */
  public static <T> Collector<T, ?, T> reduceSameToSingleton() {
    return Collectors.collectingAndThen(
        Collectors.toSet(),
        set -> {
          if (set.size() != 1) {
            throw new IllegalStateException();

          }
          return set.iterator().next();
        }
    );
  }

  /**
   * Collector that allows to reduce object stream to a single element,
   * and in case if stream has no elements, this collector returns a specified default value.
   * @param fallback - default fallback value
   * @param <T> - any type
   * @return - object of type T
   */
  public static <T> Collector<T, ?, T> toSingletonFallback(T fallback) {
    return Collectors.collectingAndThen(
        Collectors.toList(),
        list -> {
          if (list.size() > 1) {
            throw new IllegalStateException();

          } else if (list.size() == 1) {
            return list.get(0);
          }

          return fallback;
        }
    );
  }

}
