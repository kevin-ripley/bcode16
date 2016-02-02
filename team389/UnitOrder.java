package team389;

import battlecode.common.RobotType;

public interface UnitOrder {

  public RobotType getNextUnit();

  public void computeNextUnit();
}
