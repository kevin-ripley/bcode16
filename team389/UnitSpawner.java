package team389;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class UnitSpawner {

  public static boolean spawn(RobotController rc, RobotType type) throws GameActionException {
    if (!rc.isCoreReady()) {
      return false;
    }

    Direction d = DirectionUtils.getRandomMovableDirection();
    for (int i = 8; --i >= 0;) {
      if (rc.hasBuildRequirements(type) && rc.canBuild(d, type)) {
        rc.build(d, type);
        return true;
      }
      d = d.rotateLeft();
    }

    return false;
  }

  public static boolean spawnInDirection(RobotController rc, RobotType type, Direction d)
      throws GameActionException {
    if (!rc.isCoreReady()) {
      return false;
    }

    if (rc.hasBuildRequirements(type) && rc.canBuild(d, type)) {
      rc.build(d, type);
      return true;
    }

    return false;
  }
}
