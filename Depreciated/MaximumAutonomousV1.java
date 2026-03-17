package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * MaximumAutonomousV1
 * 
 * Simple autonomous that strafes the robot to the right for 3 seconds
 */

@Autonomous(name="MaximumAutonomousV1", group="Competition")
public class MaximumAutonomousV1 extends LinearOpMode {

    // Declare hardware
    private DcMotor frontLeftDrive = null;
    private DcMotor frontRightDrive = null;
    private DcMotor backLeftDrive = null;
    private DcMotor backRightDrive = null;
    
    private ElapsedTime runtime = new ElapsedTime();

    @Override
    public void runOpMode() {
        
        // Initialize drive motors
        frontLeftDrive = hardwareMap.get(DcMotor.class, "front_left");
        backLeftDrive = hardwareMap.get(DcMotor.class, "back_left");
        frontRightDrive = hardwareMap.get(DcMotor.class, "front_right");
        backRightDrive = hardwareMap.get(DcMotor.class, "back_right");
        
        // Set motor directions - same as MaximumDriveV3Beta
        frontLeftDrive.setDirection(DcMotor.Direction.FORWARD);
        backLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        frontRightDrive.setDirection(DcMotor.Direction.FORWARD);
        backRightDrive.setDirection(DcMotor.Direction.REVERSE);
        
        // Set motors to brake when power is zero
        frontLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Display initialization status
        telemetry.addData("Status", "Initialized");
        telemetry.addData("Action", "Will strafe RIGHT for 3 seconds");
        telemetry.update();

        // Wait for the driver to press START
        waitForStart();
        runtime.reset();

        // Strafe right for 3 seconds
        // For strafing right: lateral = positive value
        double axial = 0;      // No forward/backward movement
        double lateral = 1.0;  // Strafe right at full power
        double yaw = 0;        // No rotation
        
        // Calculate wheel powers using mecanum drive formulas
        double frontLeftPower = axial + lateral + yaw;
        double frontRightPower = axial - lateral - yaw;
        double backLeftPower = axial - lateral + yaw;
        double backRightPower = axial + lateral - yaw;
        
        // Set the motor powers
        frontLeftDrive.setPower(frontLeftPower);
        frontRightDrive.setPower(frontRightPower);
        backLeftDrive.setPower(backLeftPower);
        backRightDrive.setPower(backRightPower);
        
        // Run for 3 seconds
        while (opModeIsActive() && runtime.seconds() < 3.0) {
            telemetry.addData("Status", "Strafing Right");
            telemetry.addData("Time Elapsed", "%.1f seconds", runtime.seconds());
            telemetry.addData("Time Remaining", "%.1f seconds", 3.0 - runtime.seconds());
            telemetry.update();
        }
        
        // Stop all motors
        frontLeftDrive.setPower(0);
        frontRightDrive.setPower(0);
        backLeftDrive.setPower(0);
        backRightDrive.setPower(0);
        
        telemetry.addData("Status", "Complete!");
        telemetry.addData("Total Time", "%.1f seconds", runtime.seconds());
        telemetry.update();
    }
}
