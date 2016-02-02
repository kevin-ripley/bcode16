package team389;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;
import team389.InterestingLocationCache.Invalidator;
import team389.InterestingLocationCache.Sharer;

public class DefaultPickupLocationReporter implements PickupLocationReporter, Invalidator, Sharer {

  private static final int PICKUP_LOCATION_NEARNESS_THRESHOLD = 32;
  private static final int PICKUP_LOCATION_TARGET_TIMEOUT = 200;
  private static final int MAX_NEW_PICKUPS_TO_SEARCH = 3;

  private final RobotController rc;
  private final InterestingLocationCache interestingCache;

  public DefaultPickupLocationReporter(RobotController rc) {
    this.rc = rc;
    interestingCache = new DefaultInterestingLocationCache(
        rc, PICKUP_LOCATION_NEARNESS_THRESHOLD, PICKUP_LOCATION_TARGET_TIMEOUT);
  }

  @Override
  public boolean isStillInteresting(MapLocation loc) throws GameActionException {
    // Invalidate pickup location once you're adjacent or on it.
    return rc.getLocation().distanceSquaredTo(loc) > 2;
  };

  @Override
  public void shareOldNotRemoved(
      MapLocation loc, MessageSender messageSender) throws GameActionException {}

  @Override
  public void shareNewAdd(
      MapLocation loc, MessageSender messageSender) throws GameActionException {
    messageSender.sendPickupLocationEverywhere(loc);
  }

  @Override
  public void shareNewRemove(
      MapLocation loc, MessageSender messageSender) throws GameActionException {}

  @Override
  public void reportPickup(MapLocation loc) {
    interestingCache.add(loc);
  }

  @Override
  public void findNearbyPickups() {
    int sensorRange = rc.getType().sensorRadiusSquared;
    MapLocation[] parts = rc.sensePartLocations(sensorRange);
    for (int i = 0; i < MAX_NEW_PICKUPS_TO_SEARCH && i < parts.length; i++) {
      interestingCache.addNew(parts[i]);
    }

    RobotInfo[] neutrals = rc.senseNearbyRobots(sensorRange, Team.NEUTRAL);
    for (int i = 0; i < MAX_NEW_PICKUPS_TO_SEARCH && i < neutrals.length; i++) {
      interestingCache.addNew(neutrals[i].location);
    }
  }

  @Override
  public void invalidateRetrievedPickups() throws GameActionException {
    interestingCache.invalidateNoLongerInteresting(this /* invalidator */);
  }

  @Override
  public MapLocation getClosestPickup(MapLocation loc) {
    return interestingCache.getClosestNotRemoved(loc);
  }

  @Override
  public void shareNewPickups(MessageSender messageSender) throws GameActionException {
    interestingCache.shareNewlyAddedAndRemoved(this /* sharer */, messageSender);
  }

  @Override
  public void showDebugInfo() throws GameActionException {
    interestingCache.showDebugInfo(255, 165, 0);
  }
}
