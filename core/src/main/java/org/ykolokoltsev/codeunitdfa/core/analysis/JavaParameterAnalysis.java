package org.ykolokoltsev.codeunitdfa.core.analysis;

import com.tngtech.archunit.core.domain.JavaParameter;
import java.util.Optional;
import org.checkerframework.dataflow.analysis.BackwardAnalysisImpl;
import org.checkerframework.dataflow.cfg.node.MethodAccessNode;
import org.checkerframework.dataflow.cfg.node.MethodInvocationNode;
import org.checkerframework.dataflow.cfg.node.Node;

public class JavaParameterAnalysis
    extends BackwardAnalysisImpl<SourceTypeValue, DataSourceStore, DataSourceTransfer>
    implements DataSourceBackwardAnalysis {

  private final JavaParameter target;

  public JavaParameterAnalysis(final JavaParameter target) {
    super();
    this.transferFunction = new DataSourceTransfer(this);
    this.target = target;
  }

  @Override
  public Optional<Node> getSourceNode(Node n) {
    if (n instanceof MethodInvocationNode) {
      final MethodInvocationNode invocationNode = (MethodInvocationNode) n;
      final MethodAccessNode method = invocationNode.getTarget();
      final String methodSignature = String.format(
          "JavaMethod{%s.%s}",
          method.getReceiver().getType().toString(),
          method.getMethod().toString());
      final String targetMethodSignature = target.getOwner().toString().replaceAll(" ", "");
      if (targetMethodSignature.equals(methodSignature)) {
        return Optional.of(invocationNode.getArgument(target.getIndex()));
      }
    }
    return Optional.empty();
  }
}
