package team389;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class NonPersistentAlliedArchonTracker implements AlliedArchonTracker {

  private static final int MAX_ARCHONS = 10;

  private final RobotController rc;
  private final int[] ids;
  private final MapLocation[] locs;
  private final int[] timestamps;

  private int numArchons;
  private int lastUpdateRound;
  private AlliedArchonInfo closestAlliedArchon;

  public NonPersistentAlliedArchonTracker(RobotController rc) {
    this.rc = rc;
    ids = new int[MAX_ARCHONS];
    locs = new MapLocation[MAX_ARCHONS];
    timestamps = new int[MAX_ARCHONS];
    numArchons = 0;
    lastUpdateRound = -1;
  }

  @Override
  public void reportAlliedArchon(int archonId, MapLocation archonLoc) {
    maybeClear();
    lastUpdateRound = rc.getRoundNum();
    for (int i = numArchons; --i >= 0;) {
      if (ids[i] == archonId) {
        locs[i] = archonLoc;
        timestamps[i] = rc.getRoundNum();
        return;
      }
    }

    if (numArchons < ids.length) {
      ids[numArchons] = archonId;
      locs[numArchons] = archonLoc;
      numArchons++;
    }
  }

  @Override
  public AlliedArchonInfo getClosestAlliedArchon(MapLocation myLoc) {
    maybeClear();
    if (closestAlliedArchon != null) {
      return closestAlliedArchon;
    }
    int closestDist = 99999;
    int closestId = -1;
    MapLocation closest = null;
    int closetTimestamp = 99999;
    for (int i = numArchons; --i >= 0;) {
      MapLocation loc = locs[i];
      int dist = myLoc.distanceSquaredTo(loc);
      if (closest == null || dist < closestDist) {
        closest = loc;
        closestId = ids[i];
        closestDist = dist;
        closetTimestamp = timestamps[i];
      }
    }

    closestAlliedArchon = closest != null ? new AlliedArchonInfo(closestId, closest,
        closetTimestamp) : null;
    return closestAlliedArchon;
  }

  @Override
  public AlliedArchonInfo[] getAlliedArchons() {
    maybeClear();
    AlliedArchonInfo[] alliedArchons = new AlliedArchonInfo[numArchons];
    for (int i = numArchons; --i >= 0;) {
      alliedArchons[i] = new AlliedArchonInfo(ids[i], locs[i], timestamps[i]);
    }
    return alliedArchons;
  }

  @Override
  public AlliedArchonInfo getLowestIdAlliedArchon(MapLocation myLoc) {
    maybeClear();
    int leastId = 99999;
    MapLocation leastIdLoc = null;
    int leastTimestamp = 99999;
    for (int i = numArchons; --i >= 0;) {
      int id = ids[i];
      if (leastIdLoc == null || id < leastId) {
        leastIdLoc = locs[i];
        leastId = id;
        leastTimestamp = timestamps[i];
      }
    }
    return leastIdLoc == null || rc.getType() == RobotType.ARCHON && rc.getID() < leastId
        ? null
        : new AlliedArchonInfo(leastId, leastIdLoc, leastTimestamp);
  }

  @Override
  public boolean isInRangeOfAlliedArchon(MapLocation loc, int range) {
    maybeClear();
    for (int i = numArchons; --i >= 0;) {
      if (loc.distanceSquaredTo(locs[i]) <= range) {
        return true;
      }
    }

    return false;
  }

  @Override
  public void showDebugInfo() {
    maybeClear();
    for (int i = numArchons; --i >= 0;) {
      rc.setIndicatorLine(rc.getLocation(), locs[i], 255, 255, 200);
    }
  }

  private void maybeClear() {
    if (rc.getRoundNum() > lastUpdateRound + ArchonBehavior.POS_BROADCAST_WAIT) {
      numArchons = 0;
      closestAlliedArchon = null;
    }
  }
}
