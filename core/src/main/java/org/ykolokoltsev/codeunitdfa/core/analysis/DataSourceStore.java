package org.ykolokoltsev.codeunitdfa.core.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.dataflow.analysis.Store;
import org.checkerframework.dataflow.cfg.node.BinaryOperationNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.cfg.visualize.CFGVisualizer;
import org.checkerframework.dataflow.expression.JavaExpression;
import org.ykolokoltsev.codeunitdfa.core.analysis.SourceTypeValue.SourceTypeEnum;

@Getter
public class DataSourceStore implements Store<DataSourceStore> {

  /**
   * Nodes that participate in formation of the target value.
   */
  private final Map<JavaExpression, SourceTypeValue> informationSources = new HashMap<>();
  /**
   * When {@link #leastUpperBound} joins two sources with distinct {@link #informationSources},
   * it means that conditional expression that produced data source store branching also
   * contributes into target data. By moving backwards, we need to await for the nearest
   * boolean expression and add it to {@link #informationSources}.
   */
  @Setter
  private boolean awaitConditional = false;

  /**
   * Checks if a given node is already present in the {@link #informationSources} collection.
   */
  public boolean isPresent(Node node) {
    return informationSources.containsKey(JavaExpression.fromNode(node));
  }

  /**
   * Returns expression with the given name or empty if not found.
   */
  public Optional<JavaExpression> findByName(
      final Node node
  ) {
    final JavaExpression expression = informationSources.keySet().stream()
        .filter(k -> k.toString().equals(node.toString()))
        .collect(CollectionUtils.toSingletonFallback(null));
    return Optional.ofNullable(expression);
  }

  /**
   * Updates type of existing expression.
   */
  public void updateSourceType(final JavaExpression expression, SourceTypeEnum type) {
    informationSources.get(expression).setType(type);
  }

  public boolean isEmpty() {
    return informationSources.isEmpty();
  }

  public void add(final Node source) {
    // binary operations may be nested
    if (source instanceof BinaryOperationNode) {
      extractOperandTree((BinaryOperationNode) source)
          .forEach(op -> {
            final JavaExpression expression = JavaExpression.fromNode(op);
            informationSources.put(expression, new SourceTypeValue(expression));
          });
    } else {
      final JavaExpression expression = JavaExpression.fromNode(source);
      informationSources.put(expression, new SourceTypeValue(expression));
    }
  }

  public void remove(final Node node) {
    final JavaExpression expression = JavaExpression.fromNode(node);
    informationSources.remove(expression);
  }

  @Override
  public DataSourceStore copy() {
    DataSourceStore copy = new DataSourceStore();
    copy.informationSources.putAll(informationSources);
    copy.awaitConditional = awaitConditional;
    return copy;
  }

  @Override
  public DataSourceStore leastUpperBound(DataSourceStore other) {
    DataSourceStore lubStore = new DataSourceStore();
    lubStore.informationSources.putAll(informationSources);

    other.informationSources.forEach((key, value) -> {
      if (informationSources.containsKey(key)) {
        SourceTypeValue currValue = informationSources.get(key);
        lubStore.informationSources.put(key, currValue.leastUpperBound(value));

      } else {
        lubStore.informationSources.put(key, value);
        lubStore.awaitConditional = true;
      }
    });
    return lubStore;
  }

  @Override
  public DataSourceStore widenedUpperBound(DataSourceStore previous) {
    throw new UnsupportedOperationException("widenedUpperBound is not implemented");
  }

  @Override
  public boolean canAlias(JavaExpression a, JavaExpression b) {
    return true;
  }

  @Override
  public String visualize(CFGVisualizer<?, DataSourceStore, ?> viz) {
    String originExpressions = informationSources.entrySet().stream()
        .map(e -> String.format("\n{%s: %s}",
            e.getKey().toString(), e.getValue().getType().name()))
        .collect( Collectors.joining( "," ));

    if (StringUtils.isBlank(originExpressions)) {
      originExpressions = "none";
    }
    originExpressions += String.format("; awaitConditional = %s;", awaitConditional);
    return viz.visualizeStoreKeyVal("Store: ", originExpressions);
  }

  private Set<Node> extractOperandTree(BinaryOperationNode binaryOperationNode) {
    Set<Node> nodes = new HashSet<>();

    if (binaryOperationNode.getLeftOperand() instanceof BinaryOperationNode) {
      nodes.addAll(extractOperandTree((BinaryOperationNode)
          binaryOperationNode.getLeftOperand()));
    } else {
      nodes.add(binaryOperationNode.getLeftOperand());
    }

    if (binaryOperationNode.getRightOperand() instanceof BinaryOperationNode) {
      nodes.addAll(extractOperandTree((BinaryOperationNode)
          binaryOperationNode.getRightOperand()));
    } else {
      nodes.add(binaryOperationNode.getRightOperand());
    }

    return nodes;
  }

}
