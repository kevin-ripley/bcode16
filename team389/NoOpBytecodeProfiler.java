package team389;

public class NoOpBytecodeProfiler implements BytecodeProfiler {

  @Override
  public void start() {}

  @Override
  public void split(String tag) {}

  @Override
  public void end() {}

  @Override
  public void printToConsole() {}

  @Override
  public void printToIndicatorString(int index) {}
}
