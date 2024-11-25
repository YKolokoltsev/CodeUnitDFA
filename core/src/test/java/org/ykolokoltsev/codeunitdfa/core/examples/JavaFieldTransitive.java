package org.ykolokoltsev.codeunitdfa.core.examples;

public class JavaFieldTransitive {
  private int x;
  private int y;

  public void setToParamViaOneLocal(int a) {
    int b = a;
    this.x = b;
  }

  public void setToParamViaField(int a) {
    y = a;
    this.x = y;
  }

  public void setToParamViaTwoLocals(int a) {
    int b = a;
    int c = b;
    this.x = c;
  }

  public void setToSecondParamViaFirst(int a, int b) {
    a = b;
    this.x = a;
  }

  public void setToBothParamsViaOverride(int a, int b) {
    a = b + a;
    this.x = a;
  }

  public void setToParamAndConstant(int a) {
    a = a + 10;
    this.x = a;
  }

  public void resetPreviousValue(int a, int b) {
    x = a;
    x = b;
  }

  public void useFieldAsAsLocalVariable(int a, int b) {
    x = a;
    x = b + x;
  }
}
