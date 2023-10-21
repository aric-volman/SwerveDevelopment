package frc.robot;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.commands.SwerveTeleop;
import frc.robot.subsystems.swerve.SwerveDrive;
import frc.robot.subsystems.swerve.SwerveModuleIO;
import frc.robot.subsystems.swerve.SwerveModuleIOSim;

public class RobotContainer {

   // Xbox + an additional one for PC use
   private final Joystick driver = new Joystick(0);
   private final Joystick dSim = new Joystick(1);

   // Define axises for using joystick
   private final int translationAxis = 0;
   private final int strafeAxis = 1;
   // private final int rotationAxis = 4; // For xBox

   private final int rotationAxis = 0;
   private static boolean isSim = Robot.isSimulation();

   // Creates array of swerve modules for use in SwerveDrive object
   SwerveModuleIO[] swerveMods = new SwerveModuleIOSim[]{
      new SwerveModuleIOSim(0), 
      new SwerveModuleIOSim(1), 
      new SwerveModuleIOSim(2), 
      new SwerveModuleIOSim(3)};
   
   // Empty SwerveDrive object
   SwerveDrive swerve;

   public RobotContainer() {
      // Initialize SwerveDrive object with modules
      this.swerve = new SwerveDrive(this.swerveMods[0], this.swerveMods[1], this.swerveMods[2], this.swerveMods[3]);

      // Supply teleop command with joystick methods
      this.swerve.setDefaultCommand(new SwerveTeleop(this.swerve, () -> {
         return -this.driver.getRawAxis(translationAxis);
      }, () -> {
         return -this.driver.getRawAxis(strafeAxis);
      }, () -> {
         return -this.dSim.getRawAxis(rotationAxis);
      }, () -> {
         return true;
      }));
      this.configureBindings();
   }

   private void configureBindings() {
   }

   public Command getAutonomousCommand() {
      return null;
   }

   /**
    * Gets Robot.isReal() from RobotContainer (slow when calling every loop)
    * @return If simulated or not
    */
   public static boolean getSimOrNot() {
      return isSim;
   }
}