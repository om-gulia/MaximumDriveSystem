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
 * MaximumDriveV5OBL - One Button Launch System
 * 
 * FEATURES:
 * - One-button automated launch sequence
 * - Emergency stop capability
 * - Live camera preview
 * - RPM monitoring with READY TO LAUNCH status
 * 
 * GAMEPAD 1 (Driver):
 * - Left Stick Y: Forward/Backward
 * - Left Stick X: Strafe Left/Right
 * - Right Stick X: Rotate Left/Right
 * - Right Bumper: Turbo Mode
 * - Left Bumper: Precision Mode
 * 
 * GAMEPAD 2 (Operator):
 * MANUAL MODE:
 * - Right Stick Y: Launch Motor
 * - Left Stick Y: Intake Servos
 * - A/B/X/Y: Manual servo controls
 * 
 * ONE-BUTTON LAUNCH:
 * - D-Pad Down: Start auto launch sequence
 * - D-Pad Up: Emergency stop
 */

@TeleOp(name="MaximumDriveV5OBL", group="Competition")
public class MaximumDriveV5OBL extends LinearOpMode {

    // Launch sequence states
    private enum LaunchState {
        IDLE,           // Manual control mode
        STEP1_HOLDING,  // Servos holding balls back
        STEP2_SPINUP,   // Motor spinning up to launch speed
        STEP3_LAUNCHING, // Executing launch pulses
        STEP4_SPINDOWN, // Stopping everything
        ABORTED         // Emergency stop activated
    }
    
    // Hardware
    private ElapsedTime runtime = new ElapsedTime();
    
    // Drive motors
    private DcMotor frontLeftDrive = null;
    private DcMotor frontRightDrive = null;
    private DcMotor backLeftDrive = null;
    private DcMotor backRightDrive = null;
    
    // Launch system
    private DcMotorEx launchMotor = null;
    private CRServo intakeServo1 = null;
    private CRServo intakeServo2 = null;
    
    // Camera
    private VisionPortal visionPortal = null;
    
    // Drive settings
    private double speedMultiplier = 0.8;
    private final double TURBO_SPEED = 1.0;
    private final double PRECISION_SPEED = 0.4;
    private final double NORMAL_SPEED = 0.8;
    
    // Launch motor settings
    private final double TICKS_PER_REV = 28.0;
    private final int MAX_LAUNCH_RPM = 3000;  // Changed from 4000 to 3000
    private final int READY_TO_LAUNCH_RPM = 3000;  // Changed from 4000 to 3000
    private final int MAX_DISPLAY_RPM = 4000;
    private final double MAX_LAUNCH_VELOCITY = (MAX_LAUNCH_RPM / 60.0) * TICKS_PER_REV;  // Max ticks/sec
    
    // Launch sequence settings
    private final double SERVO_HOLD_POWER = -1.0;    // Backwards to hold balls
    private final double SERVO_LAUNCH_POWER = 1.0;   // Forward to launch
    private final double LAUNCH_PULSE_DURATION = 0.3; // Seconds per pulse
    private final int NUMBER_OF_PULSES = 3;          // 3 balls to launch
    
    // Launch sequence state variables
    private LaunchState launchState = LaunchState.IDLE;
    private ElapsedTime sequenceTimer = new ElapsedTime();
    private ElapsedTime pulseTimer = new ElapsedTime();
    private int pulsesCompleted = 0;
    private boolean pulseActive = false;
    private boolean useVelocityControl = false;  // Track which motor control mode to use
    private boolean targetRPMReached = false;  // Track if we've hit 3000 RPM at least once

    @Override
    public void runOpMode() {
        
        // Initialize drive motors
        frontLeftDrive = hardwareMap.get(DcMotor.class, "front_left");
        backLeftDrive = hardwareMap.get(DcMotor.class, "back_left");
        frontRightDrive = hardwareMap.get(DcMotor.class, "front_right");
        backRightDrive = hardwareMap.get(DcMotor.class, "back_right");
        
        // Initialize launch system
        launchMotor = hardwareMap.get(DcMotorEx.class, "out_put");
        intakeServo1 = hardwareMap.get(CRServo.class, "servo1");
        intakeServo2 = hardwareMap.get(CRServo.class, "servo2");
        
        // Initialize camera
        visionPortal = VisionPortal.easyCreateWithDefaults(
            hardwareMap.get(WebcamName.class, "Webcam123"));
        
        // Set motor directions
        frontLeftDrive.setDirection(DcMotor.Direction.FORWARD);
        backLeftDrive.setDirection(DcMotor.Direction.REVERSE);
        frontRightDrive.setDirection(DcMotor.Direction.FORWARD);
        backRightDrive.setDirection(DcMotor.Direction.REVERSE);
        launchMotor.setDirection(DcMotor.Direction.FORWARD);
        
        // Enable encoder for RPM measurement
        launchMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        launchMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        
        // Set zero power behavior
        frontLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        launchMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Display initialization
        telemetry.addData("Status", "Initialized - Ready to Start!");
        telemetry.addData("Version", "MaximumDrive V5 OBL");
        telemetry.update();

        waitForStart();
        runtime.reset();

        // Main loop
        while (opModeIsActive()) {
            
            // ==================== GAMEPAD 1: DRIVE CONTROLS ====================
            
            double axial = -gamepad1.left_stick_y;
            double lateral = gamepad1.left_stick_x;
            double yaw = gamepad1.right_stick_x;
            
            if (gamepad1.right_bumper) {
                speedMultiplier = TURBO_SPEED;
            } else if (gamepad1.left_bumper) {
                speedMultiplier = PRECISION_SPEED;
            } else {
                speedMultiplier = NORMAL_SPEED;
            }
            
            double frontLeftPower = (axial + lateral + yaw) * speedMultiplier;
            double frontRightPower = (axial - lateral - yaw) * speedMultiplier;
            double backLeftPower = (axial - lateral + yaw) * speedMultiplier;
            double backRightPower = (axial + lateral - yaw) * speedMultiplier;
            
            double maxPower = Math.max(Math.abs(frontLeftPower), Math.abs(frontRightPower));
            maxPower = Math.max(maxPower, Math.abs(backLeftPower));
            maxPower = Math.max(maxPower, Math.abs(backRightPower));
            
            if (maxPower > 1.0) {
                frontLeftPower /= maxPower;
                frontRightPower /= maxPower;
                backLeftPower /= maxPower;
                backRightPower /= maxPower;
            }
            
            frontLeftDrive.setPower(frontLeftPower);
            frontRightDrive.setPower(frontRightPower);
            backLeftDrive.setPower(backLeftPower);
            backRightDrive.setPower(backRightPower);
            
            
            // ==================== GAMEPAD 2: LAUNCH SYSTEM ====================
            
            // Check for emergency stop (D-Pad Up)
            if (gamepad2.dpad_up && launchState != LaunchState.IDLE) {
                launchState = LaunchState.ABORTED;
                sequenceTimer.reset();
            }
            
            // Check for launch sequence start (D-Pad Down)
            if (gamepad2.dpad_down && launchState == LaunchState.IDLE) {
                launchState = LaunchState.STEP1_HOLDING;
                pulsesCompleted = 0;
                pulseActive = false;
                targetRPMReached = false;  // Reset flag
                sequenceTimer.reset();
            }
            
            // Calculate current RPM
            double velocity = launchMotor.getVelocity();
            double currentRPM = (velocity * 60.0) / TICKS_PER_REV;
            int encoderPosition = launchMotor.getCurrentPosition();
            
            // Variables for motor and servo control
            double launchPower = 0;
            double intakePower = 0;
            
            // State machine for launch sequence
            switch (launchState) {
                
                case IDLE:
                    // Manual control mode with 3000 RPM limit
                    if (gamepad2.y) {
                        // Y button uses velocity control for 3000 RPM max
                        useVelocityControl = true;
                        launchPower = 1.0;
                    } else {
                        double stickInput = -gamepad2.right_stick_y;
                        // If stick is pushed near full (>0.9), use velocity control to limit RPM
                        if (Math.abs(stickInput) > 0.9) {
                            useVelocityControl = true;
                            launchPower = Math.signum(stickInput);  // Keep direction
                        } else {
                            // Lower power uses normal control
                            useVelocityControl = false;
                            launchPower = stickInput;
                        }
                    }
                    
                    if (gamepad2.a) {
                        intakePower = 1.0;
                    } else if (gamepad2.b) {
                        intakePower = -1.0;
                    } else if (gamepad2.x) {
                        intakePower = 0;
                    } else if (Math.abs(gamepad2.left_stick_y) > 0.1) {
                        intakePower = -gamepad2.left_stick_y;
                    }
                    break;
                    
                case STEP1_HOLDING:
                    // Start holding balls and spinning up motor to 3000 RPM max
                    useVelocityControl = true;
                    launchPower = 1.0;  // Will be converted to velocity
                    intakePower = SERVO_HOLD_POWER;
                    // Immediately transition to step 2
                    launchState = LaunchState.STEP2_SPINUP;
                    sequenceTimer.reset();  // Start timer for waiting period
                    break;
                    
                case STEP2_SPINUP:
                    // Keep motor at 3000 RPM max, servos holding
                    useVelocityControl = true;
                    launchPower = 1.0;  // Will be converted to velocity
                    intakePower = SERVO_HOLD_POWER;
                    
                    // Check if we've hit 3000 RPM for the first time
                    if (!targetRPMReached && Math.abs(currentRPM) >= READY_TO_LAUNCH_RPM) {
                        // Hit 3000 RPM for first time - start 1 second timer
                        targetRPMReached = true;
                        sequenceTimer.reset();
                    }
                    
                    // Once target RPM reached, wait 1 second regardless of RPM fluctuations
                    if (targetRPMReached && sequenceTimer.seconds() >= 1.0) {
                        launchState = LaunchState.STEP3_LAUNCHING;
                        pulseTimer.reset();
                        pulseActive = false;
                        pulsesCompleted = 0;
                    }
                    break;
                    
                case STEP3_LAUNCHING:
                    // Keep motor at 3000 RPM max
                    useVelocityControl = true;
                    launchPower = 1.0;  // Will be converted to velocity
                    
                    // Execute launch pulses with 1-second wait between each
                    if (pulsesCompleted < NUMBER_OF_PULSES) {
                        if (!pulseActive) {
                            // Start a new pulse
                            pulseActive = true;
                            pulseTimer.reset();
                            intakePower = SERVO_LAUNCH_POWER;
                        } else {
                            // Continue or complete pulse
                            if (pulseTimer.seconds() < LAUNCH_PULSE_DURATION) {
                                // Pulse in progress
                                intakePower = SERVO_LAUNCH_POWER;
                            } else if (pulseTimer.seconds() < LAUNCH_PULSE_DURATION + 1.0) {
                                // 1-second wait after pulse
                                intakePower = 0;
                            } else {
                                // Pulse and wait complete, prepare for next
                                intakePower = 0;
                                pulseActive = false;
                                pulsesCompleted++;
                                pulseTimer.reset();
                            }
                        }
                    } else {
                        // All pulses complete, move to spin down
                        launchState = LaunchState.STEP4_SPINDOWN;
                        sequenceTimer.reset();
                    }
                    break;
                    
                case STEP4_SPINDOWN:
                    // Stop everything
                    useVelocityControl = false;
                    launchPower = 0;
                    intakePower = 0;
                    
                    // Wait 2 seconds then return to idle
                    if (sequenceTimer.seconds() > 2.0) {
                        launchState = LaunchState.IDLE;
                    }
                    break;
                    
                case ABORTED:
                    // Emergency stop - kill everything
                    useVelocityControl = false;
                    launchPower = 0;
                    intakePower = 0;
                    
                    // After 1 second, allow restart
                    if (sequenceTimer.seconds() > 1.0) {
                        launchState = LaunchState.IDLE;
                    }
                    break;
            }
            
            // Set motor and servo powers
            if (useVelocityControl) {
                // Use velocity control to limit to 3000 RPM max
                // Handle both forward and backward directions
                if (launchPower > 0) {
                    launchMotor.setVelocity(MAX_LAUNCH_VELOCITY);
                } else if (launchPower < 0) {
                    launchMotor.setVelocity(-MAX_LAUNCH_VELOCITY);
                } else {
                    launchMotor.setPower(0);
                }
            } else {
                // Use normal power control
                launchMotor.setPower(launchPower);
            }
            intakeServo1.setPower(-intakePower);  // Reversed
            intakeServo2.setPower(intakePower);   // Reversed
            
            
            // ==================== TELEMETRY ====================
            
            displayTelemetry(frontLeftPower, frontRightPower, backLeftPower, backRightPower,
                           launchPower, currentRPM, intakePower, encoderPosition, velocity);
            
            telemetry.update();
        }
        
        // Clean up camera
        if (visionPortal != null) {
            visionPortal.close();
        }
    }
    
    private void displayTelemetry(double fl, double fr, double bl, double br,
                                  double launchPower, double currentRPM,
                                  double intakePower, int encoderPos, double velocity) {
        
        telemetry.addData("Runtime", runtime.toString());
        telemetry.addData("", "");
        
        // Drive mode
        String speedMode = "";
        if (speedMultiplier == TURBO_SPEED) {
            speedMode = "TURBO (100%)";
        } else if (speedMultiplier == PRECISION_SPEED) {
            speedMode = "PRECISION (40%)";
        } else {
            speedMode = "NORMAL (80%)";
        }
        telemetry.addData("Drive Mode", speedMode);
        telemetry.addData("Front L/R", "%.2f / %.2f", fl, fr);
        telemetry.addData("Back L/R", "%.2f / %.2f", bl, br);
        telemetry.addData("", "");
        
        // Launch motor info
        telemetry.addData("Launch Power", "%.0f%%", launchPower * 100);
        
        // Only show detailed info in IDLE mode
        if (launchState == LaunchState.IDLE) {
            telemetry.addData("Encoder Ticks", encoderPos);
            telemetry.addData("Velocity (ticks/sec)", "%.1f", velocity);
        }
        
        telemetry.addData("Launch RPM", "%.0f", Math.abs(currentRPM));
        
        // RPM Bar (scaled to 4000 RPM max)
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
        
        // RPM Status with updated ranges for 3000 RPM max
        String rpmStatus;
        double absRPM = Math.abs(currentRPM);
        if (absRPM < 50) {
            rpmStatus = "STOPPED";
        } else if (absRPM < 1500) {
            rpmStatus = "LOW";
        } else if (absRPM < 2800) {
            rpmStatus = "MEDIUM";
        } else if (absRPM < 3200) {  // 2800-3200 is READY (allows for slight variation)
            rpmStatus = "READY TO LAUNCH";
        } else {
            rpmStatus = "OVERSPEED";
        }
        telemetry.addData("RPM Status", rpmStatus);
        
        // Only show intake and camera in IDLE mode
        if (launchState == LaunchState.IDLE) {
            telemetry.addData("", "");
            telemetry.addData("Intake", "%.0f%%", intakePower * 100);
            
            telemetry.addData("", "");
            telemetry.addData("=== CAMERA ===", "");
            if (visionPortal != null) {
                telemetry.addData("Camera FPS", "%.1f", visionPortal.getFps());
                telemetry.addData("Camera State", visionPortal.getCameraState());
            }
        }
        
        // Launch system status
        telemetry.addData("", "");
        telemetry.addData("=== LAUNCH SYSTEM ===", "");
        
        // Display step indicators
        String step1 = (launchState.ordinal() >= LaunchState.STEP1_HOLDING.ordinal() && 
                       launchState != LaunchState.IDLE && launchState != LaunchState.ABORTED) ? "[1]" : "1";
        String step2 = (launchState.ordinal() >= LaunchState.STEP2_SPINUP.ordinal() && 
                       launchState != LaunchState.IDLE && launchState != LaunchState.ABORTED) ? "[2]" : "2";
        String step3 = (launchState.ordinal() >= LaunchState.STEP3_LAUNCHING.ordinal() && 
                       launchState != LaunchState.IDLE && launchState != LaunchState.ABORTED) ? "[3]" : "3";
        String step4 = (launchState == LaunchState.STEP4_SPINDOWN) ? "[4]" : "4";
        
        if (launchState == LaunchState.ABORTED) {
            telemetry.addData("Auto Launch", "1 2 3 4 - ABORTED");
            telemetry.addData("Status", "EMERGENCY STOP!");
            telemetry.addData("", "Press D-Pad Down to restart");
        } else {
            String statusSuffix = "";
            if (launchState == LaunchState.IDLE) {
                statusSuffix = " - Ready";
            }
            telemetry.addData("Auto Launch", step1 + " " + step2 + " " + step3 + " " + step4 + statusSuffix);
            
            // Display current status
            switch (launchState) {
                case IDLE:
                    telemetry.addData("D-Pad Down", "Start Auto Launch");
                    telemetry.addData("D-Pad Up", "Emergency Stop");
                    break;
                case STEP1_HOLDING:
                case STEP2_SPINUP:
                    telemetry.addData("Status", "SPINNING UP");
                    telemetry.addData("Target RPM", READY_TO_LAUNCH_RPM);  // Shows 3000
                    if (targetRPMReached) {
                        telemetry.addData("Wait Timer", String.format("%.1fs / 1.0s", sequenceTimer.seconds()));
                    }
                    telemetry.addData("", "");
                    telemetry.addData("D-Pad Up", "EMERGENCY STOP");
                    break;
                case STEP3_LAUNCHING:
                    telemetry.addData("Status", "LAUNCHING (" + (pulsesCompleted + (pulseActive ? 1 : 0)) + "/" + NUMBER_OF_PULSES + ")");
                    if (pulseActive) {
                        telemetry.addData("Pulse Timer", "%.2fs", pulseTimer.seconds());
                    }
                    telemetry.addData("", "");
                    telemetry.addData("D-Pad Up", "EMERGENCY STOP");
                    break;
                case STEP4_SPINDOWN:
                    telemetry.addData("Status", "COMPLETE!");
                    telemetry.addData("", "Returning to manual in " + (int)(2.0 - sequenceTimer.seconds()) + "s...");
                    break;
            }
        }
    }
}
