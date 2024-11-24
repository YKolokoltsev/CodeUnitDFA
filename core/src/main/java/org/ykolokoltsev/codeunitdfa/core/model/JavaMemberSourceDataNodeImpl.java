package org.ykolokoltsev.codeunitdfa.core.model;

import com.tngtech.archunit.core.domain.JavaMember;
import lombok.Builder;
import lombok.Getter;
import org.checkerframework.dataflow.expression.JavaExpression;
import org.ykolokoltsev.codeunitdfa.core.analysis.SourceTypeValue.SourceTypeEnum;

@Getter
public class JavaMemberSourceDataNodeImpl extends SourceDataNode {
  // ArchUnit model node
  private final JavaMember expressionOwner;

  @Builder
  public JavaMemberSourceDataNodeImpl(
      final JavaExpression expression,
      final SourceTypeEnum sourceType,
      final JavaMember expressionOwner
  ) {
    super(expression, sourceType);
    this.expressionOwner = expressionOwner;
  }

  @Override
  public String getUnitName() {
    return expressionOwner.getName();
  }
}
