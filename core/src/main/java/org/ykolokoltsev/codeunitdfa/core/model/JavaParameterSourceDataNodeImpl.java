package org.ykolokoltsev.codeunitdfa.core.model;

import com.tngtech.archunit.core.domain.JavaParameter;
import lombok.Builder;
import lombok.Getter;
import org.checkerframework.dataflow.expression.JavaExpression;
import org.ykolokoltsev.codeunitdfa.core.analysis.SourceTypeValue.SourceTypeEnum;

@Getter
public class JavaParameterSourceDataNodeImpl extends SourceDataNode {
  private final JavaParameter parameter;

  @Builder
  public JavaParameterSourceDataNodeImpl(
      final JavaExpression expression,
      final JavaParameter parameter) {
    super(expression, SourceTypeEnum.LOCAL);
    this.parameter = parameter;
  }

  @Override
  public String getUnitName() {
    return expression.toString();
  }

  @Override
  public String getOwnerTypeName() {
    return parameter.getClass().getSimpleName();
  }
}
