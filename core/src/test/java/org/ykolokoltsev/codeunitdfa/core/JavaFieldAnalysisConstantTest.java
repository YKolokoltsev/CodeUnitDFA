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
import org.ykolokoltsev.codeunitdfa.core.examples.JavaFieldConstant;
import org.ykolokoltsev.codeunitdfa.core.examples.JavaFieldParameterSource;
import org.ykolokoltsev.codeunitdfa.core.helpers.CFGAnalysisLauncher;
import org.ykolokoltsev.codeunitdfa.core.model.CodeUnitAnalysisInterpreter;
import org.ykolokoltsev.codeunitdfa.core.model.SourceDataNode;

public class JavaFieldAnalysisConstantTest {

  private static JavaClass exampleClass;
  private static CFGAnalysisLauncher launcher;
  private static CodeUnitAnalysisInterpreter interpreter;

  @BeforeAll
  public static void beforeAll() {
    final JavaClasses classes = new ClassFileImporter()
        .withImportOption(new OnlyIncludeTests())
        .importPackages(JavaFieldConstant.class.getPackageName());
    exampleClass = classes.get(JavaFieldConstant.class);
    launcher = new CFGAnalysisLauncher();
    interpreter = new CodeUnitAnalysisInterpreter();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("javaFieldConstant_data")
  void javaFieldConstant_methods_works(
      final String methodName,
      final Class<?>[] methodParams,
      final List<String> expectedSourceExpressions,
      final List<String> expectedSourceDataNodes
  ) {
    // setup
    final JavaMethod codeUnit = exampleClass.getMethod(methodName, methodParams);
    final ControlFlowGraph cfg = launcher.buildCfg(codeUnit);

    final JavaField targetField = exampleClass.getField(JavaFieldParameterSource.Fields.x);
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

  private static Stream<Arguments> javaFieldConstant_data() {
    return Stream.of(
        arguments(
            "fromConstantLiteral",
            new Class<?>[] {},
            List.of("{17, CONSTANT}"),
            List.of("{17, JavaMethod}")),
        arguments(
            "fromConstantVariable",
            new Class<?>[] {},
            List.of("{33, CONSTANT}"),
            List.of("{33, JavaMethod}")),
        arguments(
            "fromConstantExpression",
            new Class<?>[] {},
            List.of("{17, CONSTANT}", "{33, CONSTANT}"),
            List.of("{17, JavaMethod}", "{33, JavaMethod}"))
    );
  }


}
