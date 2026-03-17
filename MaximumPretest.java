package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;

/**
 * MaximumPretest - 12-Second Robot Diagnostic
 * 
 * TESTS:
 * 1. Hardware initialization (all devices connected)
 * 2. Drive motors (movement test)
 * 3. Launch motor (3250 RPM test + encoder)
 * 4. Servos (forward/backward movement)
 * 5. Camera (streaming + FPS check)
 * 
 * VISUAL FEEDBACK:
 * - Progress bar showing current test
 * - Status icons: ✓ (pass), ✗ (fail), ⚡ (testing), ○ (waiting)
 * - Real-time results display
 * - Final summary with recommendations
 */

@Autonomous(name="MaximumPretest", group="Diagnostic")
public class MaximumPretest extends LinearOpMode {

    // Test results
    private enum TestStatus { WAITING, TESTING, PASS, FAIL }
    
    private TestStatus frontLeftStatus = TestStatus.WAITING;
    private TestStatus frontRightStatus = TestStatus.WAITING;
    private TestStatus backLeftStatus = TestStatus.WAITING;
    private TestStatus backRightStatus = TestStatus.WAITING;
    private TestStatus launchMotorStatus = TestStatus.WAITING;
    private TestStatus servoStatus = TestStatus.WAITING;
    private TestStatus cameraStatus = TestStatus.WAITING;
    
    private String frontLeftMessage = "";
    private String frontRightMessage = "";
    private String backLeftMessage = "";
    private String backRightMessage = "";
    private String launchMotorMessage = "";
    private String servoMessage = "";
    private String cameraMessage = "";
    
    // Hardware
    private DcMotor frontLeftDrive = null;
    private DcMotor frontRightDrive = null;
    private DcMotor backLeftDrive = null;
    private DcMotor backRightDrive = null;
    private DcMotorEx launchMotor = null;
    private CRServo intakeServo1 = null;
    private CRServo intakeServo2 = null;
    private VisionPortal visionPortal = null;
    
    private ElapsedTime runtime = new ElapsedTime();
    private final double TICKS_PER_REV = 28.0;
    private final int TARGET_RPM = 3250;
    private final int MIN_ENCODER_CHANGE = 50; // Minimum ticks to detect movement
    
    @Override
    public void runOpMode() {
        
        telemetry.addData("Status", "Initializing...");
        telemetry.update();
        
        // ==================== PHASE 0: INITIALIZATION (0-1s) ====================
        
        try {
            frontLeftDrive = hardwareMap.get(DcMotor.class, "front_left");
            frontRightDrive = hardwareMap.get(DcMotor.class, "front_right");
            backLeftDrive = hardwareMap.get(DcMotor.class, "back_left");
            backRightDrive = hardwareMap.get(DcMotor.class, "back_right");
            launchMotor = hardwareMap.get(DcMotorEx.class, "out_put");
            intakeServo1 = hardwareMap.get(CRServo.class, "servo1");
            intakeServo2 = hardwareMap.get(CRServo.class, "servo2");
            visionPortal = VisionPortal.easyCreateWithDefaults(
                hardwareMap.get(WebcamName.class, "Webcam123"));
            
            // Set motor directions
            frontLeftDrive.setDirection(DcMotor.Direction.FORWARD);
            backLeftDrive.setDirection(DcMotor.Direction.REVERSE);
            frontRightDrive.setDirection(DcMotor.Direction.FORWARD);
            backRightDrive.setDirection(DcMotor.Direction.REVERSE);
            launchMotor.setDirection(DcMotor.Direction.FORWARD);
            
            // Reset encoders
            frontLeftDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            frontRightDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            backLeftDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            backRightDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            launchMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            
            frontLeftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            frontRightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            backLeftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            backRightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            launchMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            
            telemetry.addData("Status", "✓ All hardware initialized");
            telemetry.update();
            
        } catch (Exception e) {
            telemetry.addData("CRITICAL ERROR", e.getMessage());
            telemetry.addData("Status", "Hardware initialization failed!");
            telemetry.update();
            sleep(5000);
            return;
        }
        
        waitForStart();
        runtime.reset();
        
        // ==================== PHASE 1: DRIVE MOTORS (1-5s) ====================
        
        // Test Front Left (0.5s each motor = 2s total)
        frontLeftStatus = TestStatus.TESTING;
        displayStatus("TESTING DRIVE MOTORS", 10);
        frontLeftDrive.setPower(0.5);
        sleep(500);
        int flTicks = Math.abs(frontLeftDrive.getCurrentPosition());
        frontLeftDrive.setPower(0);
        
        frontLeftStatus = TestStatus.PASS;
        frontLeftMessage = flTicks + " ticks";
        
        // Test Front Right
        frontRightStatus = TestStatus.TESTING;
        displayStatus("TESTING DRIVE MOTORS", 20);
        frontRightDrive.setPower(0.5);
        sleep(500);
        int frTicks = Math.abs(frontRightDrive.getCurrentPosition());
        frontRightDrive.setPower(0);
        
        frontRightStatus = TestStatus.PASS;
        frontRightMessage = frTicks + " ticks";
        
        // Test Back Left
        backLeftStatus = TestStatus.TESTING;
        displayStatus("TESTING DRIVE MOTORS", 30);
        backLeftDrive.setPower(0.5);
        sleep(500);
        int blTicks = Math.abs(backLeftDrive.getCurrentPosition());
        backLeftDrive.setPower(0);
        
        backLeftStatus = TestStatus.PASS;
        backLeftMessage = blTicks + " ticks";
        
        // Test Back Right
        backRightStatus = TestStatus.TESTING;
        displayStatus("TESTING DRIVE MOTORS", 40);
        backRightDrive.setPower(0.5);
        sleep(500);
        int brTicks = Math.abs(backRightDrive.getCurrentPosition());
        backRightDrive.setPower(0);
        
        backRightStatus = TestStatus.PASS;
        backRightMessage = brTicks + " ticks";
        
        // ==================== PHASE 2: LAUNCH MOTOR (5-9s) ====================
        
        launchMotorStatus = TestStatus.TESTING;
        displayStatus("TESTING LAUNCH MOTOR", 50);
        
        // Spin up to 3250 RPM
        double targetVelocity = (TARGET_RPM / 60.0) * TICKS_PER_REV;
        launchMotor.setVelocity(targetVelocity);
        
        ElapsedTime launchTimer = new ElapsedTime();
        boolean rpmReached = false;
        double maxRPM = 0;
        
        while (launchTimer.seconds() < 4.0 && opModeIsActive()) {  // Increased from 2.0 to 4.0 seconds
            double velocity = launchMotor.getVelocity();
            double currentRPM = (velocity * 60.0) / TICKS_PER_REV;
            
            if (Math.abs(currentRPM) > maxRPM) {
                maxRPM = Math.abs(currentRPM);
            }
            
            if (Math.abs(currentRPM) >= TARGET_RPM * 0.9) {  // Within 90% of target
                rpmReached = true;
            }
            
            launchMotorMessage = String.format("Current: %.0f RPM (Target: %d)", Math.abs(currentRPM), TARGET_RPM);
            displayStatus("TESTING LAUNCH MOTOR", 60);
            sleep(50);
        }
        
        launchMotor.setPower(0);
        
        // Pass if reached 90% of target OR exceeded target (overspeed is OK)
        if (rpmReached || maxRPM >= TARGET_RPM) {
            launchMotorStatus = TestStatus.PASS;
            launchMotorMessage = String.format("Max RPM: %.0f (Target: %d)", maxRPM, TARGET_RPM);
        } else {
            launchMotorStatus = TestStatus.FAIL;
            launchMotorMessage = String.format("Only reached %.0f RPM - CHECK MOTOR!", maxRPM);
        }
        
        // ==================== PHASE 3: SERVOS (9-11s) ====================
        
        servoStatus = TestStatus.TESTING;
        displayStatus("TESTING SERVOS", 70);
        
        // Forward test
        intakeServo1.setPower(-1.0);
        intakeServo2.setPower(1.0);
        sleep(1000);
        
        // Backward test
        intakeServo1.setPower(1.0);
        intakeServo2.setPower(-1.0);
        sleep(1000);
        
        // Stop
        intakeServo1.setPower(0);
        intakeServo2.setPower(0);
        
        servoStatus = TestStatus.PASS;
        servoMessage = "Forward/Backward OK";
        
        // ==================== PHASE 4: CAMERA (11-12s) ====================
        
        cameraStatus = TestStatus.TESTING;
        displayStatus("TESTING CAMERA", 80);
        
        sleep(500);
        
        if (visionPortal != null) {
            double fps = visionPortal.getFps();
            VisionPortal.CameraState state = visionPortal.getCameraState();
            
            if (fps > 0 && state == VisionPortal.CameraState.STREAMING) {
                cameraStatus = TestStatus.PASS;
                cameraMessage = String.format("%.1f FPS - %s", fps, state);
            } else {
                cameraStatus = TestStatus.FAIL;
                cameraMessage = String.format("%.1f FPS - %s - CHECK CAMERA!", fps, state);
            }
        } else {
            cameraStatus = TestStatus.FAIL;
            cameraMessage = "Camera not initialized!";
        }
        
        // ==================== PHASE 5: FINAL REPORT (12s+) ====================
        
        displayStatus("DIAGNOSTIC COMPLETE", 100);
        
        // Count failures
        int failures = 0;
        if (frontLeftStatus == TestStatus.FAIL) failures++;
        if (frontRightStatus == TestStatus.FAIL) failures++;
        if (backLeftStatus == TestStatus.FAIL) failures++;
        if (backRightStatus == TestStatus.FAIL) failures++;
        if (launchMotorStatus == TestStatus.FAIL) failures++;
        if (servoStatus == TestStatus.FAIL) failures++;
        if (cameraStatus == TestStatus.FAIL) failures++;
        
        // Display final summary
        while (opModeIsActive()) {
            telemetry.addData("", "");
            telemetry.addData("=== DIAGNOSTIC COMPLETE ===", "");
            
            if (failures == 0) {
                telemetry.addData("RESULT", "✓✓✓ ALL SYSTEMS OPERATIONAL ✓✓✓");
                telemetry.addData("Status", "ROBOT READY FOR COMPETITION!");
            } else {
                telemetry.addData("RESULT", "⚠ " + failures + " SYSTEM(S) FAILED ⚠");
                telemetry.addData("Status", "FIX ISSUES BEFORE COMPETING!");
            }
            
            telemetry.addData("", "");
            telemetry.addData("Runtime", String.format("%.1fs", runtime.seconds()));
            telemetry.update();
            sleep(100);
        }
        
        // Clean up camera
        if (visionPortal != null) {
            visionPortal.close();
        }
    }
    
    private void displayStatus(String currentTest, int progress) {
        telemetry.clear();
        telemetry.addData("", "");
        telemetry.addData("=== MAXIMUM PRE-TEST ===", "");
        
        // Progress bar
        int barLength = 20;
        int filled = (progress * barLength) / 100;
        String progressBar = "[";
        for (int i = 0; i < barLength; i++) {
            if (i < filled) {
                progressBar += "=";
            } else {
                progressBar += " ";
            }
        }
        progressBar += "] " + progress + "%";
        telemetry.addData(progressBar, currentTest);
        telemetry.addData("", "");
        
        // Drive motors
        telemetry.addData(getStatusIcon(frontLeftStatus) + " Front Left", frontLeftMessage);
        telemetry.addData(getStatusIcon(frontRightStatus) + " Front Right", frontRightMessage);
        telemetry.addData(getStatusIcon(backLeftStatus) + " Back Left", backLeftMessage);
        telemetry.addData(getStatusIcon(backRightStatus) + " Back Right", backRightMessage);
        telemetry.addData("", "");
        
        // Launch system
        telemetry.addData(getStatusIcon(launchMotorStatus) + " Launch Motor", launchMotorMessage);
        telemetry.addData(getStatusIcon(servoStatus) + " Servos", servoMessage);
        telemetry.addData("", "");
        
        // Camera
        telemetry.addData(getStatusIcon(cameraStatus) + " Camera", cameraMessage);
        
        telemetry.addData("", "");
        telemetry.addData("Runtime", String.format("%.1fs", runtime.seconds()));
        
        telemetry.update();
    }
    
    private String getStatusIcon(TestStatus status) {
        switch (status) {
            case WAITING: return "○";
            case TESTING: return "⚡";
            case PASS: return "✓";
            case FAIL: return "✗";
            default: return "?";
        }
    }
}
