package team389;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public interface DistantHostileReporter {

  public void reportDistantHostile(
      MapLocation loc, int coreDelayTenths, boolean isZombie) throws GameActionException;
}
