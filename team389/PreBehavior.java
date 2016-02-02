package team389;

import battlecode.common.GameActionException;

public interface PreBehavior {

  /** Runs logic before a behavior. */
  public void preRun() throws GameActionException;
}
