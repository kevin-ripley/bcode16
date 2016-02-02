package team389;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public interface EnemyTurretCache {

  public void reportEnemyTurretPresent(MapLocation loc, int timestamp);

  public void reportEnemyTurretAbsent(MapLocation loc, int timestamp);

  public boolean isInEnemyTurretRange(MapLocation loc);

  public void invalidateNearbyEnemyTurrets() throws GameActionException;

  public MapLocation getNewestTurret();

  public void shareRandomEnemyTurret(MessageSender messageSender) throws GameActionException;

  public void showDebugInfo() throws GameActionException;
}
