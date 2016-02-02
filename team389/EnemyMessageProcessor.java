package team389;

import battlecode.common.GameActionException;
import battlecode.common.Signal;

public interface EnemyMessageProcessor {

  public void processEnemyMessage(Signal s) throws GameActionException;
}
