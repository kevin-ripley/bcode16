package team389;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public interface NavigationSafetyPolicy {
  public boolean isSafeToMoveTo(RobotController rc, MapLocation loc) throws GameActionException;
}