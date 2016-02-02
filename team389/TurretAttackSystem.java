package team389;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class TurretAttackSystem implements AttackSystem {

  @Override
  public MapLocation getBestEnemyToShoot(RobotController rc,
      RobotInfo[] enemies,
      int numEnemies,
      RobotInfo[] allies,
      int numAllies) {
    RobotPlayer.profiler.split("before best turret enemy");

    MapLocation bestLocation = null;
    double bestScore = 0;
    for (int i = numEnemies; --i >= 0;) {
      if (!rc.canAttackLocation(enemies[i].location)) {
        continue;
      }
      RobotInfo info = enemies[i];
      double score = getScoreOfRobot(rc, info, allies, numAllies);
      if (score > bestScore) {
        bestScore = score;
        bestLocation = info.location;
      }
    }
    RobotPlayer.profiler.split("after best turret enemy");
    return bestLocation;
  }

  private static int getNumAlliesWhoCanAttackLocation(RobotController rc, MapLocation location,
      RobotInfo[] allies, int numAllies) {
    return rc.senseNearbyRobots(location, RobotType.SOLDIER.attackRadiusSquared, rc
        .getTeam()).length;
  }

  private static double getScoreOfRobot(RobotController rc, RobotInfo enemy, RobotInfo[] allies,
      int numAllies) {
    double score = 10000;
    boolean inRange = enemy.location.distanceSquaredTo(rc.getLocation()) <= rc
        .getType().sensorRadiusSquared;
    if (inRange || enemy.coreDelay >= 2) {
      score += 1000000;
    }
    if (enemy.type == RobotType.GUARD) {
      score -= 5000;
    }

    if (inRange) {
      int numNearbyAllies = 1 + getNumAlliesWhoCanAttackLocation(rc, enemy.location, allies,
          numAllies);
      double turnsToKill = enemy.health / numNearbyAllies;
      score -= turnsToKill;
    } else {
      score += enemy.coreDelay; // Hope to kill someone with a high core delay
                                // by them going over bytecodes
    }
    return score;
  }
}
