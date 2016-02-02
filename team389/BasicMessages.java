package team389;

import battlecode.common.RobotController;
import battlecode.common.Signal;

public interface BasicMessages {
  Signal[] getAllyBasicMessages();

  Signal[] getEnemyBasicMessages();

  void addMessage(Signal s, RobotController rc);

  void clearBasicMessages();
}
