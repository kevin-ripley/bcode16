package team389;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class DefaultArmyRally implements ArmyRally, ArmyRallyReporter {

  private static final int RALLY_TIMEOUT = 15;

  private final RobotController rc;

  private MapLocation rally;
  private int lastRallyRound;

  public DefaultArmyRally(RobotController rc) {
    this.rc = rc;
    rally = null;
    lastRallyRound = -1;
  }

  @Override
  public void reportRally(MapLocation loc) {
    lastRallyRound = rc.getRoundNum();
    rally = loc;
  }

  @Override
  public MapLocation getRally() {
    return rally != null && lastRallyRound + RALLY_TIMEOUT >= rc.getRoundNum()
        ? rally
        : null;
  }
}
