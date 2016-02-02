package team389;

import battlecode.common.MapLocation;

public class NoOpZombieDenReporter implements ZombieDenReporter {

  @Override
  public void reportDen(MapLocation denLoc) {}

  @Override
  public void reportDenDestroyed(MapLocation denLoc) {}

  @Override
  public void searchForNewDens() {};

  @Override
  public void invalidateNearbyDestroyedDens() {}

  @Override
  public void shareAllDens(MessageSender messageSender) {}

  @Override
  public void shareNewlyAddedAndRemovedDens(MessageSender messageSender) {}

  @Override
  public MapLocation getClosestDen(MapLocation loc, MapLocationSet blacklist) {
    return null;
  }

  @Override
  public void showDebugInfo() {}
}
