package org.ykolokoltsev.codeunitdfa.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption.OnlyIncludeTests;
import java.util.Set;
import java.util.stream.Collectors;
import org.checkerframework.dataflow.cfg.ControlFlowGraph;
import org.junit.jupiter.api.Test;
import org.ykolokoltsev.codeunitdfa.core.analysis.DataSourceStore;
import org.ykolokoltsev.codeunitdfa.core.analysis.JavaFieldAnalysis;
import org.ykolokoltsev.codeunitdfa.core.examples.FieldTargetExample;
import org.ykolokoltsev.codeunitdfa.core.launcher.CFGAnalysisLauncher;

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
  void explicitSetterSameName() {
    // setup
    final JavaMethod method = exampleClass.getMethod("explicitSetterSameName", int.class);
    final ControlFlowGraph cfg = launcher.buildCfg(method);

    final JavaField targetField = exampleClass.getField(FieldTargetExample.Fields.x);
    final JavaFieldAnalysis analysis = new JavaFieldAnalysis(targetField);

    // act
    analysis.performAnalysis(cfg);

    // assert
    launcher.saveAnalysisToDot(cfg, analysis, true);
    assertEntryStore(analysis, "x");
  }

  @Test
  void explicitSetterAnyName() {
    // setup
    final JavaMethod method = exampleClass.getMethod("explicitSetterAnyName", int.class);
    ControlFlowGraph cfg = launcher.buildCfg(method);
    final JavaField targetField = exampleClass.getField(FieldTargetExample.Fields.x);
    JavaFieldAnalysis analysis = new JavaFieldAnalysis(targetField);

    // act
    analysis.performAnalysis(cfg);

    // assert
    launcher.saveAnalysisToDot(cfg, analysis, true);
    assertEntryStore(analysis, "y");
  }

  private void assertEntryStore(JavaFieldAnalysis analysis, String ... expectedExpressions) {
    assertThat(analysis.getEntryStore()).isNotNull();
    final DataSourceStore entryStore = analysis.getEntryStore();

    final Set<String> actualExpressions = entryStore.getMarkedExpressions().keySet().stream()
        .map(Object::toString)
        .collect(Collectors.toSet());
    assertThat(expectedExpressions).containsExactlyInAnyOrderElementsOf(actualExpressions);
  }
}
