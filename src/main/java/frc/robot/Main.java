/* Decompiler 1ms, total 301ms, lines 13 */
package frc.robot;

import edu.wpi.first.wpilibj.RobotBase;

public final class Main {
   private Main() {
   }

   public static void main(String... args) {
      RobotBase.startRobot(Robot::new);
   }
}