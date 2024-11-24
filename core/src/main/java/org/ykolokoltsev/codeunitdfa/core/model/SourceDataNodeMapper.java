package org.ykolokoltsev.codeunitdfa.core.model;

import com.sun.source.tree.VariableTree;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaParameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Name;
import org.checkerframework.dataflow.cfg.ControlFlowGraph;
import org.checkerframework.dataflow.cfg.UnderlyingAST.CFGMethod;
import org.checkerframework.dataflow.expression.JavaExpression;
import org.checkerframework.dataflow.expression.LocalVariable;
import org.ykolokoltsev.codeunitdfa.core.analysis.SourceTypeValue;
import org.ykolokoltsev.codeunitdfa.core.analysis.SourceTypeValue.SourceTypeEnum;
import org.ykolokoltsev.codeunitdfa.core.exception.UnsupportedSourceTypeException;

public class SourceDataNodeMapper {

  private final JavaCodeUnit codeUnit;
  private final Map<Name, JavaParameter> astToArchUnitParamMap = new HashMap<>();

  public SourceDataNodeMapper(
      final ControlFlowGraph cfg,
      final JavaCodeUnit codeUnit
  ) {
    this.codeUnit = codeUnit;
    if (cfg.getUnderlyingAST() instanceof CFGMethod) {
      final List<? extends VariableTree> parameters =
          ((CFGMethod) cfg.getUnderlyingAST()).getMethod().getParameters();
      for (int i = 0; i < parameters.size(); i++) {
        this.astToArchUnitParamMap.put(parameters.get(i).getName(), codeUnit.getParameters().get(i));
      }
    }
  }

  /**
   * Maps CFG nodes from DFA model onto ArchUnit model using {@link SourceTypeValue} information collected
   * during analysis.
   */
  public SourceDataNode toSourceDataNode(
      JavaExpression javaExpression,
      SourceTypeValue value
  ) {
    switch (value.getType()) {
      case FIELD:
        return fromField(javaExpression);
      case CONSTANT:
        return fromConstant(javaExpression);
      case PARAMETER:
        return fromParameter(javaExpression);
      default:
        throw new UnsupportedSourceTypeException();
    }
  }

  /**
   * Search for the corresponding field in the full classes list of the ArchUnit model?
   */
  private JavaMemberSourceDataNodeImpl fromField(
      JavaExpression javaExpression
  ) {
    //TODO: Implement.
    return null;
  }

  /**
   * Constant is explicitly owned by the {@link #codeUnit}. This is always a terminal node.
   */
  private JavaMemberSourceDataNodeImpl fromConstant(
      JavaExpression javaExpression
  ) {
    return JavaMemberSourceDataNodeImpl.builder()
        .expressionOwner(codeUnit)
        .expression(javaExpression)
        .sourceType(SourceTypeEnum.CONSTANT)
        .build();
  }

  /**
   * Find code unit {@link JavaParameter} (ArchUnit) for the {@link LocalVariable} (DFA) defined at
   * method argument list.
   */
  private JavaParameterSourceDataNodeImpl fromParameter(
      JavaExpression javaExpression
  ) {
    final Name parameterName = ((LocalVariable) javaExpression).getElement().getSimpleName();
    return JavaParameterSourceDataNodeImpl.builder()
        .parameter(astToArchUnitParamMap.get(parameterName))
        .expression(javaExpression)
        .build();
  }
}
