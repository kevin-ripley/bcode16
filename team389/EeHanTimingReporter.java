package team389;

import battlecode.common.MapLocation;

public interface EeHanTimingReporter {

  public class TimingAttack {

    public final MapLocation location;
    public final int startRound;
    public final int endRound;

    public TimingAttack(MapLocation location, int startRound, int endRound) {
      this.location = location;
      this.startRound = startRound;
      this.endRound = endRound;
    }
  }

  public void reportTimingAttack(MapLocation location, int startRound, int endRound);

  public TimingAttack getTimingAttack();
}
