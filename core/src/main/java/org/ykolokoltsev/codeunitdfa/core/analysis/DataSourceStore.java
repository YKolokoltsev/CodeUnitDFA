package org.ykolokoltsev.codeunitdfa.core.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import org.ykolokoltsev.codeunitdfa.core.analysis.SourceTypeValue.SourceTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.dataflow.analysis.Store;
import org.checkerframework.dataflow.cfg.node.BinaryOperationNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.cfg.visualize.CFGVisualizer;
import org.checkerframework.dataflow.expression.JavaExpression;

@Getter
public class DataSourceStore implements Store<DataSourceStore> {

  /**
   * Map of all JavaExpressions found in the code unit so far, marked
   * accordingly to the fact if they participate in formation of the
   * target value.
   */
  private final Map<JavaExpression, SourceTypeValue> markedExpressions = new HashMap<>();

  // TODO: Maybe better to work with JavaExpression here
  public boolean isPresentInDependencyChain(Node expression) {
    return markedExpressions.containsKey(JavaExpression.fromNode(expression));
  }

  /**
   * When we get to expression definition during backward analysis,
   * it becomes available the data about its abstract type. Here
   * it is possible to update this type.
   *
   * @param jx - java expression object
   * @param type - new type
   */
  public void updateExpressionType(JavaExpression jx, SourceTypeEnum type) {
    Optional.ofNullable(markedExpressions.get(jx))
        .ifPresent(v -> v.setType(type));
  }

  public void addDependency(Node target, Node source) {

    // binary operations may be nested
    if (source instanceof BinaryOperationNode) {
      extractOperandTree((BinaryOperationNode) source)
          .forEach(op -> markedExpressions.put(JavaExpression.fromNode(op),
              new SourceTypeValue(SourceTypeEnum.UNKNOWN)));
    } else {
      markedExpressions.put(JavaExpression.fromNode(source),
          new SourceTypeValue(SourceTypeEnum.UNKNOWN));
    }
  }

  @Override
  public DataSourceStore copy() {
    DataSourceStore copy = new DataSourceStore();
    copy.markedExpressions.putAll(markedExpressions);
    return copy;
  }

  @Override
  public DataSourceStore leastUpperBound(DataSourceStore other) {
    DataSourceStore lubStore = new DataSourceStore();
    lubStore.markedExpressions.putAll(markedExpressions);

    other.markedExpressions.forEach((key, value) -> {
      if (markedExpressions.containsKey(key)) {
        SourceTypeValue currValue = markedExpressions.get(key);
        lubStore.markedExpressions.put(key, currValue.leastUpperBound(value));

      } else {
        lubStore.markedExpressions.put(key, value);
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
    String originExpressions = markedExpressions.entrySet().stream()
        .map(e -> String.format("\n{%s: %s}",
            e.getKey().toString(), e.getValue().getType().name()))
        .collect( Collectors.joining( "," ));

    if (StringUtils.isBlank(originExpressions)) {
      originExpressions = "none";
    }
    return viz.visualizeStoreKeyVal("Discovered sources: ", originExpressions);
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
