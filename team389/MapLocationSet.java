package team389;

import battlecode.common.MapLocation;

public interface MapLocationSet {

  public boolean contains(MapLocation loc);

  public void add(MapLocation loc);

  public void remove(MapLocation loc);
}
