package team389;

public interface BytecodeProfiler {

  public void start();

  public void split(String tag);

  public void end();

  public void printToConsole();

  public void printToIndicatorString(int index);
}
