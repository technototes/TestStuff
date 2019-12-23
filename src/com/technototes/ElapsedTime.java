package com.technototes;

public class ElapsedTime {
  long start;

  public ElapsedTime() {
    start = System.currentTimeMillis();
  }

  public ElapsedTime(long scale) {
    start = System.currentTimeMillis();
  }

  public void reset() {
    start = System.currentTimeMillis();
  }

  public long milliseconds() {
    return (System.currentTimeMillis() - start);
  }
}
