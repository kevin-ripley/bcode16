package team389;

import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

public interface RepairSystem {

  MapLocation getBestAllyToHeal(RobotInfo[] allies);
}
