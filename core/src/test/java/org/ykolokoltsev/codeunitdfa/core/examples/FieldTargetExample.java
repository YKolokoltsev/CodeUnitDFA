package org.ykolokoltsev.codeunitdfa.core.examples;

import lombok.experimental.FieldNameConstants;

@FieldNameConstants
public class FieldTargetExample {

  private int x;

  public void explicitSetterSameName(int x) {
    this.x = x;
  }

  public void explicitSetterAnyName(int y) {
    this.x = y;
  }
}
