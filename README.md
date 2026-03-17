# MaximumDrive - FTC Robot Control System

> **⚠️ ARCHIVE NOTICE:** This code is no longer actively maintained. It was designed specifically for the **MaximumEntropy Drive Bot** and may require significant modifications to work with other robot configurations.

A competition-ready robot control system for FIRST Tech Challenge (FTC) featuring automated launch sequences, RPM-controlled motors, comprehensive diagnostics, and reliable single-controller operation.

**Built for:** MaximumEntropy Drive Bot (2024-2025 season)  
**Status:** Archived - No longer maintained

## 📋 Table of Contents

- [Overview](#overview)
- [Files in This Package](#files-in-this-package)
- [Hardware Requirements](#hardware-requirements)
- [Quick Start](#quick-start)
- [Control Schemes](#control-schemes)
- [Features](#features)
- [Troubleshooting](#troubleshooting)
- [Development Notes](#development-notes)

---

## 🎯 Overview

MaximumDrive is a complete robot control system developed for the **MaximumEntropy Drive Bot** - our FTC competition robot with a mecanum drivetrain and ball-launching mechanism. The system evolved through 5 major iterations to solve real competition challenges like battery voltage inconsistency, driver workload, and launch reproducibility.

**⚠️ Important:** This code was designed and tuned specifically for our robot's hardware configuration. Motor directions, timing constants, and RPM values are all calibrated for the MaximumEntropy Drive Bot. You will need to adapt these values for your own robot.

**Key Philosophy:** *Reliability over complexity* - every feature solves a real problem discovered during testing.

---

## 📦 Files in This Package

### Competition Code

**`MaximumDriveFinal.java`** ⭐ **RECOMMENDED FOR COMPETITION**
- Single-controller operation with Gamepad2 backup
- Universal emergency stop system
- One-button automated launch sequence
- 3000 RPM velocity-controlled launch motor
- Fixed control mappings (axes corrected)
- ~500 lines, fully featured

**`MaximumDriveV5OBL.java`**
- Two-controller system (Driver + Operator)
- Same features as Final but requires both controllers
- Good for practice with dedicated operator

### Diagnostic Tools

**`MaximumPretest.java`**
- 12-second automated diagnostic
- Tests all motors, servos, camera
- Launch motor 3250 RPM test
- Visual progress bar and status icons
- Run before every match for peace of mind

### Documentation

**`VERSION_HISTORY.txt`**
- Complete changelog across all versions
- Feature comparison table
- Technical specifications
- Troubleshooting guide

**`Jackson_Test__first_and_worst_code_`**
- Original baseline code (for reference)
- Shows how far the system evolved

---

## 🔧 Hardware Requirements

### Required Components

**Robot Control Hub:**
- REV Control Hub (Android-based)
- REV Driver Hub or Android phone for Driver Station

**Drivetrain:**
- 4x DC Motors for mecanum drive
  - Configured as: `front_left`, `back_left`, `front_right`, `back_right`
  - Must support encoder feedback

**Launch System:**
- 1x DcMotorEx with encoder (REV HD Hex Motor recommended)
  - Configured as: `out_put`
  - TICKS_PER_REV = 28 (adjust for your motor)
- 2x Continuous Rotation Servos
  - Configured as: `servo1`, `servo2`

**Camera (Optional but recommended):**
- USB Webcam (Logitech C270, C920, or Microsoft LifeCam)
  - Configured as: `Webcam123`

### Motor Directions

**⚠️ CRITICAL - MaximumEntropy Drive Bot Specific:**

These motor directions are specific to our robot's wiring and gearing. **Your robot will likely need different directions.**

```java
front_left:  FORWARD
back_left:   REVERSE
front_right: FORWARD
back_right:  REVERSE
out_put:     FORWARD
```

**You MUST test and adjust these for your robot:**
1. Set all motors to FORWARD initially
2. Push left stick forward
3. Reverse any motor that spins backward
4. Test all directions (forward, strafe, rotate)
5. Adjust until robot moves correctly

*Note: Our non-standard pattern is due to specific wiring/gearing on MaximumEntropy Drive Bot.*

---

## 🚀 Quick Start

### 1. Hardware Configuration

On your REV Control Hub:
1. Go to **Configure Robot**
2. Add motors with exact names: `front_left`, `back_left`, `front_right`, `back_right`, `out_put`
3. Add servos: `servo1`, `servo2`
4. Add camera: `Webcam123`
5. Set motor directions as specified above
6. **Save and activate configuration**

### 2. Upload Code

Using Android Studio or OnBot Java:
1. Copy `MaximumDriveFinal.java` to your `teamcode` folder
2. Copy `MaximumPretest.java` to your `teamcode` folder
3. Build and upload to robot

### 3. Pre-Competition Check

1. Select **"MaximumPretest"** from OpMode menu
2. Press **INIT**
3. Press **START**
4. Watch 12-second diagnostic
5. Verify all systems show ✓ (pass)

### 4. Run Competition Code

1. Select **"MaximumDriveFinal"** from OpMode menu
2. Press **INIT**
3. Press **START** when match begins
4. Robot is ready!

---

## 🎮 Control Schemes

### MaximumDriveFinal (Single Controller)

#### Gamepad 1 - Primary Controller (Controls Everything)

**Drive Controls:**
- **Left Stick X:** Forward/Backward
- **Left Stick Y:** Strafe Left/Right
- **Right Stick X:** Rotate
- **Right Bumper:** Turbo Mode (100% speed)
- **Left Bumper:** Precision Mode (40% speed)
- **Both Bumpers (3s):** Pre-test activation hint

**Launch System:**
- **Right Trigger:** Launch motor (variable, auto-limited to 3000 RPM)
- **Left Trigger:** Intake servos (variable)
- **Y Button:** Max launch motor (3000 RPM)
- **A Button:** Intake forward
- **B Button:** Intake backward
- **X Button:** Stop intake
- **D-Pad Down:** Start automated launch sequence
- **D-Pad Up:** ⚠️ EMERGENCY STOP (stops everything)

#### Gamepad 2 - Backup/Operator (Optional)

All controls available as backup:
- **Right Stick Y:** Launch motor
- **Left Stick Y:** Intake servos
- **A/B/X/Y:** Same as Gamepad1
- **D-Pad Down:** Start auto launch
- **D-Pad Up:** EMERGENCY STOP

---

## ✨ Features

### 1. One-Button Launch Sequence

Press **D-Pad Down** to execute automated 4-step launch:

**STEP 1 - HOLDING:**
- Servos spin backwards to hold balls in place

**STEP 2 - SPIN UP:**
- Launch motor spins to 3000 RPM
- Once 3000 RPM reached, waits 1 second
- Timer continues regardless of RPM fluctuations

**STEP 3 - LAUNCHING:**
- Fires 3 balls with timed pulses
- 0.3s forward pulse per ball
- 1-second wait between each ball

**STEP 4 - SPIN DOWN:**
- Stops all systems
- Returns to manual control after 2 seconds

**Status Display:**
```
Auto Launch: [1] [2] 3 4 - SPINNING UP
Target RPM: 3000
Wait Timer: 0.8s / 1.0s
```

### 2. Velocity-Controlled Launch Motor

**Problem Solved:** Battery voltage drops during match causing inconsistent launch speeds.

**Solution:** Uses `DcMotorEx.setVelocity()` to maintain exactly 3000 RPM regardless of battery level.

```java
// Manual mode: Full trigger/stick = 3000 RPM max
if (trigger > 0.9) {
    launchMotor.setVelocity(3000 RPM in ticks/sec);
}
```

### 3. Universal Emergency Stop

**Activated by:** D-Pad Up on **EITHER** controller

**Overrides:**
- Manual drive controls (even stuck joysticks)
- Launch motor
- Intake servos
- Automated launch sequence

**Display:**
```
⚠⚠⚠ EMERGENCY STOP ⚠⚠⚠
ALL SYSTEMS HALTED

Press D-Pad Down to Reset
```

### 4. Speed Modes

- **Normal (80%):** Default balanced speed
- **Turbo (100%):** Maximum speed, hold Right Bumper
- **Precision (40%):** Fine control, hold Left Bumper

### 5. Real-Time RPM Monitoring

Live telemetry shows:
```
Launch RPM: 2847
[==============      ] 71%

RPM Status: READY TO LAUNCH
```

**Status Ranges:**
- STOPPED: 0-49 RPM
- LOW: 50-1,499 RPM
- MEDIUM: 1,500-2,799 RPM
- READY TO LAUNCH: 2,800-3,199 RPM
- OVERSPEED: 3,200+ RPM

### 6. Live Camera Preview

- Camera streams to Driver Station automatically
- FPS and status monitoring
- Tap camera icon on DS to view feed

### 7. Pre-Competition Diagnostics

Run `MaximumPretest` before matches:

```
=== MAXIMUM PRE-TEST ===
[================    ] 80% TESTING CAMERA

✓ Front Left         143 ticks
✓ Front Right        156 ticks
✓ Back Left          128 ticks
✓ Back Right         139 ticks

✓ Launch Motor       Max RPM: 3347 (Target: 3250)
✓ Servos             Forward/Backward OK
⚡ Camera            Testing...

Runtime: 11.2s
```

**Tests:**
- All 4 drive motors (encoder verification)
- Launch motor (4 seconds to reach 3250 RPM)
- Servos (forward/backward motion)
- Camera (streaming + FPS)

**Final Report:**
```
=== DIAGNOSTIC COMPLETE ===
RESULT: ✓✓✓ ALL SYSTEMS OPERATIONAL ✓✓✓
Status: ROBOT READY FOR COMPETITION!
```

---

## 🔍 Troubleshooting

### Robot Not Moving

**Symptoms:** Motors don't respond to joystick
- Check motor directions match configuration above
- Verify motor names in Control Hub config
- Check that motors are set to RUN_USING_ENCODER mode

### RPM Shows 0

**Symptoms:** Launch motor runs but RPM display shows 0
- Check encoder cable connection
- Verify `TICKS_PER_REV = 28` matches your motor
- Ensure motor is configured as DcMotorEx, not DcMotor

### Launch Won't Start

**Symptoms:** D-Pad Down doesn't trigger launch sequence
- Wait for RPM to reach 3000 first
- Check that emergency stop isn't active
- Verify you're in IDLE mode (not already launching)

### Camera Not Showing

**Symptoms:** Camera FPS shows but no video on Driver Station
- Tap camera icon on Driver Station screen
- Update Driver Station app to version 8.0+
- Check USB cable connection
- Verify camera name is exactly "Webcam123"

### Servos Continuously Spinning

**Symptoms:** Servos spin even when not commanded
- This was fixed in V3Beta+ with joystick deadzone
- Check you're using MaximumDriveFinal or V5OBL
- Older code (Jackson Test, V2Beta) has this issue

### Wrong Control Directions

**Symptoms:** Forward/backward controls strafe instead
- MaximumDriveFinal has corrected mappings
- Left Stick X = Forward/Back, Left Stick Y = Strafe
- Older versions had this swapped

### Pre-Test False Failures

**Symptoms:** Wheels show as "FAIL" but work fine
- Make sure you're using the updated MaximumPretest
- Old version had blockage detection (removed)
- New version: wheels always pass if they spin

---

## 📝 Development Notes

### Design Decisions

**Why velocity control instead of power control?**
- Battery voltage drops from 13V to 11V during matches
- Power control: 1.0 power at 13V ≠ 1.0 power at 11V
- Velocity control: 3000 RPM stays 3000 RPM regardless of voltage

**Why 3000 RPM specifically?**
- Tested range: 2000-4500 RPM
- 3000 RPM provides optimal launch distance and consistency
- Higher speeds caused overshooting and instability

**Why one-button launch?**
- Reduces driver workload during high-pressure moments
- Eliminates human timing errors
- Reproducible launches every time

**Why the 1-second wait after reaching RPM?**
- Motor overshoots target then settles
- Without wait: launches during overshoot (inconsistent)
- 1-second wait: motor stabilizes at exactly 3000 RPM

**Why non-standard motor directions?**
- Specific robot wiring/gearing configuration
- Tested multiple standard patterns - none worked
- These directions match actual robot movement

### Version History Summary

- **Jackson Test:** Basic mecanum drive, many issues
- **V2Beta:** Two controllers, RPM measurement
- **V3Beta:** Fixed controls, added deadzone
- **V4Beta:** Camera integration
- **V5OBL:** One-button launch, velocity control
- **Final:** Single controller, emergency stop, production-ready

### Known Limitations

1. **No ball counting:** System assumes 3 balls loaded
   - Consider adding distance/color sensor for auto-detection
   
2. **No autonomous mode:** Only TeleOp
   - Use MaximumAutonomousV1 as template for building autonomous

3. **Camera for display only:** No vision processing
   - VisionPortal ready for AprilTags or object detection

4. **Fixed launch sequence:** Always 3 balls
   - Modify `NUMBER_OF_PULSES` constant if needed

### Customization

**⚠️ These values are tuned for MaximumEntropy Drive Bot - you will likely need different values:**

**Adjust Launch Speed:**
```java
// In MaximumDriveFinal.java, line ~86
private final int MAX_LAUNCH_RPM = 3000;  // Tuned for our launcher
```
*Test different values: start at 2000 RPM and increase until optimal*

**Change Number of Balls:**
```java
// Line ~95
private final int NUMBER_OF_PULSES = 3;  // Our robot holds 3 balls
```

**Adjust Motor Encoder:**
```java
// Line ~84
private final double TICKS_PER_REV = 28.0;  // REV HD Hex Motor value
```
*Common values: 28 (REV HD Hex), 1440 (NeveRest 40), 2240 (NeveRest 60)*

**Modify Launch Timing:**
```java
// Line ~94
private final double LAUNCH_PULSE_DURATION = 0.3;  // Tuned for our servos
```
*Test on your robot: may need 0.2-0.5s depending on servo speed*

---

## 📚 Additional Resources

**FTC Programming Documentation:**
- [FTC SDK Documentation](https://github.com/FIRST-Tech-Challenge/FtcRobotController)
- [Game Manual 0 (GM0)](https://gm0.org/)

**Hardware Guides:**
- [REV Robotics Expansion Hub Guide](https://docs.revrobotics.com/duo-control/)
- [Mecanum Drive Explained](https://gm0.org/en/latest/docs/robot-design/drivetrains/holonomic.html)

**Learning Resources:**
- [FTC Tutorials](https://www.youtube.com/c/FIRSTTechChallenge)
- [Java Programming Basics](https://ftc-docs.firstinspires.org/programming_resources/tutorial_specific/android_studio/writing_an_op_mode_with_android_studio/Writing-an-Op-Mode-with-Android-Studio.html)

---

## 👥 Credits

**Development:** Om Gulia (with Claude AI assistance)  
**Robot:** MaximumEntropy Drive Bot  
**Testing Platform:** FTC (FIRST Tech Challenge)  
**Hardware:** REV Control Hub with mecanum drivetrain  
**Competition Season:** 2024-2025  
**Status:** Archived - No longer maintained

---

## 📄 License

This code is provided for educational and competitive use in FIRST Tech Challenge. Teams are free to use, modify, and adapt this code for their robots.

**Recommended Citation:**
```
MaximumDrive Control System
Developed by Om Gulia, 2024
https://github.com/[your-repo-here]
```

---

## 🤝 Using This Code

**⚠️ This code is archived and no longer maintained.**

This code is provided as a reference for FTC teams building similar systems. It was designed specifically for the MaximumEntropy Drive Bot and will require modification for other robots.

**If you use this code:**
- Expect to modify motor directions, RPM values, and timing constants
- Test thoroughly on your specific robot hardware
- Motor directions in this code are specific to our robot's wiring
- Launch RPM (3000) was tuned for our ball/motor combination

**Common modifications needed:**
- Motor direction configuration (always test on your robot first)
- `TICKS_PER_REV` constant (depends on your motor type)
- `MAX_LAUNCH_RPM` value (tune for your launcher design)
- Launch timing and pulse durations
- Servo power directions

---

## ⚠️ Safety Notice

**Always test in a safe environment:**
- Clear 10-foot radius around robot
- Emergency stop tested and working
- Batteries secured and charged
- All components properly mounted
- Follow all FTC safety guidelines

**Never:**
- Run robot on unstable surfaces
- Operate with loose wiring
- Test at full speed indoors
- Leave robot powered unattended

---

## 📞 Support

**⚠️ This code is no longer actively supported.**

For reference and general questions:
1. Check the Troubleshooting section above
2. Review VERSION_HISTORY.txt for technical details
3. Consult FTC forums and community Discord servers
4. Ask your team mentors and other FTC teams

**Remember:** This code was designed for the MaximumEntropy Drive Bot. Your robot will have different hardware, wiring, and tuning requirements.

**Good luck at competition! 🏆**

---

*Last Updated: March 2026*  
*MaximumDrive Version: FINAL*  
*Robot: MaximumEntropy Drive Bot*  
*Status: ⚠️ ARCHIVED - No longer maintained*