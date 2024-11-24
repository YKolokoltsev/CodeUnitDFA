package org.ykolokoltsev.codeunitdfa.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption.OnlyIncludeTests;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.checkerframework.dataflow.cfg.ControlFlowGraph;
import org.junit.jupiter.api.Test;
import org.ykolokoltsev.codeunitdfa.core.analysis.DataSourceStore;
import org.ykolokoltsev.codeunitdfa.core.analysis.JavaFieldAnalysis;
import org.ykolokoltsev.codeunitdfa.core.examples.FieldTargetExample;
import org.ykolokoltsev.codeunitdfa.core.launcher.CFGAnalysisLauncher;
import org.ykolokoltsev.codeunitdfa.core.model.SourceDataNode;

public class JavaFieldAnalysisTest {

  private final JavaClass exampleClass;
  private final CFGAnalysisLauncher launcher;

  public JavaFieldAnalysisTest() {
    final JavaClasses classes = new ClassFileImporter()
        .withImportOption(new OnlyIncludeTests())
        .importPackages(FieldTargetExample.class.getPackageName());
    exampleClass = classes.get(FieldTargetExample.class);
    launcher = new CFGAnalysisLauncher();
  }

  @Test
  void setToParam_works() {
    // setup
    final JavaMethod codeUnit = exampleClass.getMethod("setToParam", int.class);
    final ControlFlowGraph cfg = launcher.buildCfg(codeUnit);

    final JavaField targetField = exampleClass.getField(FieldTargetExample.Fields.x);
    final JavaFieldAnalysis analysis = new JavaFieldAnalysis(targetField);

    // act
    analysis.performAnalysis(cfg);

    // assert
    launcher.saveAnalysisToDot(cfg, analysis, true);
    assertEntryStore(analysis, "a");

    Set<SourceDataNode> sourceDataNodes = analysis.findSourceDataNodes(codeUnit);
    assertISourceDataNodes(sourceDataNodes, "{a, PARAMETER}");
  }

  @Test
  void setToSameNameParam_works() {
    // setup
    final JavaMethod codeUnit = exampleClass.getMethod("setToSameNameParam", int.class);
    final ControlFlowGraph cfg = launcher.buildCfg(codeUnit);

    final JavaField targetField = exampleClass.getField(FieldTargetExample.Fields.x);
    final JavaFieldAnalysis analysis = new JavaFieldAnalysis(targetField);

    // act
    analysis.performAnalysis(cfg);

    // assert
    launcher.saveAnalysisToDot(cfg, analysis, true);
    assertEntryStore(analysis, "x");

    Set<SourceDataNode> sourceDataNodes = analysis.findSourceDataNodes(codeUnit);
    assertISourceDataNodes(sourceDataNodes, "{x, PARAMETER}");
  }

  @Test
  void setToSecondParam_works() {
    // setup
    final JavaCodeUnit codeUnit = exampleClass.getMethod("setToSecondParam", int.class, int.class);
    final ControlFlowGraph cfg = launcher.buildCfg(codeUnit);
    final JavaField targetField = exampleClass.getField(FieldTargetExample.Fields.x);
    final JavaFieldAnalysis analysis = new JavaFieldAnalysis(targetField);

    // act
    analysis.performAnalysis(cfg);

    // assert
    launcher.saveAnalysisToDot(cfg, analysis, true);
    assertEntryStore(analysis, "b");

    Set<SourceDataNode> sourceDataNodes = analysis.findSourceDataNodes(codeUnit);
    assertISourceDataNodes(sourceDataNodes, "{b, PARAMETER}");
  }

  private void assertEntryStore(JavaFieldAnalysis analysis, String ... expectedExpressions) {
    assertThat(analysis.getEntryStore()).isNotNull();
    final DataSourceStore entryStore = analysis.getEntryStore();

    final Set<String> actualExpressions = entryStore.getMarkedExpressions().keySet().stream()
        .map(Object::toString)
        .collect(Collectors.toSet());
    assertThat(actualExpressions).containsExactlyInAnyOrderElementsOf(List.of(expectedExpressions));
  }

  private void assertISourceDataNodes(Set<SourceDataNode> sourceDataNodes, String ... expectedSourceDataNodes) {
    final Set<String> actualSourceDataNodes = sourceDataNodes.stream()
        .map(sdn -> String.format("{%s, %s}", sdn.getUnitName(), sdn.getSourceType()))
        .collect(Collectors.toSet());
    assertThat(actualSourceDataNodes).containsExactlyInAnyOrderElementsOf(List.of(expectedSourceDataNodes));
  }
}
