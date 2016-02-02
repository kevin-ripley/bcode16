package team389;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class AvoidBlockingAllyRetreatPolicy implements NavigationSafetyPolicy {

  private final MapLocation retreatFromLoc;

  public AvoidBlockingAllyRetreatPolicy(MapLocation retreatFromLoc) {
    this.retreatFromLoc = retreatFromLoc;
  }

  @Override
  public boolean isSafeToMoveTo(RobotController rc, MapLocation loc) throws GameActionException {
    MapLocation allyLoc = loc.add(loc.directionTo(retreatFromLoc));
    RobotInfo ally = rc.canSense(allyLoc) ? rc.senseRobotAtLocation(allyLoc) : null;
    return ally == null || ally.team != rc.getTeam();
  }
}
