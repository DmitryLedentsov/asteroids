package net.logwrapper;
public interface Logger {
    public void trace(String message);
    public void info(String message);
    public void warn(String message);
    public void error(String message);
}
