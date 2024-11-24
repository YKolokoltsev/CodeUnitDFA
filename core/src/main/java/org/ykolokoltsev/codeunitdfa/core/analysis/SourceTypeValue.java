package org.ykolokoltsev.codeunitdfa.core.analysis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.dataflow.analysis.AbstractValue;

@AllArgsConstructor
public class SourceTypeValue implements AbstractValue<SourceTypeValue> {

  /**
   * TODO: add distinct origin types, e.g. LOCAL, CONSTANT, PARAMETER, etc.
   */
  @Getter
  @Setter
  private SourceTypeEnum type;

  /**
   * When joining two origin marks for the same JavaExpression node from
   * two alternative flows, the origin mark is true in case if any flow
   * affects target.
   * <p/>
   * @param other mark for the same JavaExpression from another flow
   * @return new initialized OriginMarkValue
   */
  @Override
  public SourceTypeValue leastUpperBound(SourceTypeValue other) {
    if (type != other.type) {
      throw new RuntimeException("inconsistent value types");
    }

    return new SourceTypeValue(type);
  }

  public enum SourceTypeEnum {
    /**
     * When this AbstractValue is used for backward analysis,
     * source expression type may be unknown before reaching its
     * declaration.
     */
    UNKNOWN,

    /**
     * Local variable, declared within code unit. May be present only as a
     * transitive dependency and should never appear in the resulting DataSourceStore.
     */
    LOCAL,

    /**
     * An input parameter of a method or constructor.
     */
    PARAMETER,

    /**
     * Field of the code unit owner class or any other object.
     */
    FIELD,
    CONSTANT,
    EXTERNAL_FUNCTION_CALL,
    IMPLICIT_MODIFICATION
  }
}
