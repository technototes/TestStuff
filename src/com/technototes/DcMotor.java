package com.technototes;

public class DcMotor implements java.lang.Runnable {

  // Neverest 60's have a 'no load' RPM of 105
  // This is way to fast :/
  private static final double RPM = 105;
  // 1680 pulses per full revolution
  private static final double PULSES_PER_REVOLUTION = 1680;
  // Math :D
  private static final double PULSES_PER_MIN = PULSES_PER_REVOLUTION * RPM;
  private static final double PULSE_PER_MS = PULSES_PER_MIN / 60000.;

  // The speed at which the power reduces per ms when in 'float' mode
  private static final double FLOAT_POWER_RATIO = .03;
  // The speed at which the power reduces per ms when in 'brake' mode
  private static final double BRAKE_POWER_RATIO = .07;
  // The speed at which the power increases per ms when it's initially applied
  private static final double START_POWER_RATIO = .02;

  private volatile double power = 0;
  private volatile Direction dir = Direction.FORWARD;
  private volatile double position = 0;
  private volatile ZeroPowerBehavior zpb = ZeroPowerBehavior.FLOAT;
  private int id;
  private double lastPower;

  private double GetDelta(long ms, double ratio) {
    double delta = 0;
    while (ms-- > 0) {
      double newPower = power * ratio + lastPower * (1 - ratio);
      if (Math.abs(lastPower - power) < Math.abs(newPower - power)) {
        lastPower = power;
      } else {
        lastPower = newPower;
      }
      delta = lastPower * PULSE_PER_MS;
    }
    return delta;
  }

  // Stuff for the motor position simulator
  // It's pretty simplistic, and pretty much deterministic, which doesn't quite reflect reality :/
  @Override
  public void run() {
    lastPower = 0;
    ElapsedTime tm = new ElapsedTime();
    long lastTime = tm.milliseconds();
    do {
      // First, wait for at least millisecond
      // I could add a little skew in here if I wanted...
      long curTime = tm.milliseconds();
      long ms = curTime - lastTime;
      if (ms <= 0) {
        Thread.yield();
        continue;
      }
      lastTime = curTime;

      // Now move the position by the amount of power specified
      // Is power set to effectively zero?
      double power = this.power;
      double newPower;
      double ratio;
      if (power == 0) {
        ratio = (this.zpb == ZeroPowerBehavior.FLOAT) ? FLOAT_POWER_RATIO : BRAKE_POWER_RATIO;
      } else {
        ratio = START_POWER_RATIO;
      }
      double delta = GetDelta(ms, ratio);
      // Could (should?) add a 'load factor' here
      this.position += delta;
    } while (true);
  }

  public enum RunMode {
    RUN_USING_ENCODER
  }

  public enum Direction {
    FORWARD,
    REVERSE
  }

  public enum ZeroPowerBehavior {
    BRAKE, FLOAT
  }

  Thread runner;

  public DcMotor(int id) {
    System.out.printf("Pulses/min: %3.3f Pulses/ms: %3.3f\n", PULSES_PER_MIN, PULSE_PER_MS);
    this.id = id;
    runner = new Thread(this);
    runner.start();
  }

  public void setMode(RunMode mode) {
    // Don't need to do anything here currently
  }

  public void setDirection(Direction dir) {
    if (this.dir != dir) {
      this.power = -this.power;
    }
    this.dir = dir;
  }

  public Direction getDirection() {
    return this.dir;
  }

  public void setZeroPowerBehavior(ZeroPowerBehavior b) {
    this.zpb = b;
  }

  public int getCurrentPosition() {
    return (int) ((this.dir == Direction.FORWARD) ? -this.position : this.position);
  }

  public void setPower(double d) {
    this.power = Math.min(1.0, Math.max(-1.0, (this.dir == Direction.FORWARD) ? d : -d));
  }

  @Override
  public String toString() {
    return String.format("%d-%s:e[%d] %f(%f)", id, this.dir.toString(), this.getCurrentPosition(), this.power, this.lastPower);
  }
}
