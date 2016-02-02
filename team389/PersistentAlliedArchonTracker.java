package team389;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class PersistentAlliedArchonTracker implements AlliedArchonTracker {

  private static final int MAX_ARCHONS = 10;

  private final RobotController rc;
  private final int[] ids;
  private final MapLocation[] locs;
  private final int[] timestamps;

  private int numArchons;
  private int lastUpdateRound;
  private AlliedArchonInfo closestAlliedArchon;

  public PersistentAlliedArchonTracker(RobotController rc) {
    this.rc = rc;
    ids = new int[MAX_ARCHONS];
    locs = new MapLocation[MAX_ARCHONS];
    timestamps = new int[MAX_ARCHONS];
    numArchons = 0;
    lastUpdateRound = -1;
  }

  @Override
  public void reportAlliedArchon(int archonId, MapLocation archonLoc) {
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
    if (lastUpdateRound == rc.getRoundNum()) {
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

    lastUpdateRound = rc.getRoundNum();
    closestAlliedArchon = closest != null ? new AlliedArchonInfo(closestId, closest,
        closetTimestamp) : null;
    return closestAlliedArchon;
  }

  @Override
  public AlliedArchonInfo[] getAlliedArchons() {
    AlliedArchonInfo[] alliedArchons = new AlliedArchonInfo[numArchons];
    for (int i = numArchons; --i >= 0;) {
      alliedArchons[i] = new AlliedArchonInfo(ids[i], locs[i], timestamps[i]);
    }
    return alliedArchons;
  }

  @Override
  public AlliedArchonInfo getLowestIdAlliedArchon(MapLocation myLoc) {
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
    for (int i = numArchons; --i >= 0;) {
      if (loc.distanceSquaredTo(locs[i]) <= range) {
        return true;
      }
    }

    return false;
  }

  @Override
  public void showDebugInfo() {
    for (int i = numArchons; --i >= 0;) {
      rc.setIndicatorLine(rc.getLocation(), locs[i], 255, 255, 200);
    }
  }
}
