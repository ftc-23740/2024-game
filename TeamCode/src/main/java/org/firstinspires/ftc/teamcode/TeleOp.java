/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.CRServoImplEx;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.ServoImplEx;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

/*
 * This file contains an example of an iterative (Non-Linear) "OpMode".
 * An OpMode is a 'program' that runs in either the autonomous or the teleop period of an FTC match.
 * The names of OpModes appear on the menu of the FTC Driver Station.
 * When a selection is made from the menu, the corresponding OpMode
 * class is instantiated on the Robot Controller and executed.
 *
 * This particular OpMode just executes a basic Tank Drive Teleop for a two wheeled robot
 * It includes all the skeletal structure that all iterative OpModes contain.
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this OpMode to the Driver Station OpMode list
 */

@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "Cyberhazards", group = "Iterative OpMode")
public class TeleOp extends OpMode {
    // Declare OpMode members.

    private DcMotor frontLeft = null;
    private DcMotor frontRight = null;
    private DcMotor backLeft = null;
    private DcMotor backRight = null;

    private DcMotorEx viperSlide = null;
    private DcMotorEx elbow = null;

    private CRServoImplEx roller = null;
    private CRServoImplEx roller2 = null;
    private ServoImplEx wrist = null;


    private final int ELBOW_STRAIGHT_UP = 706;
    private final int ELBOW_STRAIGHT_OUT = 2090;
    private final int ELBOW_PICKUP = 3078;
    private final int ELBOW_BACK = 325;

    private int lastElbowPosition = 0;
    private int loopsWithoutElbowMovement = 0;
    private boolean elbowDisabled = false;

    /*
     * Code to run ONCE when the driver hits INIT
     */
    @Override
    public void init() {
        telemetry.addData("Status", "Initialized");

        // Initialize the hardware variables. Note that the strings used here as parameters
        // to 'get' must correspond to the names assigned during the robot configuration
        // step (using the FTC Robot Controller app on the phone).
        frontLeft = hardwareMap.get(DcMotor.class, "front_left");
        frontRight = hardwareMap.get(DcMotor.class, "front_right");
        backLeft = hardwareMap.get(DcMotor.class, "back_left");
        backRight = hardwareMap.get(DcMotor.class, "back_right");

        viperSlide = hardwareMap.get(DcMotorEx.class, "viper_slide");
        elbow = hardwareMap.get(DcMotorEx.class, "elbow");

        roller = hardwareMap.get(CRServoImplEx.class, "roller");
        roller2 = hardwareMap.get(CRServoImplEx.class, "roller2");
        wrist = hardwareMap.get(ServoImplEx.class, "wrist");

        // To drive forward, most robots need the motor on one side to be reversed, because the axles point in opposite directions.
        // Pushing the left stick forward MUST make robot go forward. So adjust these two lines based on your first test drive.
        // Note: The settings here assume direct drive on left and right wheels.  Gear Reduction or 90 Deg drives may require direction flips
        frontLeft.setDirection(DcMotor.Direction.FORWARD);
        frontRight.setDirection(DcMotor.Direction.REVERSE);
        backLeft.setDirection(DcMotor.Direction.FORWARD);
        backRight.setDirection(DcMotor.Direction.FORWARD);

        roller.setDirection(DcMotorSimple.Direction.FORWARD);

        viperSlide.setDirection(DcMotor.Direction.FORWARD);
        elbow.setDirection(DcMotor.Direction.FORWARD);

        viperSlide.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        if (!InitTracker.getInstance().didInit)
            elbow.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        elbow.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        PIDFCoefficients C = elbow.getPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER);
        telemetry.addData("P", C.p);
        telemetry.addData("I", C.i);
        telemetry.addData("D", C.d);
        telemetry.addData("F", C.f);

        C.p = 12;
        C.i = 3;
        C.d = 2;
        C.f = 0;

        elbow.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, C);

        lastElbowPosition = elbow.getCurrentPosition();

        // Tell the driver that initialization is complete.
        telemetry.addData("Status", "Initialized");
    }

    /*
     * Code to run REPEATEDLY after the driver hits INIT, but before they hit START
     */
    @Override
    public void init_loop() {
        telemetry.addData("encoder", elbow.getCurrentPosition());
    }

    /*
     * Code to run ONCE when the driver hits START
     */
    @Override
    public void start() {
        elbow.setTargetPosition(ELBOW_STRAIGHT_UP);
        elbow.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        elbow.setPower(0.75);
        wrist.setPosition(0.5);
    }

    /*
     * Code to run REPEATEDLY after the driver hits START but before they hit STOP
     */
    @Override
    public void loop() {
        telemetry.addData("encoder", elbow.getCurrentPosition());
        telemetry.addData("setpoint", elbow.getTargetPosition());

        double x = gamepad1.left_stick_x;
        double y = gamepad1.left_stick_y;
        double z = gamepad1.right_stick_x;

        if (gamepad1.right_bumper) {
            x = x * 0.3;
            y = y * 0.3;
            z = z * 0.3;
        }


        frontLeft.setPower(y - x - z);
        frontRight.setPower(y + x + z);
        backLeft.setPower(y + x - z);
        backRight.setPower(y - x + z);

        if (gamepad2.left_bumper || gamepad2.right_bumper) {
            roller.setPower(-.3);
            roller2.setPower(.2);
        } else if (gamepad2.left_trigger > .5 || gamepad2.right_trigger > .5) {
            roller.setPower(1);
            roller2.setPower(-1);
        } else {
            roller.setPower(0);
            roller2.setPower(0);
        }


        if (gamepad2.dpad_up) {
            wrist.setPosition(0.5);
        }

        if (gamepad2.dpad_left) {
            wrist.setPosition(0.75);
        }

        if (gamepad2.dpad_right) {
            wrist.setPosition(0.25);
        }

//        if(gamepad2.right_trigger > 0.2) {
//            viperSlide.setTargetPosition(0);
//
//        }
//
//        if(gamepad2.left_trigger > 0.2) {
//            viperSlide.setTargetPosition(0);
//
//        }
//
//        if(gamepad2.right_stick_y > 0) {
//            viperSlide.setTargetPosition(0);
//        }

        viperSlide.setPower(gamepad2.left_stick_y);
        // elbow.setPower(gamepad2.left_stick_y);

        if (!elbowDisabled) {
            if (gamepad2.circle) {
                elbow.setTargetPosition(ELBOW_STRAIGHT_OUT);
                elbow.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                elbow.setPower(0.75);
            }

            if (gamepad2.triangle) {
                elbow.setTargetPosition(ELBOW_STRAIGHT_UP);
                elbow.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                elbow.setPower(0.75);
            }

            if (gamepad2.cross) {
                elbow.setTargetPosition(ELBOW_PICKUP);
                elbow.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                elbow.setPower(0.75);
            }

            if (gamepad2.square) {
                elbow.setTargetPosition(ELBOW_BACK);
                elbow.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                elbow.setPower(0.75);
            }

            if (Math.abs(gamepad2.right_stick_y) > 0.25) {
                elbow.setTargetPosition(elbow.getTargetPosition() + 5 * Math.round(Math.signum(gamepad2.right_stick_y)));
            }

            if (Math.abs(elbow.getCurrent(CurrentUnit.MILLIAMPS)) > 500 && 1 == 2) {
                if (Math.abs(lastElbowPosition - elbow.getCurrentPosition()) < 1) {
                    loopsWithoutElbowMovement++;
                } else {
                    loopsWithoutElbowMovement = 0;
                    lastElbowPosition = elbow.getCurrentPosition();
                }
            }
        }

        if (loopsWithoutElbowMovement > 20) {
            elbow.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            elbow.setPower(0);
            elbowDisabled = true;
            telemetry.addData("ELBOW", "ELBOW DISABLED");
        }

//        if (gamepad2.square) {
//            elbowDisabled = false;
//            loopsWithoutElbowMovement = 0;
//            elbow.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
//            elbow.setPower(0);
//            telemetry.addData("ELBOW", "Elbow Enabled");
//        }

        telemetry.addData("Current", elbow.getCurrent(CurrentUnit.MILLIAMPS));
        telemetry.addData("LWEM", loopsWithoutElbowMovement);
        telemetry.addData("Elbow Pos", elbow.getCurrentPosition());
    }

    /*
     * Code to run ONCE after the driver hits STOP
     */
    @Override
    public void stop() {
        frontLeft.setPower(0);
        frontRight.setPower(0);
        backLeft.setPower(0);
        backRight.setPower(0);
        viperSlide.setPower(0);
    }

}
