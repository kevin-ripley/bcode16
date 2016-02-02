package team389;

import battlecode.common.RobotController;
import battlecode.common.ZombieSpawnSchedule;

public class ZombieSpawnScheduleInfo {

  private RobotController rc;
  private int currentIndex;
  private int[] rounds;

  public ZombieSpawnScheduleInfo(RobotController rc, ZombieSpawnSchedule schedule) {
    this.rc = rc;
    initialize(schedule);
  }

  private void initialize(ZombieSpawnSchedule schedule) {
    currentIndex = 0;
    rounds = schedule.getRounds();
    while (currentIndex < rounds.length && rc.getRoundNum() > rounds[currentIndex]) {
      currentIndex++;
    }
  }

  public int getNextZombieRound() {
    while (currentIndex < rounds.length && rc.getRoundNum() > rounds[currentIndex]) {
      currentIndex++;
    }
    if (currentIndex >= rounds.length) {
      return 10000000;
    }
    return rounds[currentIndex];
  }

  public boolean hasNextZombieRound() {
    while (currentIndex < rounds.length && rc.getRoundNum() > rounds[currentIndex]) {
      currentIndex++;
    }
    return currentIndex < rounds.length;
  }
}
