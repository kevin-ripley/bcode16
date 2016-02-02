package team389;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public interface PickupLocationReporter {

  public void reportPickup(MapLocation loc);

  public void findNearbyPickups() throws GameActionException;

  public void invalidateRetrievedPickups() throws GameActionException;

  public MapLocation getClosestPickup(MapLocation loc);

  public void shareNewPickups(MessageSender messageSender) throws GameActionException;

  public void showDebugInfo() throws GameActionException;
}
