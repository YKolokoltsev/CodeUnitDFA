package org.ykolokoltsev.codeunitdfa.core.analysis;

import com.tngtech.archunit.core.domain.JavaField;
import org.checkerframework.dataflow.analysis.BackwardAnalysisImpl;
import org.checkerframework.dataflow.cfg.ControlFlowGraph;
import org.checkerframework.dataflow.cfg.block.Block;
import org.checkerframework.dataflow.cfg.node.FieldAccessNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.javacutil.BugInCF;

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

  @Override
  public void performAnalysis(ControlFlowGraph cfg) {
    if (isRunning) {
      throw new BugInCF("performAnalysis() shouldn't be called when the analysis is running.");
    }
    isRunning = true;
    try {
      init(cfg);
      while (!worklist.isEmpty()) {
        Block b = worklist.poll();
        performAnalysisBlock(b);
      }
    } finally {
      assert isRunning;
      // In case performAnalysisBlock crashed, reset isRunning to false.
      isRunning = false;
    }
  }
}
