package team389;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public interface InterestingLocationCache {

  public interface Invalidator {
    public boolean isStillInteresting(MapLocation loc) throws GameActionException;
  }

  public interface Sharer {
    public void shareOldNotRemoved(
        MapLocation loc, MessageSender messageSender) throws GameActionException;

    public void shareNewAdd(
        MapLocation loc, MessageSender messageSender) throws GameActionException;

    public void shareNewRemove(
        MapLocation loc, MessageSender messageSender) throws GameActionException;
  }

  public void add(MapLocation loc);

  public void addNew(MapLocation loc);

  public void remove(MapLocation loc);

  public void removeNew(MapLocation loc);

  public void invalidateNoLongerInteresting(Invalidator invalidator) throws GameActionException;

  public void shareAllNotRemoved(
      Sharer sharer, MessageSender messageSender) throws GameActionException;

  public void shareNewlyAddedAndRemoved(
      Sharer sharer, MessageSender messageSender) throws GameActionException;

  // TODO(jven): Blacklist?
  public MapLocation getClosestNotRemoved(MapLocation loc);

  public void showDebugInfo(int red, int green, int blue);
}
