package team389;

import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class DryadArcherUnitOrder implements UnitOrder {

  private RobotController rc;
  private RobotType nextUnit;
  private static final int MAKE_VIPER_ROUND = 275;
  private int turretsMade;
  private int vipersMade;

  public DryadArcherUnitOrder(RobotController rc) {
    this.rc = rc;
    this.nextUnit = RobotType.SOLDIER;
    this.turretsMade = 0;
    this.vipersMade = 0;
  }

  @Override
  public RobotType getNextUnit() {
    return nextUnit;
  }

  @Override
  public void computeNextUnit() {
    if (rc.getRoundNum() <= MAKE_VIPER_ROUND) {
      nextUnit = RobotType.SOLDIER;
      return;
    }
    if (nextUnit == RobotType.TURRET) {
      nextUnit = RobotType.SCOUT;
      return;
    }
    double rand = Math.random();
    if (rand <= .5) {
      if (3 * vipersMade <= turretsMade) {
        nextUnit = RobotType.VIPER;
        vipersMade++;
      } else {
        nextUnit = RobotType.TURRET;
        turretsMade++;
      }
    } else {
      nextUnit = RobotType.SOLDIER;
    }
  }
}
