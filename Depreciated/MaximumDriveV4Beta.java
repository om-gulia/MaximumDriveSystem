package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;

/**
 * MaximumDriveV4Beta - Enhanced FTC TeleOp Mode with Camera
 * 
 * FEATURES:
 * - Live camera preview streamed to Driver Station
 * - FPS monitoring
 * - RPM measurement for launch motor
 * 
 * GAMEPAD 1 (Driver) - STANDARD MECANUM CONTROLS:
 * - Left Stick Y: Forward/Backward
 * - Left Stick X: Strafe Left/Right
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

@TeleOp(name="MaximumDriveV4Beta", group="Competition")
public class MaximumDriveV4Beta extends LinearOpMode {

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
    
    // Camera
    private VisionPortal visionPortal = null;
    
    // Speed control variables
    private double speedMultiplier = 0.8;
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
    
    // RPM display settings
    private final int MAX_DISPLAY_RPM = 6000;  // Adjust based on your motor's max RPM

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
        
        // Initialize camera with live preview
        // Using the camera name from your robot configuration: "Webcam123"
        visionPortal = VisionPortal.easyCreateWithDefaults(
            hardwareMap.get(WebcamName.class, "Webcam123"));
        
        // Camera will automatically stream to Driver Station
        
        // Set motor directions - using your original Jackson Test configuration
        // This is what worked for your robot before
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
        telemetry.addData("Version", "MaximumDrive V4 Beta");
        telemetry.update();

        // Wait for driver to press START
        waitForStart();
        runtime.reset();

        // Main loop
        while (opModeIsActive()) {
            
            // ==================== GAMEPAD 1: DRIVE CONTROLS ====================
            
            // Standard mecanum drive controls
            double axial = -gamepad1.left_stick_y;      // Left stick Y: forward/backward (negative because gamepad inverted)
            double lateral = gamepad1.left_stick_x;     // Left stick X: strafe left/right
            double yaw = gamepad1.right_stick_x;        // Right stick X: rotation
            
            // Speed mode selection
            if (gamepad1.right_bumper) {
                speedMultiplier = TURBO_SPEED;
            } else if (gamepad1.left_bumper) {
                speedMultiplier = PRECISION_SPEED;
            } else {
                speedMultiplier = NORMAL_SPEED;
            }
            
            // Calculate wheel powers for mecanum drive
            // Using ORIGINAL Jackson Test formulas that worked!
            double frontLeftPower = (axial + lateral + yaw) * speedMultiplier;
            double frontRightPower = (axial - lateral - yaw) * speedMultiplier;
            double backLeftPower = (axial - lateral + yaw) * speedMultiplier;
            double backRightPower = (axial + lateral - yaw) * speedMultiplier;
            
            // Normalize wheel powers
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
                launchPower = 1.0;
            } else {
                launchPower = -gamepad2.right_stick_y;
            }
            
            launchMotor.setPower(launchPower);
            
            // Calculate launch motor RPM with better accuracy
            double velocity = launchMotor.getVelocity();  // ticks per second
            double currentRPM = (velocity * 60.0) / TICKS_PER_REV;
            
            // Get encoder position for debugging
            int encoderPosition = launchMotor.getCurrentPosition();
            
            // Intake servo control
            double intakePower = 0;
            
            if (gamepad2.a) {
                intakePower = 1.0;
            } else if (gamepad2.b) {
                intakePower = -1.0;
            } else if (gamepad2.x) {
                intakePower = 0;
            } else if (Math.abs(gamepad2.left_stick_y) > 0.1) {
                // Only move if joystick moved beyond deadzone (prevents drift)
                intakePower = -gamepad2.left_stick_y;
            }
            // If nothing pressed and joystick in deadzone, intakePower stays 0
            
            intakeServo1.setPower(intakePower);
            intakeServo2.setPower(-intakePower);
            
            
            // ==================== ENHANCED TELEMETRY ====================
            
            telemetry.addData("Runtime", runtime.toString());
            telemetry.addData("", "");  // Spacing
            
            // Drive mode display
            String speedMode = "";
            if (speedMultiplier == TURBO_SPEED) {
                speedMode = "TURBO (100%)";
            } else if (speedMultiplier == PRECISION_SPEED) {
                speedMode = "PRECISION (40%)";
            } else {
                speedMode = "NORMAL (80%)";
            }
            telemetry.addData("Drive Mode", speedMode);
            
            // Drive motors
            telemetry.addData("Front L/R", "%.2f / %.2f", frontLeftPower, frontRightPower);
            telemetry.addData("Back L/R", "%.2f / %.2f", backLeftPower, backRightPower);
            telemetry.addData("", "");  // Spacing
            
            // ===== LAUNCH MOTOR RPM DISPLAY =====
            telemetry.addData("Launch Power", "%.0f%%", launchPower * 100);
            
            // Debug information for encoder
            telemetry.addData("Encoder Ticks", encoderPosition);
            telemetry.addData("Velocity (ticks/sec)", "%.1f", velocity);
            
            // Check if encoder is working
            String encoderStatus;
            if (encoderPosition == 0 && Math.abs(launchPower) > 0.1) {
                encoderStatus = "WARNING: ENCODER NOT DETECTED!";
                telemetry.addData("Encoder Status", encoderStatus);
            } else {
                telemetry.addData("Launch RPM", "%.0f", Math.abs(currentRPM));
                
                // Simple RPM bar that should actually display
                int barLength = 20;
                double rpmPercentage = Math.min(Math.abs(currentRPM) / MAX_DISPLAY_RPM, 1.0);
                int filledBars = (int)(rpmPercentage * barLength);
                
                String rpmBar = "[";
                for (int i = 0; i < barLength; i++) {
                    if (i < filledBars) {
                        rpmBar += "=";
                    } else {
                        rpmBar += " ";
                    }
                }
                rpmBar += "]";
                
                telemetry.addData("RPM Bar", rpmBar);
                
                // RPM status
                double absRPM = Math.abs(currentRPM);
                String rpmStatus;
                if (absRPM < 50) {
                    rpmStatus = "STOPPED";
                } else if (absRPM < 1500) {
                    rpmStatus = "LOW";
                } else if (absRPM < 3500) {
                    rpmStatus = "MEDIUM";
                } else if (absRPM < 5000) {
                    rpmStatus = "HIGH";
                } else {
                    rpmStatus = "MAXIMUM";
                }
                telemetry.addData("RPM Status", rpmStatus);
            }
            
            telemetry.addData("", "");  // Spacing
            telemetry.addData("Intake", "%.0f%%", intakePower * 100);
            
            // ===== CAMERA STATUS =====
            telemetry.addData("", "");  // Spacing
            telemetry.addData("=== CAMERA ===", "");
            
            if (visionPortal != null) {
                telemetry.addData("Camera FPS", "%.1f", visionPortal.getFps());
                telemetry.addData("Camera State", visionPortal.getCameraState());
            } else {
                telemetry.addData("Camera", "NOT INITIALIZED");
            }
            
            telemetry.update();
        }
        
        // Clean up camera when OpMode ends
        if (visionPortal != null) {
            visionPortal.close();
        }
    }
}
