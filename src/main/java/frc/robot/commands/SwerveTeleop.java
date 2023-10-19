/* Decompiler 5ms, total 891ms, lines 38 */
package frc.robot.commands;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.Subsystem;
import frc.robot.subsystems.swerve.SwerveDrive;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

public class SwerveTeleop extends CommandBase {
   private SwerveDrive swerve;
   private DoubleSupplier translationSup;
   private DoubleSupplier strafeSup;
   private DoubleSupplier rotationSup;
   private BooleanSupplier robotCentricSup;
   private SlewRateLimiter translationLimiter = new SlewRateLimiter(3.0D);
   private SlewRateLimiter strafeLimiter = new SlewRateLimiter(3.0D);
   private SlewRateLimiter rotationLimiter = new SlewRateLimiter(3.0D);

   public SwerveTeleop(SwerveDrive s_Swerve, DoubleSupplier translationSup, DoubleSupplier strafeSup, DoubleSupplier rotationSup, BooleanSupplier robotCentricSup) {
      this.swerve = s_Swerve;
      this.addRequirements(new Subsystem[]{s_Swerve});
      this.translationSup = translationSup;
      this.strafeSup = strafeSup;
      this.rotationSup = rotationSup;
      this.robotCentricSup = robotCentricSup;
   }

   public void execute() {
      double translationVal = this.translationLimiter.calculate(MathUtil.applyDeadband(this.translationSup.getAsDouble(), 0.0D));
      double strafeVal = this.strafeLimiter.calculate(MathUtil.applyDeadband(this.strafeSup.getAsDouble(), 0.0D));
      double rotationVal = this.rotationLimiter.calculate(MathUtil.applyDeadband(this.rotationSup.getAsDouble(), 0.0D));
      this.swerve.drive((new Translation2d(translationVal, strafeVal)).times(4.0D), rotationVal * 12.566370614359172D, this.robotCentricSup.getAsBoolean(), false);
   }
}