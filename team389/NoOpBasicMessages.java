package team389;

import battlecode.common.RobotController;
import battlecode.common.Signal;

public class NoOpBasicMessages implements BasicMessages {

  @Override
  public Signal[] getAllyBasicMessages() {
    return null;
  }

  @Override
  public Signal[] getEnemyBasicMessages() {
    return null;
  }

  @Override
  public void addMessage(Signal s, RobotController rc) {}

  @Override
  public void clearBasicMessages() {}

}
