package team389;

import battlecode.common.GameActionException;

public interface EeHanTimingCalculator {

  public void computeAndShareTimingAttack(MessageSender messageSender) throws GameActionException;
}
