package team389;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.Signal;

public class MatchObservationEnemyMessageProcessor implements EnemyMessageProcessor {

  private final RobotController rc;

  public MatchObservationEnemyMessageProcessor(RobotController rc) {
    this.rc = rc;
  }

  @Override
  public void processEnemyMessage(Signal s) throws GameActionException {
    int roundNum = rc.getRoundNum();
    int[] message = s.getMessage();
    String observation = message != null && message.length == 2
        ? "round " + roundNum + ", received [" + message[0] + ", " + message[1] + "] from "
            + s.getID() + " at " + s.getLocation()
        : "round " + roundNum + ", received null from " + s.getID() + " at " + s.getLocation();
    rc.addMatchObservation(observation);
  }
}
