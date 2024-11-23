package org.ykolokoltsev.codeunitdfa.core.analysis;

import org.checkerframework.dataflow.analysis.BackwardAnalysisImpl;
import org.checkerframework.dataflow.cfg.node.FieldAccessNode;
import org.checkerframework.dataflow.cfg.node.Node;

public class DataOriginAnalysis extends
    BackwardAnalysisImpl<OriginTypeValue, DataOriginStore, DataOriginTransfer> {

  private final String targetFieldName;

  public DataOriginAnalysis(String targetFieldName) {
    super();
    this.targetFieldName = targetFieldName;
    this.transferFunction = new DataOriginTransfer(this);
  }

  public boolean isTargetExpression(Node node) {
    if (node instanceof FieldAccessNode) {
      final FieldAccessNode targetField = (FieldAccessNode) node;
      return targetField.getFieldName().equals(targetFieldName);

    } else {
      return false;
    }
  }
}
