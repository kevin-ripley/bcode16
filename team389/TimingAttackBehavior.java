package team389;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import team389.EeHanTimingReporter.TimingAttack;

public class TimingAttackBehavior implements Behavior {

  private final RobotController rc;
  private final EeHanTimingReporter eeHanTimingReporter;
  private final NavigationSystem navigation;

  public TimingAttackBehavior(
      RobotController rc,
      EeHanTimingReporter eeHanTimingReporter,
      NavigationSystem navigation) {
    this.rc = rc;
    this.eeHanTimingReporter = eeHanTimingReporter;
    this.navigation = navigation;
  }

  @Override
  public void run() throws GameActionException {
    TimingAttack timingAttack = eeHanTimingReporter.getTimingAttack();
    if (timingAttack != null) {
      rc.setIndicatorString(0, "I'm executing a timing attack at " + timingAttack.location
          + " from rounds " + timingAttack.startRound + " to " + timingAttack.endRound + ".");
      int roundNum = rc.getRoundNum();
      boolean avoidAttackers = roundNum < timingAttack.startRound;
      boolean clearRubble = rc.getType() != RobotType.TTM && rc.getType() != RobotType.SCOUT;
      navigation.directTo(timingAttack.location, avoidAttackers, clearRubble);
    } else {
      rc.setIndicatorString(0, "I'm lost during a timing attack.");
      navigation.moveRandomly();
    }
  }
}
