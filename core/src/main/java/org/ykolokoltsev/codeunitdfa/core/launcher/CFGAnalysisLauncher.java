package org.ykolokoltsev.codeunitdfa.core.launcher;

import com.tngtech.archunit.core.domain.JavaMethod;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.dataflow.analysis.AbstractValue;
import org.checkerframework.dataflow.analysis.Analysis;
import org.checkerframework.dataflow.analysis.Store;
import org.checkerframework.dataflow.analysis.TransferFunction;
import org.checkerframework.dataflow.cfg.CFGProcessor.CFGProcessResult;
import org.checkerframework.dataflow.cfg.CFGProcessor;
import org.checkerframework.dataflow.cfg.ControlFlowGraph;
import org.checkerframework.dataflow.cfg.visualize.CFGVisualizer;
import org.checkerframework.dataflow.cfg.visualize.DOTCFGVisualizer;

@Slf4j
public class CFGAnalysisLauncher {

  // used for source code search
  private final URI projectDir;
  
  // DOT files output folder 
  private final URI targetDir;

  private final JavaCompiler compiler;
  private final DiagnosticCollector<JavaFileObject> diagnostics;
  
  /**
   * Initialize project folders assuming standard maven configuration.
   */
  public CFGAnalysisLauncher() {
    try {
      URI[] projectDirs = Optional
          .ofNullable(CFGAnalysisLauncher.class.getResource("/"))
          .map(url -> {
            try {
              return url.toURI();
            } catch (URISyntaxException e) {
              throw new RuntimeException(e);
            }
          })
          .map(uri -> new URI[]{
              // service
              uri.resolve("../../"),
              // target
              uri.resolve("../"),
          })
          .filter(uris -> {
            for (URI uri : uris) {
              File javaDir = new File(uri);
              if (!javaDir.exists() || !javaDir.isDirectory()) {
                throw new RuntimeException("not a valid folder: " + uri);
              }
            }
            return true;
          })
          .orElseThrow(() -> new RuntimeException("unavailable resource root"));

      projectDir = projectDirs[0];
      log.info("projectDir: " + projectDir);

      targetDir = projectDirs[1];
      log.info("targetDir: " + targetDir);

      compiler = ToolProvider.getSystemJavaCompiler();
      diagnostics = new DiagnosticCollector<>();

    } catch (Exception ex) {
      throw new RuntimeException("failed to find project sources", ex);
    }
  }

  public
  <V extends AbstractValue<V>, S extends Store<S>, T extends TransferFunction<V, S>>
  void saveAnalysisToDot(ControlFlowGraph cfg, Analysis<V, S, T> analysis, boolean verbose) {
    final Map<String, Object> args = new LinkedHashMap<>(2);
    args.put("outdir", targetDir.getPath());
    args.put("verbose", verbose);

    CFGVisualizer<V, S, T> viz = new DOTCFGVisualizer<>();
    viz.init(args);
    viz.visualizeWithAction(cfg, cfg.getEntryBlock(), analysis);
    viz.shutdown();
  }

  @SneakyThrows
  public ControlFlowGraph buildCfg(JavaMethod javaMethod) {
    final Class<?> targetClass = Class.forName(javaMethod.getOwner().getName());
    final String methodName = javaMethod.getName();
    
    // find and setup sources to be compiled 
    final File srcFile = findSourceFile(targetClass);
    final StandardJavaFileManager fileManager = compiler.getStandardFileManager(
        diagnostics, null, StandardCharsets.UTF_8);
    final Iterable< ? extends JavaFileObject > sources =
        fileManager.getJavaFileObjectsFromFiles(List.of(srcFile));

    // setup AbstractProcessor for building CFG, required for dataflow analysis
    CFGProcessor cfgProcessor = new CFGProcessor(targetClass.getSimpleName(), methodName);
    
    // run annotation processing phase and print diagnostics
    // checker option example: "-Aflowdotdir=" + targetDir.getPath()
    Iterable<String> options = List.of("-proc:only");
    final CompilationTask task = compiler.getTask( null, fileManager, diagnostics,
        options, null, sources );
    task.setProcessors(List.of(cfgProcessor));
    task.call();

    printDiagnostics();
    fileManager.close();

    CFGProcessResult res = cfgProcessor.getCFGProcessResult();
    if (res == null) {
      throw new RuntimeException("internal error in type processor!"
          + " method typeProcessOver() doesn't get called.");
    }

    if (!res.isSuccess()) {
      throw new RuntimeException(res.getErrMsg());
    }
    return res.getCFG();
  }
  
  private void printDiagnostics() {
    for( final Diagnostic< ? extends JavaFileObject > diagnostic:
        diagnostics.getDiagnostics() ) {

      System.out.format("Custom compilation: %s, line %d in %s",
          diagnostic.getMessage( null ),
          diagnostic.getLineNumber(),
          Optional.ofNullable(diagnostic.getSource())
              .map(JavaFileObject::getName)
              .orElse("none"));
    }
  }

  @SneakyThrows
  private File findSourceFile(Class<?> targetClass) {
    try (Stream<Path> walkStream = Files.walk(Paths.get(projectDir))) {
      List<File> files = walkStream.filter(p -> p.toFile().isFile())
          .filter(f -> f.toString().endsWith(targetClass.getSimpleName() + ".java"))
          .map(Path::toFile)
          .collect(Collectors.toList());
      if (files.size() != 1) {
        throw new RuntimeException("undefined source file for class: " + targetClass);
      }
      return files.get(0);
    }
  }
}
