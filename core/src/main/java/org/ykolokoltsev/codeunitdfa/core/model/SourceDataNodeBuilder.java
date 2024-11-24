package org.ykolokoltsev.codeunitdfa.core.model;

import com.sun.source.tree.VariableTree;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaParameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.lang.model.element.Name;
import org.checkerframework.dataflow.cfg.ControlFlowGraph;
import org.checkerframework.dataflow.cfg.UnderlyingAST.CFGMethod;
import org.checkerframework.dataflow.expression.JavaExpression;
import org.checkerframework.dataflow.expression.LocalVariable;
import org.ykolokoltsev.codeunitdfa.core.analysis.SourceTypeValue;
import org.ykolokoltsev.codeunitdfa.core.exception.UnsupportedSourceTypeException;

public class SourceDataNodeBuilder {

  // DataFlow model
  private final ControlFlowGraph cfg;

  // ArchUnit model
  private final JavaCodeUnit codeUnit;

  // Internal (mixed) data
  private final Map<Name, JavaParameter> parameterMap = new HashMap<>();

  public SourceDataNodeBuilder(
      final ControlFlowGraph cfg,
      final JavaCodeUnit codeUnit
  ) {
    this.cfg = cfg;
    this.codeUnit = codeUnit;
    if (cfg.getUnderlyingAST() instanceof CFGMethod) {
      final List<? extends VariableTree> parameters =
          ((CFGMethod) cfg.getUnderlyingAST()).getMethod().getParameters();
      for (int i = 0; i < parameters.size(); i++) {
        this.parameterMap.put(parameters.get(i).getName(), codeUnit.getParameters().get(i));
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
      case LOCAL:
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
    //TODO: Implement.
    return null;
  }

  /**
   * Find {@link JavaParameter} node in the ArchUnit model having the same name as
   * {@link LocalVariable} from the DFA result.
   */
  private JavaParameterSourceDataNodeImpl fromParameter(
      JavaExpression javaExpression
  ) {
    assert javaExpression instanceof LocalVariable;
    final Name parameterName = ((LocalVariable) javaExpression).getElement().getSimpleName();
    final JavaParameter javaParameter = Optional.ofNullable(parameterMap.get(parameterName)).orElseThrow();
    return JavaParameterSourceDataNodeImpl.builder()
        .parameter(javaParameter)
        .expression(javaExpression)
        .build();
  }
}
