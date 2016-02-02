package team389;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class DefaultEnemyTurretCache implements EnemyTurretCache {

  private static final int MAX_TURRETS = 6;

  private static class TurretInfo {
    private final MapLocation loc;
    private final boolean present;
    private final int timestamp;

    private TurretInfo(MapLocation loc, boolean present, int timestamp) {
      this.loc = loc;
      this.present = present;
      this.timestamp = timestamp;
    }

    public TurretInfo asPresent(int newTimestamp) {
      if (present) {
        return this;
      }

      return new TurretInfo(loc, true /* present */, newTimestamp);
    }

    public TurretInfo asAbsent(int newTimestamp) {
      if (!present) {
        return this;
      }

      return new TurretInfo(loc, false /* present */, newTimestamp);
    }
  }

  private final RobotController rc;
  private final TurretInfo[] turrets;
  private int numTurrets;
  private int shareIndex;
  private int checkDist;

  public DefaultEnemyTurretCache(RobotController rc) {
    this.rc = rc;
    turrets = new TurretInfo[MAX_TURRETS + 1];
    checkDist = rc.getType() == RobotType.SCOUT ? RobotType.TURRET.attackRadiusSquared : 55;
  }

  @Override
  public void reportEnemyTurretPresent(MapLocation loc, int timestamp) {
    for (int i = numTurrets; --i >= 0;) {
      TurretInfo turret = turrets[i];
      if (turret.loc.equals(loc)) {
        if (turret.timestamp < timestamp && !turret.present) {
          turrets[i] = turret.asPresent(timestamp);
        }
        return;
      }
    }

    if (numTurrets < turrets.length) {
      turrets[numTurrets++] = new TurretInfo(loc, true /* present */, timestamp);
    }
    maybeRemoveFurthestTurret();
  }

  @Override
  public void reportEnemyTurretAbsent(MapLocation loc, int timestamp) {
    for (int i = numTurrets; --i >= 0;) {
      TurretInfo turret = turrets[i];
      if (turret.loc.equals(loc)) {
        if (turret.timestamp < timestamp && turret.present) {
          turrets[i] = turret.asAbsent(timestamp);
        }
        return;
      }
    }

    if (numTurrets < turrets.length) {
      turrets[numTurrets++] = new TurretInfo(loc, false /* present */, timestamp);
    }
    maybeRemoveFurthestTurret();
  }

  private void maybeRemoveOldestTurret() {
    if (numTurrets > MAX_TURRETS) {
      int oldestTimestamp = 99999;
      int oldestIndex = -1;
      for (int i = numTurrets; --i >= 0;) {
        TurretInfo turret = turrets[i];
        if (oldestIndex == -1 || turret.timestamp < oldestTimestamp) {
          oldestTimestamp = turret.timestamp;
          oldestIndex = i;
        }
      }
      if (oldestIndex != numTurrets - 1) {
        turrets[oldestIndex] = turrets[numTurrets - 1];
      }
      numTurrets--;
    }
  }

  private void maybeRemoveFurthestTurret() {
    MapLocation myLoc = rc.getLocation();
    if (numTurrets > MAX_TURRETS) {
      int furthestDistance = 99999;
      int furthestIndex = -1;
      for (int i = numTurrets; --i >= 0;) {
        TurretInfo turret = turrets[i];
        int dist = myLoc.distanceSquaredTo(turret.loc);
        if (furthestIndex == -1 || turret.timestamp < furthestDistance) {
          furthestDistance = dist;
          furthestIndex = i;
        }
      }
      if (furthestIndex != numTurrets - 1) {
        turrets[furthestIndex] = turrets[numTurrets - 1];
      }
      numTurrets--;
    }
  }

  @Override
  public boolean isInEnemyTurretRange(MapLocation loc) {
    for (int i = numTurrets; --i >= 0;) {
      if (turrets[i].present
          && loc.distanceSquaredTo(turrets[i].loc) <= checkDist) {
        return true;
      }
    }

    return false;
  }

  @Override
  public void invalidateNearbyEnemyTurrets() throws GameActionException {
    int roundNum = rc.getRoundNum();
    Team enemyTeam = rc.getTeam().opponent();
    for (int i = numTurrets; --i >= 0;) {
      TurretInfo turret = turrets[i];
      if (turret.present && rc.canSenseLocation(turret.loc)) {
        RobotInfo robot = rc.senseRobotAtLocation(turret.loc);
        if (robot == null || robot.team != enemyTeam || robot.type != RobotType.TURRET) {
          turrets[i] = turret.asAbsent(roundNum /* newTimestamp */);
        }
      }
    }
  }

  @Override
  public MapLocation getNewestTurret() {
    TurretInfo oldest = null;
    for (int i = numTurrets; --i >= 0;) {
      TurretInfo turret = turrets[i];
      if (turret.present && (oldest == null || turret.timestamp > oldest.timestamp)) {
        oldest = turret;
      }
    }

    return oldest == null ? null : oldest.loc;
  }

  @Override
  public void shareRandomEnemyTurret(MessageSender messageSender) throws GameActionException {
    if (numTurrets == 0) {
      return;
    }

    shareIndex = shareIndex % numTurrets;
    TurretInfo turret = turrets[shareIndex];
    if (turrets[shareIndex].present) {
      messageSender.sendEnemyTurretLocation(turret.loc, turret.timestamp);
    } else {
      messageSender.sendEnemyTurretLocationRemoved(turret.loc, turret.timestamp);
    }
    shareIndex++;
  }

  @Override
  public void showDebugInfo() {
    for (int i = numTurrets; --i >= 0;) {
      TurretInfo turret = turrets[i];
      if (turret.present) {
        rc.setIndicatorLine(rc.getLocation(), turret.loc, 50, 50, 50);
      }
    }
  }
}
