/* Decompiler 4ms, total 245ms, lines 36 */
package frc.robot.subsystems.swerve;

import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;

public interface SwerveModuleIO {
   default void setModuleState(SwerveModuleState state, boolean isOpenLoop) {
   }

   default SwerveModuleState getModuleState() {
      return null;
   }

   default void setDriveVoltage(double volts) {
   }

   default void setTurnVoltage(double volts) {
   }

   default void setDriveBrakeMode(boolean enable) {
   }

   default SwerveModulePosition getPosition() {
      return null;
   }

   default void setTurnBrakeMode(boolean enable) {
   }

   default void resetEncoders() {
   }

   default void updateSim() {
   }
}