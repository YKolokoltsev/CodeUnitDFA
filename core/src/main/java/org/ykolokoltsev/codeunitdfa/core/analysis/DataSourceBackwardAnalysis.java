package org.ykolokoltsev.codeunitdfa.core.analysis;

import java.util.Optional;
import org.checkerframework.dataflow.cfg.node.Node;

public interface DataSourceBackwardAnalysis {
  /**
   * Check if node visited by transfer function is same as target JavaField.
   */
  Optional<Node> getSourceNode(Node n);
}
