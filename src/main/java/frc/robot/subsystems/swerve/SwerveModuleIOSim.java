/* Decompiler 9ms, total 1660ms, lines 73 */
package frc.robot.subsystems.swerve;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.simulation.DCMotorSim;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class SwerveModuleIOSim implements SwerveModuleIO {
   private final DCMotorSim driveSim = new DCMotorSim(DCMotor.getNEO(1), 1.0D, 0.01D);
   private final DCMotorSim turnSim = new DCMotorSim(DCMotor.getNEO(1), 1.0D, 1.0E-6D);
   private final PIDController drivePID = new PIDController(0.5D, 0.0D, 0.0D);
   private final PIDController turnPID = new PIDController(0.5D, 0.0D, 0.01D);
   private double driveVolts = 0.0D;
   private double turnVolts = 0.0D;
   private SwerveModuleState state = new SwerveModuleState();
   private int num = 0;

   public SwerveModuleIOSim(int num) {
      this.num = num;
   }

   public void setDriveVoltage(double voltage) {
      this.driveVolts = MathUtil.clamp(voltage, -12.0D, 12.0D);
      this.driveSim.setInputVoltage(this.driveVolts);
   }

   public void setTurnVoltage(double voltage) {
      this.turnVolts = MathUtil.clamp(voltage, -12.0D, 12.0D);
      this.turnSim.setInputVoltage(this.turnVolts);
   }

   public void setModuleState(SwerveModuleState state, boolean isOpenLoop) {
      state = SwerveModuleState.optimize(state, new Rotation2d(this.turnSim.getAngularPositionRad()));
      this.state = state;
      double percent;
      if (isOpenLoop) {
         percent = state.speedMetersPerSecond / 1.0D;
         SmartDashboard.putNumber("Open Loop Drive #" + this.num, percent);
         this.driveSim.setInputVoltage(percent * 12.0D);
      } else {
         this.drivePID.setSetpoint(state.speedMetersPerSecond);
         percent = this.driveSim.getAngularVelocityRadPerSec() * 3.141592653589793D * 1.0D / 1.0D;
         double output = this.drivePID.calculate(percent);
         this.driveSim.setInputVoltage(output);
         SmartDashboard.putNumber("Closed Loop Drive #" + this.num, output);
      }

      this.turnPID.setSetpoint(state.angle.getRadians());
      SmartDashboard.putNumber("Turning Angle #" + this.num, Units.radiansToDegrees(this.turnSim.getAngularPositionRad()));
      this.turnSim.setInputVoltage(this.turnPID.calculate(this.turnSim.getAngularPositionRad()));
   }

   public SwerveModulePosition getPosition() {
      SwerveModulePosition position = new SwerveModulePosition(this.driveSim.getAngularPositionRad() * 3.141592653589793D * 1.0D / 1.0D, new Rotation2d(this.turnSim.getAngularPositionRad()));
      SmartDashboard.putNumber("Wheel Displacement #" + this.num, position.distanceMeters);
      return position;
   }

   public SwerveModuleState getModuleState() {
      return this.state;
   }

   public void updateSim() {
      this.driveSim.update(0.02D);
      this.turnSim.update(0.02D);
   }
}