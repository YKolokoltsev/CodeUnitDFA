package org.ykolokoltsev.codeunitdfa.core.model;

import com.tngtech.archunit.core.domain.JavaCodeUnit;
import java.util.Set;
import java.util.stream.Collectors;
import org.checkerframework.dataflow.cfg.ControlFlowGraph;
import org.ykolokoltsev.codeunitdfa.core.analysis.DataSourceStore;
import org.ykolokoltsev.codeunitdfa.core.analysis.SourceTypeValue;
import org.ykolokoltsev.codeunitdfa.core.analysis.SourceTypeValue.SourceTypeEnum;

public class CodeUnitAnalysisInterpreter {

  public Set<SourceDataNode> buildSourceDataNodes(
      final ControlFlowGraph cfg,
      final JavaCodeUnit codeUnit,
      final DataSourceStore store
  ) {
    final SourceDataNodeBuilder dataNodeBuilder = new SourceDataNodeBuilder(cfg, codeUnit);
    return store.getInformationSources().entrySet().stream()
        .filter(e -> !isTransitive(e.getValue()))
        .map(e -> dataNodeBuilder.toSourceDataNode(e.getKey(), e.getValue()))
        .collect(Collectors.toSet());
  }

  // TODO: Verify with tests if a simple type check is sufficient.
  /**
   * Transitive expressions, like local variables, should be skipped.
   */
  private boolean isTransitive(final SourceTypeValue value) {
    return value.getType() == SourceTypeEnum.DECLARED;
  }
}
