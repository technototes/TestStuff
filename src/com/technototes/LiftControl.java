package com.technototes;

public class LiftControl {
  // The amount we divide speed by when dropping the lift
  private static final double DOWNWARD_SCALE = 2.0;

  // This is how many 'ticks' a brick is
  private static int BRICK_HEIGHT = 1100;

  // This is how high the base plate is (to get *over* it while holding a brick)
  private static int BASE_PLATE_HEIGHT = 400;

  // This is the height offset for placing a brick
  private static int PLACE_HEIGHT_OFFSET = 100;

  // How many ticks should we be within for 'zero'
  private static int ZERO_TICK_RANGE = 150;

  // How many ticks should we be within for a given height
  private static int POSITION_TICK_RANGE = 75;

  DcMotor left;
  DcMotor right;

  int lZero;
  int rZero;

  // to check opModeIsActive()...
  private LinearOpMode opMode;

  public LiftControl(LinearOpMode op, DcMotor leftMotor, DcMotor rightMotor) {
    opMode = op;

    left = leftMotor;
    right = rightMotor;
    // make lift motors work together: they're facing opposite directions
    left.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    right.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

    left.setDirection(DcMotor.Direction.FORWARD);
    right.setDirection(DcMotor.Direction.REVERSE);

    // Make the motors *stop* when we hit zero, not float around
    left.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    right.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

    lZero = left.getCurrentPosition();
    rZero = right.getCurrentPosition();
  }

  public void up() {
    setLiftPower(-1.0);
  }

  public void down() {
    if (!atLowerLimit())
      setLiftPower(1.0);
  }

  public void overrideDown() {
    setLiftPower(1.0);
  }

  public void stop() {
    setLiftPower(0);
  }

  // This is a little more paranoid that 'In the bottom end of the range'
  // to try to prevent more lift axle carnage...
  public boolean atLowerLimit() {
    return BothInRange(0, ZERO_TICK_RANGE) || LeftPos() < 0 || RightPos() < 0;
  }

  // Crash recovery here
  public void ResetZero() {
    lZero = left.getCurrentPosition();
    rZero = right.getCurrentPosition();
  }

  private void setLiftPower(double val) {
    if (val > 0) // If we're headed down, scale the power
      val = val / DOWNWARD_SCALE;
    left.setPower(val);
    right.setPower(val);
  }

  // Head toward the height at 'zero' to grab a brick in front of the bot
  // Returns true (which you can ignore) if it's stopped
  public boolean AcquireBrick() {
    // This just goes down until we're at the lower limit
    if (atLowerLimit()) {
      stop();
      return true;
    }
    down();
    return false;
  }

  // Synchronously moves to the 'grab a brick' height
  public void AcquireBrickWait() {
    while (!AcquireBrick() && opMode.opModeIsActive()) {
      opMode.sleep(1);
    }
  }

  public boolean GoToPosition(int target) {
    if (AverageInRange(target, POSITION_TICK_RANGE)) {
      stop();
      return true;
    }

    int avg = AveragePos();
    if (avg > target) {
      down();
    } else {
      up();
    }
    return false;
  }

  // Lift a brick to 'positioning' height (0: baseplate placing height, 1: bp+1, etc...)
  public boolean LiftBrick(int brickHeight) {
    return GoToPosition(brickHeight * BRICK_HEIGHT + BASE_PLATE_HEIGHT);
  }

  // Wait to lift a brick to 'positioning' height
  public void LiftBrickWait(int brickHeight) {
    while (!LiftBrick(brickHeight) && opMode.opModeIsActive()) {
      opMode.sleep(1);
    }
  }

  // Move *down* to the nearest 'whole brick on top of the baseplate' height
  // This height should be the right height to release a brick on the stack
  public boolean SetBrick() {
    int cur = AveragePos();
    int targetLevel = cur / BRICK_HEIGHT;
    int target = targetLevel * BRICK_HEIGHT + PLACE_HEIGHT_OFFSET;
    if (Math.abs(cur - target) < POSITION_TICK_RANGE) {
      stop();
      return true;
    }
    // Just in case, let's make sure we don't try to slam into the bot...
    if (atLowerLimit()) {
      stop();
      return true;
    }
    // We're not within that range, so keep moving down
    // This might result in overshooting. Could make this thing use PID at some point...
    down();
    return false;
  }

  // Synchronous version of the SetBrick function
  public void SetBrickWait() {
    while (!SetBrick() && opMode.opModeIsActive()) {
      opMode.sleep(1);
    }
  }

  // Helpers

  private int LeftPos() {
    return left.getCurrentPosition() - lZero;
  }

  private int RightPos() {
    return right.getCurrentPosition() - rZero;
  }

  private int AveragePos() {
    return (LeftPos() + RightPos()) / 2;
  }

  private boolean BothInRange(int target, int error) {
    return (Math.abs(target - LeftPos()) < error) && (Math.abs(target - RightPos()) < error);
  }

  private boolean AverageInRange(int target, int error) {
    return Math.abs(target - AveragePos()) < error;
  }
}
