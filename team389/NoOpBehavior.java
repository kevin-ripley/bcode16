package team389;

import battlecode.common.RobotController;

public class NoOpBehavior implements PreBehavior, Behavior {

  private final RobotController rc;

  public NoOpBehavior(RobotController rc) {
    this.rc = rc;
  }

  @Override
  public void preRun() {}

  @Override
  public void run() {
    rc.setIndicatorString(0, "I ain't doin shiet.");
  }
}
