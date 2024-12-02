package org.ykolokoltsev.codeunitdfa.core.analysis;

import com.tngtech.archunit.core.domain.JavaField;
import java.util.Optional;
import org.checkerframework.dataflow.analysis.BackwardAnalysisImpl;
import org.checkerframework.dataflow.cfg.node.AssignmentNode;
import org.checkerframework.dataflow.cfg.node.FieldAccessNode;
import org.checkerframework.dataflow.cfg.node.Node;

public class JavaFieldAnalysis
    extends BackwardAnalysisImpl<SourceTypeValue, DataSourceStore, DataSourceTransfer>
    implements DataSourceBackwardAnalysis {

  private final JavaField target;

  public JavaFieldAnalysis(final JavaField target) {
    super();
    this.transferFunction = new DataSourceTransfer(this);
    this.target = target;
  }

  /**
   * Extract source from AssignmentNode, if value is assigned to corresponding {@link #target}
   * java field.
   */
  @Override
  public Optional<Node> getSourceNode(Node node) {
    if (node instanceof AssignmentNode) {
      final Node assignmentTarget = ((AssignmentNode) node).getTarget();
      if (assignmentTarget instanceof FieldAccessNode) {
        final FieldAccessNode fieldAccess = (FieldAccessNode) assignmentTarget;
        final String fieldSignature = String.format(
            "JavaField{%s.%s}",
            fieldAccess.getReceiver().getType(),
            fieldAccess.getFieldName());
        if (target.toString().equals(fieldSignature)) {
          final Node assignmentSource = ((AssignmentNode) node).getExpression();
          return Optional.of(assignmentSource);
        }
      }
    }
    return Optional.empty();
  }
}
