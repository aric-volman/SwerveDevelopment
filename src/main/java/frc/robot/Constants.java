package frc.robot;

public final class Constants {

    public static class OperatorConstants {
        public static final int kDriverControllerPort = 0;
     }

    public static class SwerveConstants {
        public static final double driveGearRatio = 8.16; // For SDS module
        public static final double turnGearRatio = 12.8; // For SDS module

        public static final double wheelDiameterMeters = (3 / 12.0) / 3.281; // Assuming SDS module
        
        // These can be safely adjusted without adjusting discrete
        public static final double maxChassisTranslationalSpeed = 12.0 / 3.281; // Assuming L1 swerve
        public static final double maxLinearVelocityMeters = 12.0 / 3.281; // Assuming L1 swerve

        public static final double trackWidthX = 0.6985; // 27.5 inch
        public static final double trackWidthY = 0.6985; // 27.5 inch
        public static final double hypotenuse = Math.sqrt(Math.pow(trackWidthX / 2.0, 2) + Math.pow(trackWidthY / 2.0, 2));

        public static final double maxChassisAngularVelocity = Math.PI * 2.0 * 1.25; // A decent number but not fast enough

        public static final double deadBand = 0.1;
    }

}