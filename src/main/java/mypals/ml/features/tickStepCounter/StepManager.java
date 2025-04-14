package mypals.ml.features.tickStepCounter;

public class StepManager {
    public static int stepped = 0;

    public static int getStepped() {
        return stepped;
    }
    public static void reset(){
        stepped = 0;
    }

    public static void setStepped(int stepped) {
        StepManager.stepped = stepped;
    }
    public static void step(){
        stepped++;
    }
    public static void step(int s){
        stepped = stepped + s;
    }
}
