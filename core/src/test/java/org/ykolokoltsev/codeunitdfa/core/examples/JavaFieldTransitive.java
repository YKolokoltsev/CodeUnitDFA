package org.ykolokoltsev.codeunitdfa.core.examples;

public class JavaFieldTransitive {
  private int x;

  public void setToParamViaOneLocal(int a) {
    int b = a;
    this.x = b;
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
}
