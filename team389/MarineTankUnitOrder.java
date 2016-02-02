package team389;

import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class MarineTankUnitOrder implements UnitOrder {

  private RobotController rc;
  private RobotType nextUnit;
  private static final int MAKE_TURRET_ROUND = 300;

  public MarineTankUnitOrder(RobotController rc) {
    this.rc = rc;
    this.nextUnit = RobotType.SOLDIER;
  }

  @Override
  public RobotType getNextUnit() {
    return nextUnit;
  }

  @Override
  public void computeNextUnit() {
    if (rc.getRoundNum() <= MAKE_TURRET_ROUND) {
      nextUnit = RobotType.SOLDIER;
      return;
    }
    if (nextUnit == RobotType.TURRET) {
      nextUnit = RobotType.SCOUT;
    } else {
      nextUnit = Math.random() < .5 ? RobotType.TURRET : RobotType.SOLDIER;
    }
  }
}
