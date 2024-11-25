package org.ykolokoltsev.codeunitdfa.core.model;

import com.tngtech.archunit.core.domain.JavaCodeUnit;
import java.util.Set;
import java.util.stream.Collectors;
import org.checkerframework.dataflow.cfg.ControlFlowGraph;
import org.ykolokoltsev.codeunitdfa.core.analysis.DataSourceStore;

public class CodeUnitAnalysisInterpreter {

  public Set<SourceDataNode> buildSourceDataNodes(
      final ControlFlowGraph cfg,
      final JavaCodeUnit codeUnit,
      final DataSourceStore store
  ) {
    final SourceDataNodeBuilder dataNodeBuilder = new SourceDataNodeBuilder(cfg, codeUnit);
    return store.getInformationSources().entrySet().stream()
        .map(e -> dataNodeBuilder.toSourceDataNode(e.getKey(), e.getValue()))
        .collect(Collectors.toSet());
  }

}
