package team389;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class OpeningArchonBehavior implements Behavior {

  private static final int EARLY_SCOUTS = 1;
  private static final int EARLY_SCOUT_TIMEOUT = 100;

  private final RobotController rc;
  private final UnitOrder unitOrder;

  private int scoutsMade;

  public OpeningArchonBehavior(RobotController rc, UnitOrder unitOrder) {
    this.rc = rc;
    this.unitOrder = unitOrder;
    scoutsMade = 0;
  }

  @Override
  public void run() throws GameActionException {
    if (!isOpeningOver() && UnitSpawner.spawn(rc, unitOrder.getNextUnit())) {
      unitOrder.computeNextUnit();
      scoutsMade++;
    }
  }

  public boolean isOpeningOver() {
    return scoutsMade >= EARLY_SCOUTS || rc.getRoundNum() >= EARLY_SCOUT_TIMEOUT;
  }
}
