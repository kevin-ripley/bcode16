package team389;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import team389.InterestingLocationCache.Invalidator;
import team389.InterestingLocationCache.Sharer;

public class DefaultZombieDenReporter implements ZombieDenReporter, Invalidator, Sharer {

  private final RobotController rc;
  private final Radar radar;
  private final InterestingLocationCache interestingCache;

  public DefaultZombieDenReporter(RobotController rc, Radar radar) {
    this.rc = rc;
    this.radar = radar;
    interestingCache = new DefaultInterestingLocationCache(
        rc, 0 /* nearnessThresold */, 0 /* targetTimeout */);
  }

  @Override
  public boolean isStillInteresting(MapLocation loc) throws GameActionException {
    if (!rc.canSenseLocation(loc)) {
      return true;
    }

    RobotInfo robot = rc.senseRobotAtLocation(loc);
    return robot != null && robot.type == RobotType.ZOMBIEDEN;
  }

  @Override
  public void shareOldNotRemoved(
      MapLocation loc, MessageSender messageSender) throws GameActionException {
    messageSender.sendDenLocationNearby(loc);
  }

  @Override
  public void shareNewAdd(
      MapLocation loc, MessageSender messageSender) throws GameActionException {
    messageSender.sendDenLocationEverywhere(loc);
  }

  @Override
  public void shareNewRemove(
      MapLocation loc, MessageSender messageSender) throws GameActionException {
    messageSender.sendDenLocationRemovedEverywhere(loc);
  }

  @Override
  public void reportDen(MapLocation denLoc) {
    interestingCache.add(denLoc);
  }

  @Override
  public void reportDenDestroyed(MapLocation denLoc) {
    interestingCache.remove(denLoc);
  }

  @Override
  public void searchForNewDens() throws GameActionException {
    RobotInfo[] zombies = radar.getNearbyZombies();
    for (int i = zombies.length; --i >= 0;) {
      RobotInfo zombie = zombies[i];
      MapLocation loc = zombie.location;
      if (zombie.type == RobotType.ZOMBIEDEN) {
        interestingCache.addNew(loc);
      }
    }
  }

  @Override
  public void invalidateNearbyDestroyedDens() throws GameActionException {
    interestingCache.invalidateNoLongerInteresting(this /* invalidator */);
  }

  @Override
  public void shareAllDens(MessageSender messageSender) throws GameActionException {
    interestingCache.shareAllNotRemoved(this /* sharer */, messageSender);
  }

  @Override
  public void shareNewlyAddedAndRemovedDens(MessageSender messageSender)
      throws GameActionException {
    interestingCache.shareNewlyAddedAndRemoved(this /* sharer */, messageSender);
  }

  @Override
  public MapLocation getClosestDen(MapLocation loc, MapLocationSet blacklist) {
    return interestingCache.getClosestNotRemoved(loc);
  }

  @Override
  public void showDebugInfo() {
    interestingCache.showDebugInfo(0 /* red */, 255 /* green */, 0 /* blue */);
  }
}
