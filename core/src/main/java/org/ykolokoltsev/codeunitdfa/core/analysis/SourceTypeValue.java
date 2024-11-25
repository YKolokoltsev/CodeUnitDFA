package org.ykolokoltsev.codeunitdfa.core.analysis;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.checkerframework.dataflow.analysis.AbstractValue;
import org.checkerframework.dataflow.expression.JavaExpression;
import org.checkerframework.dataflow.expression.LocalVariable;
import org.checkerframework.dataflow.expression.ValueLiteral;

@Data
@AllArgsConstructor
public class SourceTypeValue implements AbstractValue<SourceTypeValue> {

  private SourceTypeEnum type;

  /**
   * Initializes value based on the source node type.
   */
  public SourceTypeValue(final JavaExpression expression) {
    if (expression instanceof ValueLiteral) {
      type = SourceTypeEnum.CONSTANT;
    } else if (expression instanceof LocalVariable) {
      type = SourceTypeEnum.LOCAL;
    } else {
      type = SourceTypeEnum.UNKNOWN;
    }
  }

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
     * Expression is ether undefined or not supported.
     */
    UNKNOWN(0),

    /**
     * Given expression is a local variable, that may be a method input parameter.
     */
    LOCAL(1),

    /**
     * A local variable declared within the code block.
     */
    DECLARED(2),

    /**
     * Constant literal of any type.
     */
    CONSTANT(2),
    // TODO: use or delete these types.
    EXTERNAL_FUNCTION_CALL(2),
    IMPLICIT_MODIFICATION(2);

    private final int priority;
  }
}
