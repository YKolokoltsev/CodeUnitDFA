package org.ykolokoltsev.codeunitdfa.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaParameter;
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
import org.ykolokoltsev.codeunitdfa.core.analysis.JavaParameterAnalysis;
import org.ykolokoltsev.codeunitdfa.core.examples.JavaParameterLocal;
import org.ykolokoltsev.codeunitdfa.core.helpers.CFGAnalysisLauncher;
import org.ykolokoltsev.codeunitdfa.core.model.CodeUnitAnalysisInterpreter;
import org.ykolokoltsev.codeunitdfa.core.model.SourceDataNode;

public class JavaParameterAnalysisLocalTest {

  private static JavaClass exampleClass;
  private static CFGAnalysisLauncher launcher;
  private static CodeUnitAnalysisInterpreter interpreter;

  @BeforeAll
  public static void beforeAll() {
    final JavaClasses classes = new ClassFileImporter()
        .withImportOption(new OnlyIncludeTests())
        .importPackages(JavaParameterLocal.class.getPackageName());
    exampleClass = classes.get(JavaParameterLocal.class);
    launcher = new CFGAnalysisLauncher();
    interpreter = new CodeUnitAnalysisInterpreter();
  }

  @ParameterizedTest(name = "{0} - {2} - {4}")
  @MethodSource("javaParameterLocal_data")
  void javaParameterLocal_methods_works(
      final String methodName,
      final Class<?>[] methodParams,
      final String targetMethodName,
      final Class<?>[] targetMethodParams,
      final int targetParameterIdx,
      final List<String> expectedSourceExpressions,
      final List<String> expectedSourceDataNodes
  ) {
    // setup
    final JavaMethod codeUnit = exampleClass.getMethod(methodName, methodParams);
    final ControlFlowGraph cfg = launcher.buildCfg(codeUnit);

    final JavaParameter targetParameter = exampleClass
        .getMethod(targetMethodName, targetMethodParams)
        .getParameters()
        .get(targetParameterIdx);
    final JavaParameterAnalysis analysis = new JavaParameterAnalysis(targetParameter);

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

  private static Stream<Arguments> javaParameterLocal_data() {
    return Stream.of(
        /*arguments(
            "callSetX",
            new Class<?>[] {int.class},
            "setX",
            new Class<?>[] {int.class},
            0,
            List.of("{a, LOCAL}"),
            List.of("{a, JavaParameter}")),
        arguments(
            "callSetXY",
            new Class<?>[] {int.class, int.class},
            "setXY",
            new Class<?>[] {int.class, int.class},
            0,
            List.of("{a, LOCAL}"),
            List.of("{a, JavaParameter}")),
        arguments(
            "callSetXY",
            new Class<?>[] {int.class, int.class},
            "setXY",
            new Class<?>[] {int.class, int.class},
            1,
            List.of("{b, LOCAL}"),
            List.of("{b, JavaParameter}")),
        arguments(
            "callSetXYComplexArgument",
            new Class<?>[] {int.class, int.class, int.class},
            "setXY",
            new Class<?>[] {int.class, int.class},
            0,
            List.of("{a, LOCAL}", "{b, LOCAL}"),
            List.of("{a, JavaParameter}", "{b, JavaParameter}")),
        arguments(
            "callSetXYComplexArgument",
            new Class<?>[] {int.class, int.class, int.class},
            "setXY",
            new Class<?>[] {int.class, int.class},
            1,
            List.of("{b, LOCAL}", "{c, LOCAL}"),
            List.of("{b, JavaParameter}", "{c, JavaParameter}")),
        arguments(
            "callTwoMethods",
            new Class<?>[] {int.class, int.class},
            "setX",
            new Class<?>[] {int.class},
            0,
            List.of("{a, LOCAL}"),
            List.of("{a, JavaParameter}")),
        arguments(
            "callTwoMethods",
            new Class<?>[] {int.class, int.class},
            "setXY",
            new Class<?>[] {int.class, int.class},
            0,
            List.of("{a, LOCAL}"),
            List.of("{a, JavaParameter}")),*/
        arguments(
            "callTwoMethods",
            new Class<?>[] {int.class, int.class},
            "setXY",
            new Class<?>[] {int.class, int.class},
            1,
            List.of("{1, CONSTANT}", "{b, LOCAL}"),
            List.of("{1, JavaMethod}", "{b, JavaParameter}"))
    );
  }

}
