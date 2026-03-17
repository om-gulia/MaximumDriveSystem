package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

/**
 * Improved FTC TeleOp Mode
 * 
 * GAMEPAD 1 (Driver):
 * - Left Stick: Forward/Backward and Strafe Left/Right
 * - Right Stick X: Rotate Left/Right
 * - Right Bumper: Turbo Mode (full speed)
 * - Left Bumper: Precision Mode (slow speed)
 * 
 * GAMEPAD 2 (Operator):
 * - Right Stick Y: Control Launch Motor
 * - Left Stick Y: Control Intake Servos
 * - A Button: Intake In
 * - B Button: Intake Out
 * - X Button: Stop Intake
 * - Y Button: Launch Motor Max Power
 */

@TeleOp(name="MaximumDriveV2Beta", group="Competition")
public class MaximumDriveV2Beta extends LinearOpMode {

    // Declare hardware
    private ElapsedTime runtime = new ElapsedTime();
    
    // Drive motors
    private DcMotor frontLeftDrive = null;
    private DcMotor frontRightDrive = null;
    private DcMotor backLeftDrive = null;
    private DcMotor backRightDrive = null;
    
    // Attachment motors
    private DcMotorEx launchMotor = null;
    
    // Servos
    private CRServo intakeServo1 = null;
    private CRServo intakeServo2 = null;
    
    // Speed control variables
    private double speedMultiplier = 0.8;  // Default to 80% speed
    private final double TURBO_SPEED = 1.0;
    private final double PRECISION_SPEED = 0.4;
    private final double NORMAL_SPEED = 0.8;
    
    // Launch motor encoder settings
    // Adjust TICKS_PER_REV based on your motor type:
    // REV HD Hex Motor: 28 ticks/rev
    // REV Core Hex Motor: 288 ticks/rev
    // AndyMark NeveRest 20: 537.6 ticks/rev
    // AndyMark NeveRest 40: 1120 ticks/rev
    // AndyMark NeveRest 60: 1680 ticks/rev
    private final double TICKS_PER_REV = 28.0;  // Change based on your motor

    @Override
    public void runOpMode() {
        
        // Initialize drive motors
        frontLeftDrive = hardwareMap.get(DcMotor.class, "front_left");
        backLeftDrive = hardwareMap.get(DcMotor.class, "back_left");
        frontRightDrive = hardwareMap.get(DcMotor.class, "front_right");
        backRightDrive = hardwareMap.get(DcMotor.class, "back_right");
        
        // Initialize attachment motors
        launchMotor = hardwareMap.get(DcMotorEx.class, "out_put");
        
        // Initialize servos
        intakeServo1 = hardwareMap.get(CRServo.class, "servo1");
        intakeServo2 = hardwareMap.get(CRServo.class, "servo2");
        
        // Set motor directions for proper mecanum drive
        // Using original Jackson Test configuration
        frontLeftDrive.setDirection(DcMotor.Direction.FORWARD);
        backLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        frontRightDrive.setDirection(DcMotor.Direction.FORWARD);
        backRightDrive.setDirection(DcMotor.Direction.REVERSE);
        
        // Set launch motor direction
        launchMotor.setDirection(DcMotor.Direction.FORWARD);
        
        // Enable encoder for RPM measurement
        launchMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        launchMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        
        // Set zero power behavior to brake for better control
        frontLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        launchMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Display initialization status
        telemetry.addData("Status", "Initialized - Ready to Start!");
        telemetry.addData("Drive Mode", "Normal Speed (80%)");
        telemetry.update();

        // Wait for driver to press START
        waitForStart();
        runtime.reset();

        // Main loop - runs until driver presses STOP
        while (opModeIsActive()) {
            
            // ==================== GAMEPAD 1: DRIVE CONTROLS ====================
            
            // Get joystick inputs (note: Y axis is inverted on gamepads)
            double axial = -gamepad1.left_stick_y;    // Forward/Backward
            double lateral = gamepad1.left_stick_x;    // Strafe Left/Right
            double yaw = gamepad1.right_stick_x;       // Rotate Left/Right
            
            // Speed mode selection
            if (gamepad1.right_bumper) {
                speedMultiplier = TURBO_SPEED;         // Turbo mode
            } else if (gamepad1.left_bumper) {
                speedMultiplier = PRECISION_SPEED;     // Precision mode
            } else {
                speedMultiplier = NORMAL_SPEED;        // Normal mode
            }
            
            // Calculate wheel powers for mecanum drive
            double frontLeftPower = (axial + lateral + yaw) * speedMultiplier;
            double frontRightPower = (axial - lateral - yaw) * speedMultiplier;
            double backLeftPower = (axial - lateral + yaw) * speedMultiplier;
            double backRightPower = (axial + lateral - yaw) * speedMultiplier;
            
            // Normalize wheel powers to ensure none exceed 1.0
            double maxPower = Math.max(Math.abs(frontLeftPower), Math.abs(frontRightPower));
            maxPower = Math.max(maxPower, Math.abs(backLeftPower));
            maxPower = Math.max(maxPower, Math.abs(backRightPower));
            
            if (maxPower > 1.0) {
                frontLeftPower /= maxPower;
                frontRightPower /= maxPower;
                backLeftPower /= maxPower;
                backRightPower /= maxPower;
            }
            
            // Set drive motor powers
            frontLeftDrive.setPower(frontLeftPower);
            frontRightDrive.setPower(frontRightPower);
            backLeftDrive.setPower(backLeftPower);
            backRightDrive.setPower(backRightPower);
            
            
            // ==================== GAMEPAD 2: ATTACHMENT CONTROLS ====================
            
            // Launch motor control
            double launchPower = 0;
            
            if (gamepad2.y) {
                // Y button for max launch power
                launchPower = 1.0;
            } else {
                // Right stick for variable launch control
                launchPower = -gamepad2.right_stick_y;
            }
            
            launchMotor.setPower(launchPower);
            
            // Calculate launch motor RPM
            double velocity = launchMotor.getVelocity();  // ticks per second
            double currentRPM = (velocity * 60.0) / TICKS_PER_REV;
            
            
            // Intake servo control
            double intakePower = 0;
            
            if (gamepad2.a) {
                // A button: intake in
                intakePower = 1.0;
            } else if (gamepad2.b) {
                // B button: intake out
                intakePower = -1.0;
            } else if (gamepad2.x) {
                // X button: stop intake
                intakePower = 0;
            } else {
                // Left stick for variable intake control
                intakePower = -gamepad2.left_stick_y;
            }
            
            // Set servo powers (servo2 runs opposite direction)
            intakeServo1.setPower(intakePower);
            intakeServo2.setPower(-intakePower);
            
            
            // ==================== TELEMETRY ====================
            
            // Display runtime and mode
            telemetry.addData("Status", "Run Time: " + runtime.toString());
            
            String speedMode;
            if (speedMultiplier == TURBO_SPEED) {
                speedMode = "TURBO (100%)";
            } else if (speedMultiplier == PRECISION_SPEED) {
                speedMode = "PRECISION (40%)";
            } else {
                speedMode = "NORMAL (80%)";
            }
            telemetry.addData("Drive Mode", speedMode);
            
            // Display drive motor powers
            telemetry.addData("Front Motors", "Left: %.2f | Right: %.2f", frontLeftPower, frontRightPower);
            telemetry.addData("Back Motors", "Left: %.2f | Right: %.2f", backLeftPower, backRightPower);
            
            // Display attachment status
            telemetry.addData("Launch Motor", "Power: %.2f", launchPower);
            
            // Display RPM with visual indicator
            telemetry.addData("Launch RPM", "%.0f RPM", currentRPM);
            
            // Create visual RPM bar (0-6000 RPM range, adjust as needed)
            int maxDisplayRPM = 6000;
            int barLength = 20;
            int filledBars = (int)((Math.abs(currentRPM) / maxDisplayRPM) * barLength);
            filledBars = Math.min(filledBars, barLength);
            
            StringBuilder rpmBar = new StringBuilder("|");
            for (int i = 0; i < barLength; i++) {
                if (i < filledBars) {
                    rpmBar.append("█");
                } else {
                    rpmBar.append("░");
                }
            }
            rpmBar.append("|");
            
            telemetry.addData("RPM Meter", rpmBar.toString());
            
            // RPM status indicator
            String rpmStatus;
            if (Math.abs(currentRPM) < 100) {
                rpmStatus = "● IDLE";
            } else if (Math.abs(currentRPM) < 3000) {
                rpmStatus = "◐ SPINNING";
            } else if (Math.abs(currentRPM) < 5000) {
                rpmStatus = "◉ HIGH SPEED";
            } else {
                rpmStatus = "◎ MAX POWER";
            }
            telemetry.addData("Status", rpmStatus);
            
            telemetry.addData("Intake Servos", "Power: %.2f", intakePower);
            
            telemetry.addData("", "");  // Blank line for spacing
            telemetry.addData("Controls", "RB=Turbo | LB=Precision");
            
            telemetry.update();
        }
    }
}
