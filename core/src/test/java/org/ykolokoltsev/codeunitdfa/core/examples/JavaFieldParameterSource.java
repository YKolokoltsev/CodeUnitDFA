package org.ykolokoltsev.codeunitdfa.core.examples;

import lombok.experimental.FieldNameConstants;

@FieldNameConstants
public class JavaFieldParameterSource {
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

  public void setToBothParams(int a, int b) {
    this.x = a + b;
  }
}
