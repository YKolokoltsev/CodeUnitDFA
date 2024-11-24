package org.ykolokoltsev.codeunitdfa.core.analysis;

import com.tngtech.archunit.core.domain.JavaField;
import org.checkerframework.dataflow.analysis.BackwardAnalysisImpl;
import org.checkerframework.dataflow.cfg.node.FieldAccessNode;
import org.checkerframework.dataflow.cfg.node.Node;

public class JavaFieldAnalysis
    extends BackwardAnalysisImpl<SourceTypeValue, DataSourceStore, DataSourceTransfer> {

  private final JavaField target;

  public JavaFieldAnalysis(final JavaField target) {
    super();
    this.transferFunction = new DataSourceTransfer(this);
    this.target = target;
  }

  /**
   * Check if node visited by transfer function is same as target JavaField.
   */
  public boolean isTargetNode(Node node) {
    if (node instanceof FieldAccessNode) {
      final FieldAccessNode fieldAccessNode = (FieldAccessNode) node;
      return fieldAccessNode.getFieldName().equals(target.getName());

    } else {
      return false;
    }
  }
}
