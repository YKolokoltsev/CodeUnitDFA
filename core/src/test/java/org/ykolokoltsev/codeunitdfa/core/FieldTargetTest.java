package org.ykolokoltsev.codeunitdfa.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Optional;
import org.ykolokoltsev.codeunitdfa.core.analysis.DataOriginAnalysis;
import org.ykolokoltsev.codeunitdfa.core.analysis.DataOriginStore;
import org.ykolokoltsev.codeunitdfa.core.examples.FieldTargetExample;
import org.ykolokoltsev.codeunitdfa.core.launcher.CFGAnalysisLauncher;
import org.checkerframework.dataflow.cfg.ControlFlowGraph;
import org.junit.jupiter.api.Test;

public class FieldTargetTest {

  @Test
  void explicitSetterSameName() {
    CFGAnalysisLauncher launcher = new CFGAnalysisLauncher();
    ControlFlowGraph cfg = launcher.buildCfg(
        FieldTargetExample.class, "explicitSetterSameName");

    DataOriginAnalysis analysis = new DataOriginAnalysis("x");
    analysis.performAnalysis(cfg);

    launcher.saveAnalysisToDot(cfg, analysis, true);
    assertEntryStore(analysis, "x");
  }

  @Test
  void explicitSetterAnyName() {
    CFGAnalysisLauncher launcher = new CFGAnalysisLauncher();
    ControlFlowGraph cfg = launcher.buildCfg(
        FieldTargetExample.class, "explicitSetterAnyName");

    DataOriginAnalysis analysis = new DataOriginAnalysis("x");
    analysis.performAnalysis(cfg);

    launcher.saveAnalysisToDot(cfg, analysis, true);
    assertEntryStore(analysis, "y");
  }

  private void assertEntryStore(DataOriginAnalysis analysis, String ... expectedExpressionNames) {
    DataOriginStore entryStore = Optional.ofNullable(analysis.getEntryStore())
        .orElseThrow(() -> new RuntimeException("no entry store"));
    // check empty store
    assertEquals(expectedExpressionNames.length, entryStore.getMarkedExpressions().size());
    Arrays.stream(expectedExpressionNames).forEach(name -> {
      assertTrue(entryStore.getMarkedExpressions().keySet().stream()
          .anyMatch(key -> key.toString().equals(name)), "expression not found: " + name);
    });
  }
}
