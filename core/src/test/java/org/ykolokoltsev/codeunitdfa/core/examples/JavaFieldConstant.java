package org.ykolokoltsev.codeunitdfa.core.examples;

public class JavaFieldConstant {
  public int x;

  void fromConstantLiteral() {
    x = 17;
  }

  void fromConstantVariable() {
    int a = 33;
    this.x = a;
  }

  void fromConstantExpression() {
    this.x = 33 + 17;
  }
}
