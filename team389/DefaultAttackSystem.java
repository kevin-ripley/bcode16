package team389;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class DefaultAttackSystem implements AttackSystem {

  @Override
  public MapLocation getBestEnemyToShoot(RobotController rc,
      RobotInfo[] enemies,
      int numEnemies,
      RobotInfo[] allies,
      int numAllies) {
    RobotPlayer.profiler.split("before best enemy");
    RobotInfo ret = null;
    double bestTurnsToKill = 99999;
    double bestWeaponDelay = 99999;
    for (int i = numEnemies; --i >= 0;) {
      if (!rc.canAttackLocation(enemies[i].location)) {
        continue;
      }
      RobotInfo info = enemies[i];
      int numNearbyAllies = 1 + getNumAlliesWhoCanAttackLocation(rc, info.location, allies,
          numAllies);
      double turnsToKill = info.health / numNearbyAllies;
      if (turnsToKill < bestTurnsToKill) {
        bestTurnsToKill = turnsToKill;
        bestWeaponDelay = info.weaponDelay;
        ret = info;
      } else if (turnsToKill == bestTurnsToKill) {
        double actionDelay = info.weaponDelay;
        if (actionDelay < bestWeaponDelay) {
          bestWeaponDelay = actionDelay;
          ret = info;
        }
      }
    }
    RobotPlayer.profiler.split("after best enemy");
    if (ret != null) {
      return ret.location;
    }
    return null;
  }

  // TODO Could be optimized by assuming soldier range?
  private static int getNumAlliesWhoCanAttackLocation(RobotController rc, MapLocation location,
      RobotInfo[] allies, int numAllies) {
    return rc.senseNearbyRobots(location, rc.getType().attackRadiusSquared, rc.getTeam()).length;
    /*
     * int numAlliesWhoCanAttackLocation = 0; for (int i = numAllies; --i >= 0;)
     * { if (allies[i].location.distanceSquaredTo(location) <=
     * allies[i].type.attackRadiusSquared) { ++numAlliesWhoCanAttackLocation; }
     * } return numAlliesWhoCanAttackLocation;
     */
  }
}
