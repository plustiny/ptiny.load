package ptiny.load;

import java.util.Arrays;
import java.util.ArrayList;

public class DebugLevel {
    public String label;
    public int level;

    public static ArrayList<DebugLevel> debugLevels() {
        return new ArrayList<>(Arrays.asList(
                    new DebugLevel(0, "OFF"),
                    new DebugLevel(1, "BASIC"),
                    new DebugLevel(2, "VERBOSE")
                    ));
    }
   
    public DebugLevel(int level, String label){
        this.level = level;
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
