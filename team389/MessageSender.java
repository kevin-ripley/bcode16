package team389;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public interface MessageSender {

  public void sendSelfArchonLocation() throws GameActionException;

  public void sendDenLocationNearby(MapLocation loc) throws GameActionException;

  public void sendDenLocationEverywhere(MapLocation loc) throws GameActionException;

  public void sendDenLocationRemovedEverywhere(MapLocation loc) throws GameActionException;

  public void sendPickupLocationEverywhere(MapLocation loc) throws GameActionException;

  public void sendDistantHostileInfo(
      MapLocation loc, double coreDelay, boolean isZombie, int broadcastDistanceSquared)
          throws GameActionException;

  public void sendArmyRallyLocation(
      MapLocation loc, int distanceMultiplier) throws GameActionException;

  public void sendEnemyTurretLocation(
      MapLocation loc, int timestamp) throws GameActionException;

  public void sendEnemyTurretLocationRemoved(
      MapLocation loc, int timestamp) throws GameActionException;

  public void sendTimingAttackEverywhere(
      MapLocation loc, int startRound, int endRound) throws GameActionException;

  public void sendNeedTurretBuddy() throws GameActionException;
}
