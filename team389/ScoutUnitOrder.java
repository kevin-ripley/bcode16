package team389;

import battlecode.common.RobotType;

public class ScoutUnitOrder implements UnitOrder {

  @Override
  public RobotType getNextUnit() {
    return RobotType.SCOUT;
  }

  @Override
  public void computeNextUnit() {}
}
