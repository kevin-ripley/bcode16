package team389;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class EmptyEnemyTurretCache implements EnemyTurretCache {

  @Override
  public void reportEnemyTurretPresent(MapLocation loc, int timestamp) {}

  @Override
  public void reportEnemyTurretAbsent(MapLocation loc, int timestamp) {}

  @Override
  public boolean isInEnemyTurretRange(MapLocation loc) {
    return false;
  }

  @Override
  public void invalidateNearbyEnemyTurrets() throws GameActionException {}

  @Override
  public MapLocation getNewestTurret() {
    return null;
  }

  @Override
  public void shareRandomEnemyTurret(MessageSender messageSender) throws GameActionException {}

  @Override
  public void showDebugInfo() throws GameActionException {}
}
