package team389;

import battlecode.common.MapLocation;

public interface EnemyArchonTracker {

  public static class EnemyArchonInfo {
    public final int id;
    public final MapLocation loc;
    public int timestamp;

    public EnemyArchonInfo(int id, MapLocation loc, int timestamp) {
      this.id = id;
      this.loc = loc;
      this.timestamp = timestamp;
    }
  }

  public void reportEnemyArchon(int archonId, MapLocation archonLoc, int timestamp);

  public EnemyArchonInfo getClosestEnemyArchon(MapLocation myLoc);

  public EnemyArchonInfo[] getSortedEnemyArchonInfos(MapLocation myLoc);

  public EnemyArchonInfo[] getEnemyArchons();

  public void showDebugInfo();
}
