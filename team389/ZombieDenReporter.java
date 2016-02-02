package team389;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public interface ZombieDenReporter {

  public void reportDen(MapLocation denLoc);

  public void reportDenDestroyed(MapLocation denLoc);

  public void searchForNewDens() throws GameActionException;

  public void invalidateNearbyDestroyedDens() throws GameActionException;

  public void shareAllDens(MessageSender messageSender) throws GameActionException;

  public void shareNewlyAddedAndRemovedDens(MessageSender messageSender) throws GameActionException;

  public MapLocation getClosestDen(MapLocation loc, MapLocationSet blacklist);

  public void showDebugInfo();
}
