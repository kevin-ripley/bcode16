package team389;

import battlecode.common.MapLocation;

public class NoOpDistantHostileReporter implements DistantHostileReporter {

  @Override
  public void reportDistantHostile(MapLocation loc, int coreDelayTenths, boolean isZombie) {}
}
