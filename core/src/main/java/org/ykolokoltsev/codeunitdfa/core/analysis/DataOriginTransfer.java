package org.ykolokoltsev.codeunitdfa.core.analysis;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ykolokoltsev.codeunitdfa.core.analysis.OriginTypeValue.OriginTypeEnum;
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
import org.checkerframework.dataflow.expression.JavaExpression;

@RequiredArgsConstructor
class DataOriginTransfer extends AbstractNodeVisitor<
    TransferResult<OriginTypeValue, DataOriginStore>,
    TransferInput<OriginTypeValue, DataOriginStore>>
    implements BackwardTransferFunction<OriginTypeValue, DataOriginStore> {

  /**
   * The analysis used by this transfer function.
   */
  private final DataOriginAnalysis analysis;

  @Override
  public DataOriginStore initialNormalExitStore(
      UnderlyingAST underlyingAST, List<ReturnNode> returnNodes) {
    return new DataOriginStore();
  }

  @Override
  public DataOriginStore initialExceptionalExitStore(UnderlyingAST underlyingAST) {
    return new DataOriginStore();
  }

  /**
   * A default node visiting method implementation used by AbstractNodeVisitor.
   *
   * <p>This default implementation returns the input information unchanged, or in the case of
   * conditional input information, merged.
   *
   * @param in the transfer input
   * @return the input information, as a TransferResult
   */
  @Override
  public TransferResult<OriginTypeValue, DataOriginStore> visitNode(
      Node n, TransferInput<OriginTypeValue, DataOriginStore> in) {

    if (in.containsTwoStores()) {
      DataOriginStore thenStore = in.getThenStore();
      DataOriginStore elseStore = in.getElseStore();
      return new ConditionalTransferResult<>(null, thenStore, elseStore);

    } else {
      DataOriginStore store = in.getRegularStore();
      return new RegularTransferResult<>(null, store);
    }
  }

  /**
   * Checks the left hand of the assignment operation if it may affect
   * value of the analysis target, and updates store.
   *
   * @param n object of AssignmentNode
   * @param in transfer input
   * @return regular transfer result
   */
  @Override
  public TransferResult<OriginTypeValue, DataOriginStore> visitAssignment(
      AssignmentNode n,
      TransferInput<OriginTypeValue, DataOriginStore> in) {

    DataOriginStore store = in.getRegularStore();
    Node target = n.getTarget();

    if (store.isPresentInDependencyChain(target)
        || analysis.isTargetExpression(target)) {
      store.addDependency(n.getTarget(), n.getExpression());
    }

    return new RegularTransferResult<>(null, store);
  }

  /**
   * Update OriginTypeValue for local variables to INPUT or LOCAL.
   *
   * @param n object of LocalVariableNode
   * @param in transfer input
   * @return regular transfer result
   */
  @Override
  public TransferResult<OriginTypeValue, DataOriginStore> visitLocalVariable(
      LocalVariableNode n,
      TransferInput<OriginTypeValue, DataOriginStore> in) {
    DataOriginStore store = in.getRegularStore();

    store.updateExpressionType(JavaExpression.fromNode(n), n.getInSource() ?
        OriginTypeEnum.INPUT : OriginTypeEnum.LOCAL);

    return new RegularTransferResult<>(null, store);
  }
}
