package team389;

import battlecode.common.MapLocation;

public interface AlliedArchonTracker {

  public static class AlliedArchonInfo {
    public final int id;
    public final MapLocation loc;
    public final int timestamp;

    public AlliedArchonInfo(int id, MapLocation loc, int timestamp) {
      this.id = id;
      this.loc = loc;
      this.timestamp = timestamp;
    }
  }

  public void reportAlliedArchon(int archonId, MapLocation archonLoc);

  public AlliedArchonInfo getClosestAlliedArchon(MapLocation myLoc);

  public AlliedArchonInfo getLowestIdAlliedArchon(MapLocation myLoc);

  public AlliedArchonInfo[] getAlliedArchons();

  public boolean isInRangeOfAlliedArchon(MapLocation loc, int range);

  public void showDebugInfo();
}
