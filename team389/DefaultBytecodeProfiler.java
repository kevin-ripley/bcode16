package team389;

import battlecode.common.Clock;
import battlecode.common.RobotController;

/**
 * Used to track bytecode usage
 * 
 * Example usage:
 * 
 * BytecodeProfiler p = new DefaultBytecodeProfiler(rc);
 * p.reset();
 * p.split("updateRadar");
 * p.split("processMessages");
 * p.printToConsole();
 * p.printToIndicatorString(2);
 * 
 * @author ryancheu
 */
public class DefaultBytecodeProfiler implements BytecodeProfiler {

  private static final int MAX_SPLITS = 100;

  private final RobotController rc;

  private int startRoundNum;
  private int startByte;
  private int currentRoundNum;
  private int currentByte;
  private String[] tags;
  private int[] splits;
  private int numSplits;

  public DefaultBytecodeProfiler(RobotController rc) {
    this.rc = rc;
  }

  @Override
  public void start() {
    startRoundNum = rc.getRoundNum();
    startByte = Clock.getBytecodeNum();
    currentRoundNum = startRoundNum;
    currentByte = startByte;
    tags = new String[MAX_SPLITS];
    splits = new int[MAX_SPLITS];
    numSplits = 0;
  }

  @Override
  public void split(String tag) {
    if (numSplits >= tags.length) {
      return;
    }
    int newCurrentRoundNum = rc.getRoundNum();
    int newCurrentByte = Clock.getBytecodeNum();
    int split = rc.getType().bytecodeLimit * (newCurrentRoundNum - currentRoundNum)
        + (newCurrentByte - currentByte);
    currentRoundNum = newCurrentRoundNum;
    currentByte = newCurrentByte;

    tags[numSplits] = tag;
    splits[numSplits] = split;
    numSplits++;
  }

  @Override
  public void end() {
    split("end");
  }

  @Override
  public void printToConsole() {
    StringBuffer sb = new StringBuffer();
    sb.append("\n+++\n");
    sb.append("ROBOT ID = " + rc.getID() + ", START ROUND = " + startRoundNum + "\n");
    addSplitsToBuffer(sb, "\n" /* delimiter */);
    sb.append("END ROUND = " + rc.getRoundNum() + "\n");
    sb.append("+++");

    System.out.println(sb.toString());
  }

  @Override
  public void printToIndicatorString(int index) {
    StringBuffer sb = new StringBuffer();
    addSplitsToBuffer(sb, "   " /* delimiter */);
    rc.setIndicatorString(index, sb.toString());
  }

  private void addSplitsToBuffer(StringBuffer sb, String delimiter) {
    sb.append("start: " + startByte + delimiter);
    for (int i = 0; i < numSplits; i++) {
      sb.append(tags[i] + ": " + splits[i] + delimiter);
    }
  }
}
