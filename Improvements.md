MAXIMUMDRIVE VERSION HISTORY & CHANGELOG
=========================================

================================================================================
JACKSON TEST (ORIGINAL - BASELINE)
================================================================================

Basic mecanum drive with single controller, two servos, and launch motor.
Issues: Poor organization, no speed modes, no RPM measurement, servo drift.


================================================================================
V2BETA - TWO-CONTROLLER SYSTEM
================================================================================

MAJOR ADDITIONS:
✓ Two-controller system (Driver + Operator)
✓ Speed modes: Normal (80%), Turbo (100%), Precision (40%)
✓ Launch motor RPM measurement with visual bar
✓ RPM status indicators (STOPPED, LOW, MEDIUM, HIGH, MAXIMUM)
✓ Professional code structure and documentation
✓ Button controls for servos (A/B/X) + Y for max launch

TECHNICAL:
- DcMotorEx for velocity measurement
- TICKS_PER_REV = 28 (configurable)
- Zero power behavior: BRAKE


================================================================================
V3BETA → V4BETA - CONTROL FIXES & CAMERA
================================================================================

NOTE: V3Beta was upgraded and renamed to V4Beta when camera was added.

V3BETA IMPROVEMENTS:
✓ Fixed motor directions for proper movement
✓ Corrected joystick mappings
✓ Added joystick deadzone (0.1) to prevent servo drift
✓ Resolved rotation control issues

V4BETA ADDITIONS:
✓ Live camera preview to Driver Station
✓ Camera FPS and state monitoring
✓ VisionPortal integration

FINAL MOTOR DIRECTIONS:
- Front Left: FORWARD, Back Left: REVERSE
- Front Right: FORWARD, Back Right: REVERSE

CONTROLS:
- Left Stick Y: Forward/Back
- Left Stick X: Strafe
- Right Stick X: Rotate


================================================================================
V5OBL - ONE-BUTTON LAUNCH SYSTEM
================================================================================

FOCUS: Automated launch sequence with emergency controls

ONE-BUTTON LAUNCH (D-Pad Down):
STEP 1: Servos hold balls backwards, motor starts spinning
STEP 2: Wait for RPM >= 3500 (READY TO LAUNCH)
STEP 3: Execute 3 launch pulses (0.3s each, forward servo motion)
STEP 4: Spin down, wait 2s, return to manual

Display: 1 2 3 4 → [1] [2] 3 4 → [1] [2] [3] 4 → [1] [2] [3] [4]

EMERGENCY STOP (D-Pad Up):
- Stops everything immediately
- Shows "ABORTED" status
- Can restart after 1 second

NEW RPM RANGES:
- STOPPED: 0-49
- LOW: 50-1,499
- MEDIUM: 1,500-3,499
- READY TO LAUNCH: 3,500-4,499
- OVERSPEED: 4,500+

SMART INTERFACE:
- IDLE mode: Shows all details (encoder, intake, camera)
- During launch: Hides extras for cleaner display
- Manual controls locked during auto sequence

SETTINGS:
- Max RPM: 4500
- RPM bar scales to 4000
- Launch pulse duration: 0.3s
- Number of pulses: 3


================================================================================
AUTONOMOUSV1 - BASIC AUTONOMOUS
================================================================================

Simple autonomous program that strafes right for 3 seconds.
Uses same motor configuration as V5OBL.
Good template for building complex autonomous sequences.


================================================================================
FEATURE COMPARISON
================================================================================

FEATURE                    | V2Beta | V4Beta | V5OBL | Auto V1
---------------------------|--------|--------|-------|--------
Two-controller system      |   ✓    |   ✓    |   ✓   |   -
Speed modes (3 levels)     |   ✓    |   ✓    |   ✓   |   -
RPM measurement & bar      |   ✓    |   ✓    |   ✓   |   -
Fixed controls & deadzone  |   -    |   ✓    |   ✓   |   -
Camera streaming           |   -    |   ✓    |   ✓   |   -
One-button launch          |   -    |   -    |   ✓   |   -
Emergency stop             |   -    |   -    |   ✓   |   -
Smart telemetry            |   -    |   -    |   ✓   |   -
Autonomous movement        |   -    |   -    |   -   |   ✓


================================================================================
RECOMMENDED USAGE
================================================================================

COMPETITION → V5OBL (most features, one-button launch, emergency stop)
PRACTICE → V4Beta (simpler, full manual control)
AUTONOMOUS → AutonomousV1 as template


================================================================================
HARDWARE CONFIGURATION
================================================================================

MOTORS:
- front_left, back_left, front_right, back_right: DcMotor
- out_put: DcMotorEx (launch motor with encoder)

SERVOS:
- servo1, servo2: CRServo (servo2 runs opposite)

CAMERA:
- Webcam123: WebcamName

ENCODER:
- TICKS_PER_REV: 28 (REV HD Hex Motor - adjust for your motor)


================================================================================
QUICK TROUBLESHOOTING
================================================================================

Servos spinning continuously → V3Beta+ have deadzone fix
Robot not moving → Check motor directions match V4Beta config
No camera video → Tap camera icon on Driver Station (need DS 8.0+)
RPM shows 0 → Check encoder connection, verify TICKS_PER_REV
Launch won't start → Ensure RPM reaches 3500 in Step 2


================================================================================

Development: Om (with Claude)
Platform: FTC - REV Control Hub
Current Version: V5OBL (Dec 2024)