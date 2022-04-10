package edu.neu.madcourse.metu;

public class Utils {
    public static int calculateFriendLevel(int connectionPoint) {
        if (connectionPoint == 0) {
            return 0;
        } else if (connectionPoint > 0 && connectionPoint < 10) {
            return 1;
        } else if (connectionPoint >= 10 && connectionPoint < 40) {
            return 2;
        } else {
            return 3;
        }
    }
}
