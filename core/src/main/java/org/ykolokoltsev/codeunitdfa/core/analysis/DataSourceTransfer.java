package org.ykolokoltsev.codeunitdfa.core.analysis;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.checkerframework.dataflow.cfg.node.CaseNode;
import org.checkerframework.dataflow.cfg.node.VariableDeclarationNode;
import org.checkerframework.dataflow.expression.JavaExpression;
import org.ykolokoltsev.codeunitdfa.core.analysis.SourceTypeValue.SourceTypeEnum;
import org.checkerframework.dataflow.analysis.BackwardTransferFunction;
import org.checkerframework.dataflow.analysis.ConditionalTransferResult;
import org.checkerframework.dataflow.analysis.RegularTransferResult;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.cfg.UnderlyingAST;
import org.checkerframework.dataflow.cfg.node.AbstractNodeVisitor;
import org.checkerframework.dataflow.cfg.node.AssignmentNode;
import org.checkerframework.dataflow.cfg.node.LocalVariableNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.cfg.node.ReturnNode;

@RequiredArgsConstructor
class DataSourceTransfer extends AbstractNodeVisitor<
    TransferResult<SourceTypeValue, DataSourceStore>,
    TransferInput<SourceTypeValue, DataSourceStore>>
    implements BackwardTransferFunction<SourceTypeValue, DataSourceStore> {

  /**
   * The analysis used by this transfer function.
   */
  private final JavaFieldAnalysis analysis;

  /**
   * Returns the initial store that should be used at the normal exit block.
   */
  @Override
  public DataSourceStore initialNormalExitStore(
      final UnderlyingAST underlyingAST,
      final List<ReturnNode> returnNodes
  ) {
    return new DataSourceStore();
  }

  /**
   * Returns the initial store that should be used at the exceptional exit block or given the underlying AST of
   * a control flow graph.
   */
  @Override
  public DataSourceStore initialExceptionalExitStore(
      final UnderlyingAST underlyingAST
  ) {
    return new DataSourceStore();
  }

  /**
   * A default node visiting method implementation used by AbstractNodeVisitor.
   */
  @Override
  public TransferResult<SourceTypeValue, DataSourceStore> visitNode(
      final Node n,
      final TransferInput<SourceTypeValue, DataSourceStore> in
  ) {
    if (in.containsTwoStores()) {
      final DataSourceStore thenStore = in.getThenStore();
      final DataSourceStore elseStore = in.getElseStore();
      return new ConditionalTransferResult<>(null, thenStore, elseStore);

    } else {
      final DataSourceStore store = in.getRegularStore();
      checkAwaitConditional(n, store);
      return new RegularTransferResult<>(null, store);
    }
  }

  /**
   * Checks the left hand of the assignment operation if it may affect
   * value of the analysis target, and updates store.
   */
  @Override
  public TransferResult<SourceTypeValue, DataSourceStore> visitAssignment(
      final AssignmentNode n,
      final TransferInput<SourceTypeValue, DataSourceStore> in
  ) {
    final DataSourceStore store = in.getRegularStore();
    final Node target = n.getTarget();

    checkAwaitConditional(n.getExpression(), store);

    if (store.isPresent(target)) {
      store.remove(target);
      store.add(n.getExpression());

    } else if (analysis.isTargetNode(target) && store.isEmpty()) {
      store.add(n.getExpression());
    }

    return new RegularTransferResult<>(null, store);
  }

  /**
   * Updates {@link JavaExpression} type to {@link SourceTypeEnum#LOCAL}.
   */
  @Override
  public TransferResult<SourceTypeValue, DataSourceStore> visitLocalVariable(
      final LocalVariableNode n,
      final TransferInput<SourceTypeValue, DataSourceStore> in) {
    final DataSourceStore store = in.getRegularStore();
    checkAwaitConditional(n, store);
    store.findByName(n).ifPresent(e -> store.updateSourceType(e, SourceTypeEnum.LOCAL));
    return new RegularTransferResult<>(null, store);
  }

  /**
   * Updates {@link JavaExpression} type to {@link SourceTypeEnum#DECLARED}.
   */
  @Override
  public TransferResult<SourceTypeValue, DataSourceStore> visitVariableDeclaration(
      final VariableDeclarationNode n,
      final TransferInput<SourceTypeValue, DataSourceStore> in) {
    final DataSourceStore store = in.getRegularStore();
    store.findByName(n).ifPresent(e -> store.updateSourceType(e, SourceTypeEnum.DECLARED));
    return new RegularTransferResult<>(null, store);
  }

  private void checkAwaitConditional(
      Node n,
      final DataSourceStore store
  ) {
    if (store.isAwaitConditional()) {
      if (n instanceof CaseNode) {
        final Node switchExpr = ((CaseNode) n).getSwitchOperand().getExpression();
        store.add(switchExpr);
      } else {
        store.add(n);
      }
      store.setAwaitConditional(false);
    }
  }
}
