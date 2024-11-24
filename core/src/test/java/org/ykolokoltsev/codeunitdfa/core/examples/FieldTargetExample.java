package org.ykolokoltsev.codeunitdfa.core.examples;

import lombok.experimental.FieldNameConstants;

@FieldNameConstants
public class FieldTargetExample {
  private int x;

  public void setToParam(int a) {
    this.x = a;
  }

  public void setToSameNameParam(int x) {
    this.x = x;
  }

  public void setToSecondParam(int a, int b) {
    this.x = b;
  }
}
