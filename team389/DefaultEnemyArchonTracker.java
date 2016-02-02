package team389;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class DefaultEnemyArchonTracker implements EnemyArchonTracker {

  private static final int MAX_ARCHONS = 10;

  private final RobotController rc;
  private final int[] ids;
  private final MapLocation[] locs;
  private final int[] timestamps;

  private int numArchons;

  public DefaultEnemyArchonTracker(RobotController rc) {
    this.rc = rc;
    ids = new int[MAX_ARCHONS];
    locs = new MapLocation[MAX_ARCHONS];
    timestamps = new int[MAX_ARCHONS];
    numArchons = 0;

    MapLocation[] enemyLocs = rc.getInitialArchonLocations(rc.getTeam().opponent());
    for (int i = enemyLocs.length; --i >= 0;) {
      ids[i] = -1;
      locs[i] = enemyLocs[i];
      timestamps[i] = 0;
    }
    numArchons = enemyLocs.length;
  }

  @Override
  public void reportEnemyArchon(int archonId, MapLocation archonLoc, int timestamp) {
    for (int i = numArchons; --i >= 0;) {
      if (ids[i] == archonId || ids[i] == -1) {
        locs[i] = archonLoc;
        timestamps[i] = timestamp;
        return;
      }
    }

    if (numArchons < ids.length) {
      ids[numArchons] = archonId;
      locs[numArchons] = archonLoc;
      timestamps[numArchons] = timestamp;
      numArchons++;
    }
  }

  @Override
  public EnemyArchonInfo getClosestEnemyArchon(MapLocation myLoc) {
    int closestDist = 99999;
    int closestId = -1;
    int closestTimestamp = -1;
    MapLocation closest = null;
    for (int i = numArchons; --i >= 0;) {
      MapLocation loc = locs[i];
      int dist = myLoc.distanceSquaredTo(loc);
      if (closest == null || dist < closestDist) {
        closest = loc;
        closestId = ids[i];
        closestDist = dist;
        closestTimestamp = timestamps[i];
      }
    }

    return closest != null ? new EnemyArchonInfo(closestId, closest, closestTimestamp) : null;
  }

  @Override
  public EnemyArchonInfo[] getSortedEnemyArchonInfos(MapLocation myLoc) {

    // Sort enemy archons by distance
    for (int i = 0; i < numArchons - 1; i++) {
      int minIndex = i;
      int minDistance = locs[i].distanceSquaredTo(rc.getLocation());
      for (int j = i + 1; j < numArchons; j++) {
        int dist = locs[j].distanceSquaredTo(rc.getLocation());
        if (dist < minDistance) {
          minDistance = dist;
          minIndex = j;
        }
      }
      if (minIndex != i) {
        MapLocation tmpLoc = locs[i];
        locs[i] = locs[minIndex];
        locs[minIndex] = tmpLoc;

        int tmpId = ids[i];
        ids[i] = ids[minIndex];
        ids[minIndex] = tmpId;

        int tmpTimestamp = timestamps[i];
        timestamps[i] = timestamps[minIndex];
        timestamps[minIndex] = tmpTimestamp;
      }
    }

    EnemyArchonInfo[] copy = new EnemyArchonInfo[numArchons];
    for (int i = numArchons; --i >= 0;) {
      copy[i] = new EnemyArchonInfo(ids[i], locs[i], timestamps[i]);
    }
    return copy;
  }

  @Override
  public void showDebugInfo() {
    for (int i = numArchons; --i >= 0;) {
      rc.setIndicatorLine(rc.getLocation(), locs[i], 255, 165, 0);
    }
  }

  @Override
  public EnemyArchonInfo[] getEnemyArchons() {
    EnemyArchonInfo[] enemyArchonInfos = new EnemyArchonInfo[numArchons];
    for (int i = numArchons; --i >= 0;) {
      enemyArchonInfos[i] = new EnemyArchonInfo(ids[i], locs[i], timestamps[i]);
    }
    return enemyArchonInfos;
  }
}
