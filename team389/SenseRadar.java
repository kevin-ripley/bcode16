package team389;

import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

public class SenseRadar implements Radar {

  private final RobotController rc;

  private int lastUpdateNearbyRobots;
  private int lastUpdateNearbyAllies;
  private int lastUpdateNearbyEnemies;
  private int lastUpdateNearbyZombies;
  private int lastUpdateNearbyHostiles;

  private RobotInfo[] nearbyRobots;
  private RobotInfo[] nearbyAllies;
  private RobotInfo[] nearbyEnemies;
  private RobotInfo[] nearbyZombies;
  private RobotInfo[] nearbyHostiles;

  public SenseRadar(RobotController rc) {
    this.rc = rc;

    this.lastUpdateNearbyRobots = -1;
    this.lastUpdateNearbyAllies = -1;
    this.lastUpdateNearbyEnemies = -1;
    this.lastUpdateNearbyZombies = -1;
    this.lastUpdateNearbyHostiles = -1;
  }

  @Override
  public RobotInfo[] getNearbyRobots() {
    if (rc.getRoundNum() != lastUpdateNearbyRobots) {
      lastUpdateNearbyRobots = rc.getRoundNum();
      nearbyRobots = rc.senseNearbyRobots();
    }
    return nearbyRobots;
  }

  @Override
  public RobotInfo[] getNearbyAllies() {
    if (rc.getRoundNum() != lastUpdateNearbyAllies) {
      lastUpdateNearbyAllies = rc.getRoundNum();
      nearbyAllies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam());
    }
    return nearbyAllies;
  }

  @Override
  public RobotInfo[] getNearbyEnemies() {
    if (rc.getRoundNum() != lastUpdateNearbyEnemies) {
      lastUpdateNearbyEnemies = rc.getRoundNum();
      nearbyEnemies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam()
          .opponent());
    }
    return nearbyEnemies;
  }

  @Override
  public RobotInfo[] getNearbyZombies() {
    if (rc.getRoundNum() != lastUpdateNearbyZombies) {
      lastUpdateNearbyZombies = rc.getRoundNum();
      nearbyZombies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.ZOMBIE);
    }
    return nearbyZombies;
  }

  @Override
  public RobotInfo[] getNearbyHostiles() {
    if (rc.getRoundNum() != lastUpdateNearbyHostiles) {
      lastUpdateNearbyHostiles = rc.getRoundNum();
      nearbyHostiles = rc.senseHostileRobots(rc.getLocation(), rc.getType().sensorRadiusSquared);
    }
    return nearbyHostiles;
  }

  @Override
  public void showDebugInfo() {}
}
