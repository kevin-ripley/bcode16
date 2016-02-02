package team389;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class DefaultRepairSystem implements RepairSystem {

  private RobotController rc;

  public DefaultRepairSystem(RobotController rc) {
    this.rc = rc;
  }

  @Override
  public MapLocation getBestAllyToHeal(RobotInfo[] allies) {
    double leastHealthLessThanMax = 99999;
    RobotInfo bestAlly = null;
    for (int i = allies.length; --i >= 0;) {
      if (allies[i].type == RobotType.ARCHON || allies[i].location.distanceSquaredTo(rc
          .getLocation()) > RobotType.ARCHON.attackRadiusSquared) {
        continue;
      }
      double diff = allies[i].maxHealth - allies[i].health;
      if (diff > 0 && diff < leastHealthLessThanMax) {
        leastHealthLessThanMax = diff;
        bestAlly = allies[i];
      }
    }
    if (bestAlly != null) {
      return bestAlly.location;
    }
    return null;
  }
}
