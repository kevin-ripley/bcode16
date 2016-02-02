package team389;

import battlecode.common.MapLocation;

public interface MapLocationIntMap {

  public int getValue(MapLocation loc);

  public void increment(MapLocation loc);

  public void decrement(MapLocation loc);
}
