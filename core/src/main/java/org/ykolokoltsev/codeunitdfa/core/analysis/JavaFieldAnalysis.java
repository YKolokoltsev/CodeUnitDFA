package org.ykolokoltsev.codeunitdfa.core.analysis;

import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaField;
import java.util.Set;
import java.util.stream.Collectors;
import org.checkerframework.dataflow.analysis.BackwardAnalysisImpl;
import org.checkerframework.dataflow.cfg.node.FieldAccessNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.ykolokoltsev.codeunitdfa.core.model.SourceDataNode;
import org.ykolokoltsev.codeunitdfa.core.model.SourceDataNodeMapper;

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

  /**
   * Transform analysis result from CheckerFramework model to CodeUnitDFA model.
   */
  public Set<SourceDataNode> findSourceDataNodes(
      final JavaCodeUnit codeUnit
  ) {
    final DataSourceStore entryStore = getEntryStore();

    // analysis if fully initialized
    assert cfg != null;
    assert entryStore != null;
    assert !isRunning;

    final SourceDataNodeMapper sdnMapper = new SourceDataNodeMapper(cfg, codeUnit);
    return entryStore.getMarkedExpressions().entrySet().stream()
        .map(e -> sdnMapper.toSourceDataNode(e.getKey(), e.getValue()))
        .collect(Collectors.toSet());
  }
}
