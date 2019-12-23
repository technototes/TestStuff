package com.technototes;

public class LinearOpMode {
  public boolean opModeIsActive() {
    return true;
  }
  public void sleep(long ms) {
    try {
      ElapsedTime tm = new ElapsedTime(1);
      while (tm.milliseconds() < ms && this.opModeIsActive()) {
        Thread.sleep(Math.min(ms - tm.milliseconds(), 10));
      }
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }
  }
}

