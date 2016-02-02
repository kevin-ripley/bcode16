package team389;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class AvoidAttackingUnitsPolicy implements NavigationSafetyPolicy {

  private final Radar radar;
  private final EnemyTurretCache enemyTurretCache;

  public AvoidAttackingUnitsPolicy(Radar radar, EnemyTurretCache enemyTurretCache) {
    this.radar = radar;
    this.enemyTurretCache = enemyTurretCache;
  }

  @Override
  public boolean isSafeToMoveTo(RobotController rc, MapLocation loc) {
    RobotInfo[] hostiles = radar.getNearbyHostiles();
    for (int i = hostiles.length; --i >= 0;) {
      RobotInfo robot = hostiles[i];
      MapLocation robotAttackFromLoc = robot.type.canMove()
          ? robot.location.add(robot.location.directionTo(loc))
          : robot.location;
      if (robot.type.canAttack()
          && loc.distanceSquaredTo(robotAttackFromLoc) <= robot.type.attackRadiusSquared) {
        return false;
      }
    }
    return !enemyTurretCache.isInEnemyTurretRange(loc);
  }
}