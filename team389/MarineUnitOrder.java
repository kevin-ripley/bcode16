package team389;

import battlecode.common.RobotType;

public class MarineUnitOrder implements UnitOrder {

  @Override
  public RobotType getNextUnit() {
    return RobotType.SOLDIER;
  }

  @Override
  public void computeNextUnit() {}
}
