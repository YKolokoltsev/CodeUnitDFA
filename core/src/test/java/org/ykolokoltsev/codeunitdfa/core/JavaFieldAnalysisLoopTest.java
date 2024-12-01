package org.ykolokoltsev.codeunitdfa.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption.OnlyIncludeTests;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.checkerframework.dataflow.cfg.ControlFlowGraph;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.ykolokoltsev.codeunitdfa.core.analysis.DataSourceStore;
import org.ykolokoltsev.codeunitdfa.core.analysis.JavaFieldAnalysis;
import org.ykolokoltsev.codeunitdfa.core.examples.JavaFieldLoop;
import org.ykolokoltsev.codeunitdfa.core.helpers.CFGAnalysisLauncher;
import org.ykolokoltsev.codeunitdfa.core.model.CodeUnitAnalysisInterpreter;
import org.ykolokoltsev.codeunitdfa.core.model.SourceDataNode;

public class JavaFieldAnalysisLoopTest {
  private static final String JAVA_FIELD_NAME = "x";

  private static JavaClass exampleClass;
  private static CFGAnalysisLauncher launcher;
  private static CodeUnitAnalysisInterpreter interpreter;

  @BeforeAll
  public static void beforeAll() {
    final JavaClasses classes = new ClassFileImporter()
        .withImportOption(new OnlyIncludeTests())
        .importPackages(JavaFieldLoop.class.getPackageName());
    exampleClass = classes.get(JavaFieldLoop.class);
    launcher = new CFGAnalysisLauncher();
    interpreter = new CodeUnitAnalysisInterpreter();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("javaFieldLoop_data")
  void javaFieldLoop_methods_works(
      final String methodName,
      final Class<?>[] methodParams,
      final List<String> expectedSourceExpressions,
      final List<String> expectedSourceDataNodes
  ) {
    // setup
    final JavaMethod codeUnit = exampleClass.getMethod(methodName, methodParams);
    final ControlFlowGraph cfg = launcher.buildCfg(codeUnit);

    final JavaField targetField = exampleClass.getField(JAVA_FIELD_NAME);
    final JavaFieldAnalysis analysis = new JavaFieldAnalysis(targetField);

    // act
    analysis.performAnalysis(cfg);

    // assert
    launcher.saveAnalysisToDot(cfg, analysis, true);

    final DataSourceStore entryStore = analysis.getEntryStore();
    assertThat(entryStore).isNotNull();
    ModelAsserts.assertEntryStore(entryStore, expectedSourceExpressions);

    Set<SourceDataNode> sourceDataNodes = interpreter.buildSourceDataNodes(cfg, codeUnit, entryStore);
    ModelAsserts.assertSourceDataNodes(sourceDataNodes, expectedSourceDataNodes);
  }

  private static Stream<Arguments> javaFieldLoop_data() {
    return Stream.of(
        arguments(
            "forLoop",
            new Class<?>[] {},
            List.of("{1, CONSTANT}", "{0, CONSTANT}", "{3, CONSTANT}"),
            List.of("{3, JavaMethod}", "{1, JavaMethod}", "{0, JavaMethod}")),
        arguments(
            "forLoopIterative",
            new Class<?>[] {int.class},
            List.of("{1, CONSTANT}", "{this.x, FIELD}", "{a, LOCAL}", "{0, CONSTANT}",
                "{3, CONSTANT}"),
            List.of("{this.x, JavaField}", "{3, JavaMethod}", "{1, JavaMethod}", "{0, JavaMethod}",
                "{a, JavaParameter}")),
        arguments(
            "whileLoop",
            new Class<?>[] {},
            List.of("{1, CONSTANT}", "{0, CONSTANT}", "{3, CONSTANT}"),
            List.of("{3, JavaMethod}", "{1, JavaMethod}", "{0, JavaMethod}")),
        arguments(
            "doWhileLoop",
            new Class<?>[] {},
            List.of("{1, CONSTANT}", "{0, CONSTANT}", "{3, CONSTANT}"),
            List.of("{3, JavaMethod}", "{1, JavaMethod}", "{0, JavaMethod}"))
    );
  }
}
