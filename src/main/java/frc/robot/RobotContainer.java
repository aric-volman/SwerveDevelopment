/* Decompiler 11ms, total 557ms, lines 40 */
package frc.robot;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.commands.SwerveTeleop;
import frc.robot.subsystems.swerve.SwerveDrive;
import frc.robot.subsystems.swerve.SwerveModuleIO;
import frc.robot.subsystems.swerve.SwerveModuleIOSim;

public class RobotContainer {
   private final Joystick driver = new Joystick(0);
   private final Joystick d2 = new Joystick(1);
   private final int translationAxis = 0;
   private final int strafeAxis = 1;
   private final int rotationAxis = 0;
   SwerveModuleIO[] swerveMods = new SwerveModuleIOSim[]{new SwerveModuleIOSim(0), new SwerveModuleIOSim(1), new SwerveModuleIOSim(2), new SwerveModuleIOSim(3)};
   SwerveDrive swerve;

   public RobotContainer() {
      this.swerve = new SwerveDrive(this.swerveMods[0], this.swerveMods[1], this.swerveMods[2], this.swerveMods[3]);
      this.swerve.setDefaultCommand(new SwerveTeleop(this.swerve, () -> {
         return -this.driver.getRawAxis(0);
      }, () -> {
         return -this.driver.getRawAxis(1);
      }, () -> {
         return -this.d2.getRawAxis(0);
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
}