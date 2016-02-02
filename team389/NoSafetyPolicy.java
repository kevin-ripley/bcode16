package team389;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class NoSafetyPolicy implements NavigationSafetyPolicy {

  @Override
  public boolean isSafeToMoveTo(RobotController rc, MapLocation loc) {
    return true;
  }
}