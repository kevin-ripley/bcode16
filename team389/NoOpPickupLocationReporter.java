package team389;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class NoOpPickupLocationReporter implements PickupLocationReporter {

  @Override
  public void reportPickup(MapLocation loc) {}

  @Override
  public void findNearbyPickups() throws GameActionException {}

  @Override
  public void invalidateRetrievedPickups() throws GameActionException {}

  @Override
  public MapLocation getClosestPickup(MapLocation loc) {
    return null;
  }

  @Override
  public void shareNewPickups(MessageSender messageSender) throws GameActionException {}

  @Override
  public void showDebugInfo() throws GameActionException {}
}
