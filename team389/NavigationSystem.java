package team389;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public interface NavigationSystem {

  public boolean directTo(
      MapLocation loc,
      boolean avoidAttackers,
      boolean clearRubble) throws GameActionException;

  public boolean directToOnlyForward(
      MapLocation loc,
      boolean avoidAttackers,
      boolean clearRubble) throws GameActionException;

  public boolean directToOnlyForwardAndSides(
      MapLocation loc,
      boolean avoidAttackers,
      boolean clearRubble) throws GameActionException;

  public boolean directToWithoutBlockingAllyRetreat(
      Direction dir, MapLocation retreatFromLoc) throws GameActionException;

  public boolean directToOnlyNonDiagonal(Direction dir) throws GameActionException;

  public boolean directToWithMaximumEnemyExposure(
      MapLocation loc,
      int maximumEnemyExposure) throws GameActionException;

  public boolean directToAvoidingAlliedArchons(
      MapLocation loc,
      int avoidDist,
      boolean clearRubble) throws GameActionException;

  public boolean bugTo(
      MapLocation loc,
      boolean avoidAttackers,
      boolean clearRubble) throws GameActionException;

  public boolean moveRandomly() throws GameActionException;
}
