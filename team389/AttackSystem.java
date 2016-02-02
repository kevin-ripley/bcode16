package team389;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public interface AttackSystem {
  public MapLocation getBestEnemyToShoot(RobotController rc, RobotInfo[] enemies,
      int numEnemies,
      RobotInfo[] allies, int numAllies);
}
