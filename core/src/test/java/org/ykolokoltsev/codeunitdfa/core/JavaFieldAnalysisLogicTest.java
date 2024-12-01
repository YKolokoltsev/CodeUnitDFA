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
import org.ykolokoltsev.codeunitdfa.core.examples.JavaFieldLogic;
import org.ykolokoltsev.codeunitdfa.core.helpers.CFGAnalysisLauncher;
import org.ykolokoltsev.codeunitdfa.core.model.CodeUnitAnalysisInterpreter;
import org.ykolokoltsev.codeunitdfa.core.model.SourceDataNode;

public class JavaFieldAnalysisLogicTest {
  private static final String JAVA_FIELD_NAME = "x";

  private static JavaClass exampleClass;
  private static CFGAnalysisLauncher launcher;
  private static CodeUnitAnalysisInterpreter interpreter;

  @BeforeAll
  public static void beforeAll() {
    final JavaClasses classes = new ClassFileImporter()
        .withImportOption(new OnlyIncludeTests())
        .importPackages(JavaFieldLogic.class.getPackageName());
    exampleClass = classes.get(JavaFieldLogic.class);
    launcher = new CFGAnalysisLauncher();
    interpreter = new CodeUnitAnalysisInterpreter();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("javaFieldLogic_data")
  void javaFieldLogic_methods_works(
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

  private static Stream<Arguments> javaFieldLogic_data() {
    return Stream.of(
        arguments(
            "mergeIfThenBranches",
            new Class<?>[] {int.class, int.class, boolean.class},
            List.of("{a, LOCAL}", "{b, LOCAL}", "{c, LOCAL}"),
            List.of("{a, JavaParameter}", "{b, JavaParameter}", "{c, JavaParameter}")),
        arguments(
            "branchesDoNotAffectField",
            new Class<?>[] {boolean.class, int.class},
            List.of("{a, LOCAL}"),
            List.of("{a, JavaParameter}")),
        arguments(
            "branchesEquivalent",
            new Class<?>[] {boolean.class},
            List.of("{1, CONSTANT}"),
            List.of("{1, JavaMethod}")),
        arguments(
            "fromGreaterThen",
            new Class<?>[] {int.class, int.class},
            List.of("{1, CONSTANT}", "{2, CONSTANT}", "{a, LOCAL}", "{b, LOCAL}"),
            List.of("{1, JavaMethod}", "{2, JavaMethod}", "{a, JavaParameter}",
                "{b, JavaParameter}")),
        arguments(
            "fromAssignment",
            new Class<?>[] {boolean.class, boolean.class},
            List.of("{1, CONSTANT}", "{2, CONSTANT}", "{b, LOCAL}"),
            List.of("{1, JavaMethod}", "{2, JavaMethod}", "{b, JavaParameter}")),
        arguments(
            "fromComplexCondition",
            new Class<?>[] {int.class, int.class, boolean.class},
            List.of("{1, CONSTANT}", "{2, CONSTANT}", "{a, LOCAL}", "{b, LOCAL}", "{c, LOCAL}"),
            List.of("{1, JavaMethod}", "{2, JavaMethod}", "{a, JavaParameter}",
                "{b, JavaParameter}", "{c, JavaParameter}")),
        arguments(
            "elseIfBranch",
            new Class<?>[] {boolean.class, boolean.class},
            List.of("{1, CONSTANT}", "{2, CONSTANT}", "{3, CONSTANT}", "{a, LOCAL}", "{b, LOCAL}"),
            List.of("{1, JavaMethod}", "{2, JavaMethod}", "{3, JavaMethod}", "{a, JavaParameter}",
                "{b, JavaParameter}")),
        arguments(
            "orBranch",
            new Class<?>[] {boolean.class, boolean.class},
            List.of("{1, CONSTANT}", "{2, CONSTANT}", "{a, LOCAL}", "{b, LOCAL}"),
            List.of("{1, JavaMethod}", "{2, JavaMethod}", "{a, JavaParameter}",
                "{b, JavaParameter}")),
        arguments(
            "switchBranchWithBreak",
            new Class<?>[] {int.class},
            List.of("{1, CONSTANT}", "{2, CONSTANT}", "{a, LOCAL}"),
            List.of("{1, JavaMethod}", "{2, JavaMethod}", "{a, JavaParameter}")),
        arguments(
            "switchWithoutBreak",
            new Class<?>[] {int.class},
            List.of("{2, CONSTANT}"),
            List.of("{2, JavaMethod}")),
        arguments(
            "switchWithReturn",
            new Class<?>[] {int.class},
            List.of("{1, CONSTANT}", "{2, CONSTANT}", "{a, LOCAL}"),
            List.of("{1, JavaMethod}", "{2, JavaMethod}", "{a, JavaParameter}"))
    );
  }
}
