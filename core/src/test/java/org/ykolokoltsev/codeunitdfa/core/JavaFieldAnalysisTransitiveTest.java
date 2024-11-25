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
import org.ykolokoltsev.codeunitdfa.core.examples.JavaFieldParameterSource;
import org.ykolokoltsev.codeunitdfa.core.examples.JavaFieldTransitive;
import org.ykolokoltsev.codeunitdfa.core.helpers.CFGAnalysisLauncher;
import org.ykolokoltsev.codeunitdfa.core.model.CodeUnitAnalysisInterpreter;
import org.ykolokoltsev.codeunitdfa.core.model.SourceDataNode;

public class JavaFieldAnalysisTransitiveTest {

  private static JavaClass exampleClass;
  private static CFGAnalysisLauncher launcher;
  private static CodeUnitAnalysisInterpreter interpreter;

  @BeforeAll
  public static void beforeAll() {
    final JavaClasses classes = new ClassFileImporter()
        .withImportOption(new OnlyIncludeTests())
        .importPackages(JavaFieldParameterSource.class.getPackageName());
    exampleClass = classes.get(JavaFieldTransitive.class);
    launcher = new CFGAnalysisLauncher();
    interpreter = new CodeUnitAnalysisInterpreter();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("javaFieldTransitive_data")
  void javaFieldTransitive_methods_works(
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

  private static Stream<Arguments> javaFieldTransitive_data() {
    return Stream.of(
        arguments(
            "setToParamViaOneLocal",
            new Class<?>[] {int.class},
            List.of("{a, LOCAL}"),
            List.of("{a, JavaParameter}")),
        arguments(
            "setToParamViaField",
            new Class<?>[] {int.class},
            List.of("{a, LOCAL}"),
            List.of("{a, JavaParameter}")),
        arguments(
            "setToParamViaTwoLocals",
            new Class<?>[] {int.class},
            List.of("{a, LOCAL}"),
            List.of("{a, JavaParameter}")),
        arguments(
            "setToSecondParamViaFirst",
            new Class<?>[] {int.class, int.class},
            List.of("{b, LOCAL}"),
            List.of("{b, JavaParameter}")),
        arguments(
            "setToBothParamsViaOverride",
            new Class<?>[] {int.class, int.class},
            List.of("{a, LOCAL}", "{b, LOCAL}"),
            List.of("{a, JavaParameter}", "{b, JavaParameter}")),
        arguments(
            "setToParamAndConstant",
            new Class<?>[] {int.class},
            List.of("{a, LOCAL}", "{10, CONSTANT}"),
            List.of("{a, JavaParameter}", "{10, JavaMethod}")),
        arguments(
            "resetPreviousValue",
            new Class<?>[] {int.class, int.class},
            List.of("{b, LOCAL}"),
            List.of("{b, JavaParameter}")),
        arguments(
            "useFieldAsAsLocalVariable",
            new Class<?>[] {int.class, int.class},
            List.of("{a, LOCAL}", "{b, LOCAL}"),
            List.of("{a, JavaParameter}", "{b, JavaParameter}"))
    );
  }


}
