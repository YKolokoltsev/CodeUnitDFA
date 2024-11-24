package org.ykolokoltsev.codeunitdfa.core.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.checkerframework.dataflow.expression.JavaExpression;
import org.ykolokoltsev.codeunitdfa.core.analysis.SourceTypeValue.SourceTypeEnum;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class SourceDataNode {
  // DFA model
  protected final JavaExpression expression;
  // CodeUnitDFA model
  protected final SourceTypeEnum sourceType;

  /**
   * Unified name of the ArchUnit object wrapping data source.
   */
  public abstract String getUnitName();
}
