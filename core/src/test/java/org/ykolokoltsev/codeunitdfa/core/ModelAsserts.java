package org.ykolokoltsev.codeunitdfa.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.ykolokoltsev.codeunitdfa.core.analysis.DataSourceStore;
import org.ykolokoltsev.codeunitdfa.core.model.SourceDataNode;

@UtilityClass
public class ModelAsserts {
  public void assertEntryStore(
      final DataSourceStore entryStore,
      final List<String> expectedSourceExpressions
  ) {
    final Set<String> actualExpressions = entryStore.getInformationSources().entrySet().stream()
        .map(e -> String.format("{%s, %s}", e.getKey().toString(), e.getValue().getType()))
        .collect(Collectors.toSet());
    assertThat(actualExpressions).containsExactlyInAnyOrderElementsOf(expectedSourceExpressions);
  }

  public void assertSourceDataNodes(
      final Set<SourceDataNode> sourceDataNodes,
      final List<String> expectedSourceDataNodes
  ) {
    final Set<String> actualSourceDataNodes = sourceDataNodes.stream()
        .map(sdn -> String.format("{%s, %s}", sdn.getUnitName(), sdn.getOwnerTypeName()))
        .collect(Collectors.toSet());
    assertThat(actualSourceDataNodes).containsExactlyInAnyOrderElementsOf(expectedSourceDataNodes);
  }
}
