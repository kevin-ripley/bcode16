package team389;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class DefaultInterestingLocationCache implements InterestingLocationCache {

  private static final int LOCS_ARRAY_MAX_SIZE = 50;

  private final RobotController rc;
  /**
   * The cache will not hold two locations whose distance is less than or equal to this threshold.
   */
  private final int nearnessThreshold;
  /**
   * getClosestNotRemoved will not return a target more than this many times consecutively. To
   * disable blacklisting, give a non-positive value.
   */
  private final int targetTimeout;

  private MapLocation[] locs;
  private MapLocationSet addedLocs;
  private MapLocationSet deletedLocs;
  private int totalLocs;

  private MapLocation[] newAddedLocs;
  private MapLocation[] newDeletedLocs;
  private int totalNewAddedLocs;
  private int totalNewDeletedLocs;

  private MapLocationSet blacklist;
  private MapLocation lastTarget;
  private int turnsUntilBlacklist;

  public DefaultInterestingLocationCache(
      RobotController rc, int nearnessThreshold, int targetTimeout) {
    this.rc = rc;
    this.nearnessThreshold = nearnessThreshold;
    this.targetTimeout = targetTimeout;

    locs = new MapLocation[LOCS_ARRAY_MAX_SIZE];
    addedLocs = new ArrayMapLocationIntMap();
    deletedLocs = new ArrayMapLocationIntMap();
    totalLocs = 0;

    newAddedLocs = new MapLocation[LOCS_ARRAY_MAX_SIZE];
    newDeletedLocs = new MapLocation[LOCS_ARRAY_MAX_SIZE];
    totalNewAddedLocs = 0;
    totalNewDeletedLocs = 0;

    blacklist = new ArrayMapLocationIntMap();
    lastTarget = null;
    turnsUntilBlacklist = targetTimeout;
  }

  @Override
  public void add(MapLocation loc) {
    add(loc, false /* isNew */);
  }

  @Override
  public void addNew(MapLocation loc) {
    add(loc, true /* isNew */);
  }

  private void add(MapLocation loc, boolean isNew) {
    if (deletedLocs.contains(loc) || addedLocs.contains(loc)) {
      return;
    }
    if (maybeAdd(locs, loc, totalLocs)) {
      addedLocs.add(loc);
      totalLocs++;
      if (isNew && maybeAdd(newAddedLocs, loc, totalNewAddedLocs)) {
        totalNewAddedLocs++;
      }
    }
  }

  private boolean maybeAdd(MapLocation[] array, MapLocation el, int currentSize) {
    if (currentSize >= array.length) {
      return false;
    }

    for (int i = currentSize; --i >= 0;) {
      MapLocation loc = array[i];
      boolean shouldAdd = nearnessThreshold == 0
          ? loc.equals(el)
          : loc.distanceSquaredTo(el) <= nearnessThreshold;
      if (shouldAdd) {
        return false;
      }
    }

    array[currentSize] = el;
    return true;
  }

  @Override
  public void remove(MapLocation loc) {
    remove(loc, false /* isNew */);
  }

  @Override
  public void removeNew(MapLocation loc) {
    remove(loc, true /* isNew */);
  }

  private void remove(MapLocation loc, boolean isNew) {
    if (!deletedLocs.contains(loc)) {
      deletedLocs.add(loc);
      if (isNew && maybeAdd(newDeletedLocs, loc, totalNewDeletedLocs)) {
        totalNewDeletedLocs++;
      }
    }
    if (!addedLocs.contains(loc) && maybeAdd(locs, loc, totalLocs)) {
      addedLocs.add(loc);
      totalLocs++;
    }
  }

  @Override
  public void invalidateNoLongerInteresting(Invalidator invalidator) throws GameActionException {
    for (int i = totalLocs; --i >= 0;) {
      MapLocation loc = locs[i];
      if (!invalidator.isStillInteresting(loc)) {
        removeNew(loc);
      }
    }
  }

  @Override
  public void shareAllNotRemoved(
      Sharer sharer, MessageSender messageSender) throws GameActionException {
    for (int i = totalLocs; --i >= 0;) {
      MapLocation loc = locs[i];
      if (!deletedLocs.contains(loc)) {
        sharer.shareOldNotRemoved(loc, messageSender);
      }
    }
  }

  @Override
  public void shareNewlyAddedAndRemoved(
      Sharer sharer, MessageSender messageSender) throws GameActionException {
    for (int i = totalNewAddedLocs; --i >= 0;) {
      sharer.shareNewAdd(newAddedLocs[i], messageSender);
    }
    for (int i = totalNewDeletedLocs; --i >= 0;) {
      sharer.shareNewRemove(newDeletedLocs[i], messageSender);
    }
    totalNewAddedLocs = 0;
    totalNewDeletedLocs = 0;
  }

  @Override
  public MapLocation getClosestNotRemoved(MapLocation loc) {
    int closestDist = 99999;
    MapLocation closest = null;
    for (int i = totalLocs; --i >= 0;) {
      MapLocation el = locs[i];
      int dist = loc.distanceSquaredTo(el);
      if (!deletedLocs.contains(el)
          && !blacklist.contains(el)
          && (closest == null || dist < closestDist)) {
        closestDist = dist;
        closest = el;
      }
    }

    maybeBlacklist(closest);
    return closest;
  }

  private void maybeBlacklist(MapLocation target) {
    if (targetTimeout <= 0) {
      return;
    }

    if (lastTarget != null && target != null && target.equals(lastTarget)) {
      if (--turnsUntilBlacklist <= 0) {
        turnsUntilBlacklist = targetTimeout;
        blacklist.add(target);
      }
      return;
    }

    lastTarget = target;
    turnsUntilBlacklist = targetTimeout;
  }

  @Override
  public void showDebugInfo(int red, int green, int blue) {
    MapLocation myLoc = rc.getLocation();
    for (int i = totalLocs; --i >= 0;) {
      MapLocation loc = locs[i];
      if (!deletedLocs.contains(loc)) {
        rc.setIndicatorLine(myLoc, loc, red, green, blue);
      }
    }
  }
}
