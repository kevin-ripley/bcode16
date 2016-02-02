package team389;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class AvoidAlliedArchonsPolicy implements NavigationSafetyPolicy {

  private final AlliedArchonTracker alliedArchonTracker;
  private final EnemyTurretCache enemyTurretCache;
  private final int avoidDist;

  public AvoidAlliedArchonsPolicy(
      AlliedArchonTracker alliedArchonTracker,
      EnemyTurretCache enemyTurretCache,
      int avoidDist) {
    this.alliedArchonTracker = alliedArchonTracker;
    this.enemyTurretCache = enemyTurretCache;
    this.avoidDist = avoidDist;
  }

  @Override
  public boolean isSafeToMoveTo(RobotController rc, MapLocation loc) {
    return !alliedArchonTracker.isInRangeOfAlliedArchon(loc, avoidDist)
        && !enemyTurretCache.isInEnemyTurretRange(loc);
  }
}
