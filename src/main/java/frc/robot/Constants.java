package frc.robot;

public final class Constants {

    public class OperatorConstants {
        public static final int kDriverControllerPort = 0;
     }

    public class SwerveConstants {
        public static final double driveGearRatio = 8.16;
        public static final double turnGearRatio = 12.8;

        public static final double wheelDiameterMeters = 1.0;
        public static final double maxLinearVelocityMeters = 1.0;

        public static final double trackWidthX = 1.0;
        public static final double trackWidthY = 1.0;

        public static final double maxTranslationalSpeed = 14.0 / 3.281;
        public static final double maxAngularVelocity = Math.PI * 2.0 * 1.25;
        public static final double deadBand = 0.0;
    }

    public class SimConstants {

        public static final double kvVoltSecondsPerMeter = 0.16;
        public static final double kaVoltSecondsSquaredPerMeter = 0.0348;

        public static final double kvVoltSecondsPerRadian = 2.0;
        public static final double kaVoltSecondsSquaredPerRadian = 1.24;
    }


}