/* Decompiler 21ms, total 565ms, lines 108 */
package frc.robot.subsystems.swerve;

import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.hal.SimDouble;
import edu.wpi.first.hal.simulation.SimDeviceDataJNI;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.SPI.Port;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class SwerveDrive extends SubsystemBase {
   private static AHRS navx;
   private SwerveModuleIO[] moduleIO;
   private SwerveModulePosition[] modulePositions = new SwerveModulePosition[4];
   private SwerveDriveKinematics kinematics;
   private SwerveDrivePoseEstimator poseEstimator;
   private SwerveDriveOdometry swerveOdometry;
   private Field2d field;
   private double integratedSimAngle;

   public SwerveDrive(SwerveModuleIO FL, SwerveModuleIO FR, SwerveModuleIO BL, SwerveModuleIO BR) {
      this.moduleIO = new SwerveModuleIO[]{FL, FR, BL, BR};

      for(int i = 0; i < 4; ++i) {
         this.modulePositions[i] = new SwerveModulePosition(0.0D, Rotation2d.fromRadians(0.0D));
      }

      this.kinematics = new SwerveDriveKinematics(this.getModuleTranslations());
      this.poseEstimator = new SwerveDrivePoseEstimator(this.kinematics, new Rotation2d(), this.modulePositions, new Pose2d());
      this.swerveOdometry = new SwerveDriveOdometry(this.kinematics, this.getRotation(), this.modulePositions);
      this.field = new Field2d();
   }

   public void periodic() {
      this.modulePositions = new SwerveModulePosition[]{this.moduleIO[0].getPosition(), this.moduleIO[1].getPosition(), this.moduleIO[2].getPosition(), this.moduleIO[3].getPosition()};
      this.swerveOdometry.update(this.getRotation(), this.modulePositions);
      this.poseEstimator.update(this.getRotation(), this.modulePositions);
      this.field.setRobotPose(this.getPose());
      SmartDashboard.putData("Field", this.field);
   }

   public void simulationPeriodic() {
      int dev = SimDeviceDataJNI.getSimDeviceHandle("navX-Sensor[0]");
      SimDouble angle = new SimDouble(SimDeviceDataJNI.getSimValueHandle(dev, "Yaw"));
      double omega = this.kinematics.toChassisSpeeds(this.getStates()).omegaRadiansPerSecond;
      this.integratedSimAngle += 0.02D * omega * 57.29577951308232D;
      angle.set(this.integratedSimAngle);

      for(int i = 0; i < 4; ++i) {
         this.moduleIO[i].updateSim();
      }

   }

   public void drive(Translation2d translation, double rotation, boolean fieldRelative, boolean isOpenLoop) {
      SwerveModuleState[] swerveModuleStates = this.kinematics.toSwerveModuleStates(fieldRelative ? ChassisSpeeds.fromFieldRelativeSpeeds(translation.getX(), translation.getY(), rotation, this.getRotation()) : new ChassisSpeeds(translation.getX(), translation.getY(), rotation));
      SwerveDriveKinematics.desaturateWheelSpeeds(swerveModuleStates, 4.0D);

      for(int i = 0; i < 4; ++i) {
         this.moduleIO[i].setModuleState(swerveModuleStates[i], isOpenLoop);
      }

   }

   public double getHeading() {
      return -navx.getRotation2d().getDegrees();
   }

   public double getTurnRate() {
      return -navx.getRate();
   }

   public Rotation2d getRotation() {
      return navx.getRotation2d();
   }

   public Pose2d getPose() {
      return this.poseEstimator.getEstimatedPosition();
   }

   public SwerveModuleState[] getStates() {
      SwerveModuleState[] states = new SwerveModuleState[4];

      for(int i = 0; i < 4; ++i) {
         states[i] = this.moduleIO[i].getModuleState();
      }

      return states;
   }

   public Translation2d[] getModuleTranslations() {
      return new Translation2d[]{new Translation2d(0.5D, 0.5D), new Translation2d(0.5D, -0.5D), new Translation2d(-0.5D, 0.5D), new Translation2d(-0.5D, -0.5D)};
   }

   static {
      navx = new AHRS(Port.kMXP);
   }
}