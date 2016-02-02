package team389;

import battlecode.common.RobotInfo;

public interface Radar {

  public RobotInfo[] getNearbyRobots();

  public RobotInfo[] getNearbyAllies();

  public RobotInfo[] getNearbyEnemies();

  public RobotInfo[] getNearbyZombies();

  public RobotInfo[] getNearbyHostiles();

  public void showDebugInfo();
}
