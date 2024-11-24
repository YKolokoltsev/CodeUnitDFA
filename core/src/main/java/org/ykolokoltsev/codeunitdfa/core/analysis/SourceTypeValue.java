package org.ykolokoltsev.codeunitdfa.core.analysis;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.checkerframework.dataflow.analysis.AbstractValue;

@Data
@AllArgsConstructor
public class SourceTypeValue implements AbstractValue<SourceTypeValue> {

  private SourceTypeEnum type;

  // TODO: Test for real expressions (where this method is used).
  /**
   * Selects a more specific {@link SourceTypeEnum} value.
   */
  @Override
  public SourceTypeValue leastUpperBound(SourceTypeValue other) {
    if (type != other.type
        && type.getPriority() == other.type.getPriority()
        && getType() != SourceTypeEnum.UNKNOWN) {
      throw new RuntimeException(String.format("incomparable types: %s, %s", type, other.type));
    }

    if (type.getPriority() > other.type.getPriority()) {
      return new SourceTypeValue(type);
    }
    return new SourceTypeValue(other.type);
  }

  @Getter
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public enum SourceTypeEnum {
    /**
     * In backwards analysis expression type is unknown before its declaration.
     */
    UNKNOWN(0),

    /**
     * Given expression is a local variable, that may be a method input parameter.
     */
    LOCAL(1),

    /**
     * A local variable declared within on of the code blocks.
     */
    DECLARED(2),

    /**
     * Field of the code unit owner class or any other object.
     */
    // TODO: use or delete these types.
    CONSTANT(2),
    EXTERNAL_FUNCTION_CALL(2),
    IMPLICIT_MODIFICATION(2);

    private final int priority;
  }
}
