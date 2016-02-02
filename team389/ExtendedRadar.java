package team389;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class ExtendedRadar implements Radar, DistantHostileReporter {

  private static final int MAX_HOSTILES = 4000;

  private class DistantHostiles {
    private final Team team;
    private final MapLocation[] locs;
    private final int[] coreDelayTenths;
    private final int[] timestamps;
    private final MapLocationSet locSet;
    private int numHostiles;
    private int lastCleanRound;

    public DistantHostiles(Team team) {
      this.team = team;
      this.locs = new MapLocation[MAX_HOSTILES];
      this.coreDelayTenths = new int[MAX_HOSTILES];
      this.timestamps = new int[MAX_HOSTILES];
      locSet = new ArrayMapLocationIntMap();
      numHostiles = 0;
      lastCleanRound = -1;
    }

    private RobotInfo[] asRobotInfos() {
      clean();
      RobotInfo[] infos = new RobotInfo[numHostiles];
      for (int i = numHostiles; --i >= 0;) {
        infos[i] = new RobotInfo(
            0 /* id */,
            team,
            RobotType.SOLDIER /* type */,
            locs[i],
            coreDelayTenths[i] / 10.0 /* coreDelay */,
            0 /* weaponDelay */,
            RobotType.SOLDIER.attackPower /* attackPower */,
            RobotType.SOLDIER.maxHealth /* health */,
            RobotType.SOLDIER.maxHealth /* maxHealth */,
            0 /* zombieInfectedTurns */,
            0 /* viperInfectedTurns */);
      }
      return infos;
    }

    public void add(MapLocation loc, int coreDelay) {
      for (int i = numHostiles; --i >= 0;) {
        if (locs[i].equals(loc)) {
          return;
        }
      }

      locs[numHostiles] = loc;
      coreDelayTenths[numHostiles] = coreDelay;
      timestamps[numHostiles] = rc.getRoundNum();
      numHostiles++;
    }

    private void clean() {
      int currentRound = rc.getRoundNum();
      if (lastCleanRound >= currentRound) {
        return;
      }

      lastCleanRound = currentRound;
      if (numHostiles == 0) {
        return;
      }
      for (int i = numHostiles; --i >= 0;) {
        if (10 * timestamps[i] + coreDelayTenths[i] < 10 * currentRound) {
          locSet.remove(locs[i]);
          if (i != numHostiles - 1) {
            locs[i] = locs[numHostiles - 1];
            coreDelayTenths[i] = coreDelayTenths[numHostiles - 1];
            timestamps[i] = timestamps[numHostiles - 1];
          }
          numHostiles--;
        }
      }
    }

    private void showDebugInfoWithColor(int r, int g, int b) {
      for (int i = numHostiles; --i >= 0;) {
        rc.setIndicatorLine(rc.getLocation(), locs[i], r, g, b);
      }
    }
  }

  private final RobotController rc;
  private final Radar senseRadar;
  private final DistantHostiles distantEnemies;
  private final DistantHostiles distantZombies;

  public ExtendedRadar(RobotController rc, Radar senseRadar) {
    this.rc = rc;
    this.senseRadar = senseRadar;
    distantEnemies = new DistantHostiles(rc.getTeam().opponent());
    distantZombies = new DistantHostiles(Team.ZOMBIE);
  }

  @Override
  public void reportDistantHostile(
      MapLocation loc, int coreDelayTenths, boolean isZombie) throws GameActionException {
    DistantHostiles distantHostiles = isZombie ? distantZombies : distantEnemies;
    distantHostiles.add(loc, coreDelayTenths);
  }

  @Override
  public RobotInfo[] getNearbyRobots() {
    return senseRadar.getNearbyRobots();
  }

  @Override
  public RobotInfo[] getNearbyAllies() {
    return senseRadar.getNearbyAllies();
  }

  @Override
  public RobotInfo[] getNearbyEnemies() {
    RobotInfo[] senseEnemies = senseRadar.getNearbyEnemies();
    RobotInfo[] distantEnemyInfos = distantEnemies.asRobotInfos();
    RobotInfo[] enemies = new RobotInfo[senseEnemies.length + distantEnemyInfos.length];
    for (int i = senseEnemies.length; --i >= 0;) {
      enemies[i] = senseEnemies[i];
    }
    for (int i = distantEnemyInfos.length; --i >= 0;) {
      enemies[senseEnemies.length + i] = distantEnemyInfos[i];
    }
    return enemies;
  }

  @Override
  public RobotInfo[] getNearbyZombies() {
    RobotInfo[] senseZombies = senseRadar.getNearbyZombies();
    RobotInfo[] distantZombieInfos = distantZombies.asRobotInfos();
    RobotInfo[] zombies = new RobotInfo[senseZombies.length + distantZombieInfos.length];
    for (int i = senseZombies.length; --i >= 0;) {
      zombies[i] = senseZombies[i];
    }
    for (int i = distantZombieInfos.length; --i >= 0;) {
      zombies[senseZombies.length + i] = distantZombieInfos[i];
    }
    return zombies;
  }

  @Override
  public RobotInfo[] getNearbyHostiles() {
    RobotInfo[] senseHostiles = senseRadar.getNearbyHostiles();
    RobotInfo[] distantEnemyInfos = distantEnemies.asRobotInfos();
    RobotInfo[] distantZombieInfos = distantZombies.asRobotInfos();
    RobotInfo[] hostiles = new RobotInfo[senseHostiles.length + distantEnemyInfos.length
        + distantZombieInfos.length];
    for (int i = senseHostiles.length; --i >= 0;) {
      hostiles[i] = senseHostiles[i];
    }
    for (int i = distantEnemyInfos.length; --i >= 0;) {
      hostiles[senseHostiles.length + i] = distantEnemyInfos[i];
    }
    for (int i = distantZombieInfos.length; --i >= 0;) {
      hostiles[senseHostiles.length + distantEnemyInfos.length + i] = distantZombieInfos[i];
    }
    return hostiles;
  }

  @Override
  public void showDebugInfo() {
    distantEnemies.showDebugInfoWithColor(0, 0, 255);
    distantZombies.showDebugInfoWithColor(0, 255, 0);
  }
}
