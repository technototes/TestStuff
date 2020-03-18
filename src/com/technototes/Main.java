package com.technototes;

public class Main {
  private static DcMotor left, right;
  private static LiftControl lift;
  private static LinearOpMode opMode;
  private static ElapsedTime tm;
  private static void sleep(long ms) {
    opMode.sleep(ms);
  }

  private static void dump(String heading) {
    System.out.printf("%s: Left: %s, Right %s [%d]\n", heading, left, right, tm.milliseconds());
  }
  public static double goToAngle(double target, double current) {
    double delta = (target % 360) - (current % 360.0);
    if (delta > 180) {
      return delta - 360;
    } else if (delta < -180) {
      return 360 + delta;
    } else {
      return delta;
    }
  }

  public static void main(String[] args) {
    for (double i = -380.0; i < 380.0; i += 4.75) {
      for (double j = 380.0; j > -380.0; j -= 7.5) {
        System.out.printf("%f to %f = %f\n", i, j, goToAngle(j, i));
      }
    }
    System.exit(0);
    left = new DcMotor(0);
    right = new DcMotor(1);
    tm =  new ElapsedTime();

    opMode = new LinearOpMode();
    lift = new LiftControl(opMode, left, right);
    dump("Before");
    sleep(100);
    dump("100 later");
    lift.up();
    sleep(1000);
    lift.stop();
    dump("up for 1000");
    lift.AcquireBrickWait();
    dump("Just acquired");
    java.util.Random rnd = new java.util.Random(System.currentTimeMillis());
    for (int i = 0; i < 20; i++) {
      int which = Math.abs(rnd.nextInt() % 7);
      lift.LiftBrickWait(which);
      dump(String.format("Went to %d", which));
      lift.SetBrickWait();
      dump("Setting brick");
    }
    System.exit(0);
  }
}
