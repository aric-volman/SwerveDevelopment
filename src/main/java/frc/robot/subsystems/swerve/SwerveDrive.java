package frc.robot.subsystems.swerve;

import java.util.ArrayList;
import java.util.stream.Stream;

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
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.SPI.Port;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.RobotContainer;

public class SwerveDrive extends SubsystemBase {
   // Create Navx
   private static AHRS navx = new AHRS(Port.kMXP);

   // Create object representing swerve modules
   private SwerveModuleIO[] moduleIO;

   // Create object that represents swerve module positions (i.e. radians and meters)
   private SwerveModulePosition[] modulePositions = new SwerveModulePosition[4];

   // Create kinematics object
   private SwerveDriveKinematics kinematics;

   // Create poseEstimator object
   private SwerveDrivePoseEstimator poseEstimator;

   // Create swerveOdometry object
   private SwerveDriveOdometry swerveOdometry;

   // Add field to show robot
   private Field2d field;

   // Integrate angular velocity of chassis in simulation
   // In order to find the rotation of the chassis
   private double integratedSimAngle;

   /**
    * Creates a new SwerveDrive
    * @param FL Swerve module
    * @param FR Swerve module
    * @param BL Swerve module
    * @param BR Swerve module
    */
   public SwerveDrive(SwerveModuleIO FL, SwerveModuleIO FR, SwerveModuleIO BL, SwerveModuleIO BR) {
      // Assign modules to their object
      this.moduleIO = new SwerveModuleIO[]{FL, FR, BL, BR};

      // Iterate through module positions and assign 0 values
      for(int i = 0; i < 4; ++i) {
         this.modulePositions[i] = new SwerveModulePosition(0.0, Rotation2d.fromRadians(0.0));
      }

      // Initialize all other objects
      this.kinematics = new SwerveDriveKinematics(this.getModuleTranslations());
      this.poseEstimator = new SwerveDrivePoseEstimator(this.kinematics, new Rotation2d(), this.modulePositions, new Pose2d());
      this.swerveOdometry = new SwerveDriveOdometry(this.kinematics, this.getRotation(), this.modulePositions);
      this.field = new Field2d();
   }

   public void periodic() {
      // Update module positions
      this.modulePositions = new SwerveModulePosition[]{this.moduleIO[0].getPosition(), this.moduleIO[1].getPosition(), this.moduleIO[2].getPosition(), this.moduleIO[3].getPosition()};
      
      // Update odometry, field, and poseEstimator
      this.swerveOdometry.update(this.getRotation(), this.modulePositions);
      this.poseEstimator.update(this.getRotation(), this.modulePositions);
      this.field.setRobotPose(this.getPose());

      var translations = getModuleTranslations();
      for(int i = 0; i < 4; ++i) {
         Rotation2d moduleRot = Rotation2d.fromRadians(Math.PI*2).minus(modulePositions[i].angle);
         Rotation2d relRot = moduleRot.plus(getPose().getRotation());
         field.getObject("Module"+i).setPose(getPose().getX()+translations[i].getX()*Math.sqrt(Constants.SwerveConstants.trackWidthX), getPose().getY()+translations[i].getY()*Math.sqrt(Constants.SwerveConstants.trackWidthY), relRot);
      }
      // Put field on SmartDashboard
      SmartDashboard.putData("Field", this.field);

      SmartDashboard.putNumberArray("States", getDoubleStates());
      SmartDashboard.putNumber("Robot Rotation", getPose().getRotation().getRadians());
   }

   public void simulationPeriodic() {
      // Simulate Navx
      int dev = SimDeviceDataJNI.getSimDeviceHandle("navX-Sensor[0]");
      SimDouble angle = new SimDouble(SimDeviceDataJNI.getSimValueHandle(dev, "Yaw"));

      // Find omega/angular velocity of chassis' rotation
      double omega = this.kinematics.toChassisSpeeds(this.getStates()).omegaRadiansPerSecond;

      // Integrate dAngle into angular displacement
      this.integratedSimAngle += 0.02 * omega * (180/Math.PI);

      // Set this as gyro measurement
      angle.set(this.integratedSimAngle);

      // Update moduleIO's sim objects with a dt of 0.02
      for(int i = 0; i < 4; ++i) {
         this.moduleIO[i].updateSim();
      }

   }

   /**
    * Drive either field oriented, or not field oriented
    * @param translation Vector of x-y velocity in m/s
    * @param rotation Rotation psuedovector in rad/s
    * @param fieldRelative Whether or not the robot should drive field relative
    * @param isOpenLoop Whether or not to control robot with closed or open loop control
    */
   public void drive(Translation2d translation, double rotation, boolean fieldRelative, boolean isOpenLoop) {

      ChassisSpeeds speeds = fieldRelative ? 
      ChassisSpeeds.fromFieldRelativeSpeeds(translation.getX(), translation.getY(), rotation, this.getRotation()) : 
      new ChassisSpeeds(translation.getX(), translation.getY(), rotation);

      speeds = discretize(speeds);

      SmartDashboard.putNumber("MagnitudeVel", Math.sqrt(Math.pow(speeds.vxMetersPerSecond, 2) + Math.pow(speeds.vyMetersPerSecond, 2)));

      SwerveModuleState[] swerveModuleStates = this.kinematics.toSwerveModuleStates(speeds);

      // MUST USE SECOND TYPE OF METHOD
      SwerveDriveKinematics.desaturateWheelSpeeds(swerveModuleStates, speeds, Constants.SwerveConstants.maxLinearVelocityMeters, Constants.SwerveConstants.maxChassisTranslationalSpeed, Constants.SwerveConstants.maxChassisAngularVelocity);

      for(int i = 0; i < 4; ++i) {
         this.moduleIO[i].setModuleState(swerveModuleStates[i], isOpenLoop);
      }

   }

   /**
    * Get heading of Navx. Negative because Navx is CW positive.
    */
   public double getHeading() {
      return -navx.getRotation2d().getDegrees();
   }

   /**
    * Get rate of rotation of Navx. Negative because Navx is CW positive.
    */
   public double getTurnRate() {
      return -navx.getRate();
   }

   /**
    * Get Rotation2d of Navx. Positive value (CCW positive default).
    */
   public Rotation2d getRotation() {
      return navx.getRotation2d();
   }

   /**
    * Get Pose2d of poseEstimator.
    */
   public Pose2d getPose() {
      return poseEstimator.getEstimatedPosition(); 
   }

   /** 
    * Reset pose of robot to pose
    */
   public void resetPose(Pose2d pose) {
      poseEstimator.resetPosition(new Rotation2d(), modulePositions, pose);
   }

   /** 
    * Get chassis speeds for PathPlannerLib
    */
   public ChassisSpeeds getRobotRelativeSpeeds() {
      return ChassisSpeeds.fromFieldRelativeSpeeds(kinematics.toChassisSpeeds(getStates()), getRotation());
   }

   /** 
    * Drive the robot for PathPlannerLib
    */
   public void driveRobotRelative(ChassisSpeeds speeds) {
      // speeds = discretize(speeds);

      SmartDashboard.putNumber("MagnitudeVel", Math.sqrt(Math.pow(speeds.vxMetersPerSecond, 2) + Math.pow(speeds.vyMetersPerSecond, 2)));

      SwerveModuleState[] swerveModuleStates = this.kinematics.toSwerveModuleStates(speeds);

      // MUST USE SECOND TYPE OF METHOD
      SwerveDriveKinematics.desaturateWheelSpeeds(swerveModuleStates, speeds,  Units.feetToMeters(12), Constants.SwerveConstants.maxChassisTranslationalSpeed, Constants.SwerveConstants.maxChassisAngularVelocity);

      for(int i = 0; i < 4; ++i) {
         this.moduleIO[i].setModuleState(swerveModuleStates[i], false);
      }
   }

   /**
    * Gets the SwerveModuleState[] for our use in code.
    */
   public SwerveModuleState[] getStates() {
      SwerveModuleState[] states = new SwerveModuleState[4];

      for(int i = 0; i < 4; ++i) {
         states[i] = this.moduleIO[i].getModuleState();
      }

      return states;
   }


   /** 
    * Gets module states as double[] for AdvantageScope compatibility
    */
   public double[] getDoubleStates() {
      SwerveModuleState[] states = getStates();
      ArrayList<Double> ret = new ArrayList<Double>();

      for(int i = 0; i < 4; ++i) {
         double num = 0;
         num = states[i].angle.getRadians();
         ret.add(num);
         num = states[i].speedMetersPerSecond;
         ret.add(num);
      }

      Double[] actual = new Double[8];
      ret.toArray(actual);


      return Stream.of(actual).mapToDouble(Double::doubleValue).toArray();

   }

   /**
    * Get physical positions of wheels on Swerve chassis (half of trackwidth)
    */
   public Translation2d[] getModuleTranslations() {
      return new Translation2d[]{new Translation2d(Constants.SwerveConstants.trackWidthX / 2.0, Constants.SwerveConstants.trackWidthY / 2.0), 
         new Translation2d(Constants.SwerveConstants.trackWidthX / 2.0, -Constants.SwerveConstants.trackWidthY / 2.0), 
         new Translation2d(-Constants.SwerveConstants.trackWidthX / 2.0, Constants.SwerveConstants.trackWidthY / 2.0), 
         new Translation2d(-Constants.SwerveConstants.trackWidthX / 2.0, -Constants.SwerveConstants.trackWidthY / 2.0)};
   }

   /** Credit: WPIlib 2024 + Patribots (Author: Alexander Hamilton)
   * Discretizes a continuous-time chassis speed.
   *
   * @param vx    Forward velocity.
   * @param vy    Sideways velocity.
   * @param omega Angular velocity.
   */
  public ChassisSpeeds discretize(ChassisSpeeds speeds) {
      if (!RobotContainer.getSimOrNot()) {
         return speeds;
      }
      
      double dt = 0.02;
      
      var desiredDeltaPose = new Pose2d(
         speeds.vxMetersPerSecond * dt, 
         speeds.vyMetersPerSecond * dt, 
         new Rotation2d(speeds.omegaRadiansPerSecond * dt * -2.2)
      );

      var twist = new Pose2d().log(desiredDeltaPose);

      return new ChassisSpeeds((twist.dx / dt), (twist.dy / dt), (speeds.omegaRadiansPerSecond));
   }


}