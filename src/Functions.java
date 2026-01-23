public class Functions {
    public static double applyFunction(int function, double ratio) {
        
        switch (function) {
            case 1 -> {
                return ratio;
            }
            case 2 -> {
                return Math.sqrt(ratio);
            }
            case 3 -> {
                
                return (3 * Math.pow(ratio, 2)) - (2 * (Math.pow(ratio, 3)));
            }
            case 4 -> {
                return Math.pow(ratio, 4);
            }
            case 5 -> {
                return (3 * Math.pow(ratio, 6)) - (2 * (Math.pow(ratio, 9)));
            }
        }
        
        return 0.0;
    }
}
