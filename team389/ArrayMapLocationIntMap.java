package team389;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;

public class ArrayMapLocationIntMap implements MapLocationSet, MapLocationIntMap {

  private static final int SET_SIZE = GameConstants.MAP_MAX_HEIGHT * GameConstants.MAP_MAX_WIDTH;

  private int[] set;

  public ArrayMapLocationIntMap() {
    set = new int[SET_SIZE];
  }

  @Override
  public boolean contains(MapLocation loc) {
    return set[getHash(loc)] > 0;
  }

  @Override
  public void add(MapLocation loc) {
    set[getHash(loc)] = 1;
  }

  @Override
  public void remove(MapLocation loc) {
    set[getHash(loc)] = 0;
  }

  @Override
  public int getValue(MapLocation loc) {
    return set[getHash(loc)];
  }

  @Override
  public void increment(MapLocation loc) {
    set[getHash(loc)]++;
  }

  @Override
  public void decrement(MapLocation loc) {
    set[getHash(loc)]--;
  }

  private static int getHash(MapLocation loc) {
    return (loc.x + 16000 + GameConstants.MAP_MAX_HEIGHT * (loc.y + 16000))
        % (GameConstants.MAP_MAX_HEIGHT * GameConstants.MAP_MAX_WIDTH);
  }
}
