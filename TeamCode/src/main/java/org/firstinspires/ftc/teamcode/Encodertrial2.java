package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cRangeSensor;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import java.lang.annotation.Target;
import java.util.Set;

/**
 * Created by Trevor on 11/6/2016.
 */

@com.qualcomm.robotcore.eventloop.opmode.Autonomous(name = "2", group = "6994 Bot")
public class Encodertrial2 extends LinearHardwareMap {
    int InitialTheta = 30;
    double HypotenuseLength = 50;
    double HypotenuseDriveTime = 5;
    ElapsedTime runtime = new ElapsedTime();
    public float Linearlasterror;


    @Override
    public void runOpMode() throws InterruptedException {
        DIM = hardwareMap.deviceInterfaceModule.get(Dim);
        FrontLeft = hardwareMap.dcMotor.get(frontLeftMotor);
        FrontRight = hardwareMap.dcMotor.get(frontRightMotor);
        BackLeft = hardwareMap.dcMotor.get(backLeftMotor);
        BackRight = hardwareMap.dcMotor.get(backRightMotor);
        Gyro = hardwareMap.gyroSensor.get(gyroSensor);
        ButtonPusherLeft = hardwareMap.servo.get(buttonPusherLeft);
        ButtonPusherRight = hardwareMap.servo.get(buttonPusherRight);
        BeaconColorSensor = hardwareMap.colorSensor.get(beaconColorSensor);
        WhiteLineFinder = hardwareMap.colorSensor.get(whiteLineFinder);
        BeaconColorSensor = hardwareMap.colorSensor.get(beaconColorSensor);
        SideRangeSensor = hardwareMap.get(ModernRoboticsI2cRangeSensor.class, sideRangeSensor);
        FrontRangeSensor = hardwareMap.get(ModernRoboticsI2cRangeSensor.class, frontRangeSensor);
        BackRangeSensor = hardwareMap.get(ModernRoboticsI2cRangeSensor.class, backRangeSensor);

        FrontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        //FrontRight.setDirection(DcMotorSimple.Direction.REVERSE);
        BackLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        //BackRight.setDirection(DcMotorSimple.Direction.REVERSE);
        BeaconColorSensor.enableLed(false);
        Gyro.calibrate();
        runtime.reset();

        while (Gyro.isCalibrating() && opModeIsActive()) {
            telemetry.addData(">", "Calibrating Gyro");
            telemetry.update();
            idle();
            sleep(50);
            telemetry.addData(">", "Ready!");
            telemetry.addData(">", "Hey Jason, Try not to Fuck up");
            telemetry.update();
        }

        waitForStart();

        sleep(500);
        while (opModeIsActive()) {
            TurnWithoutEncoder(0, .25, InitialTheta);
            runtime.reset();
            sleep(50);
            DriveWithoutEncoder(.375, InitialTheta, runtime.seconds() < HypotenuseDriveTime, false);
            runtime.reset();
            sleep(50);
            TurnWithoutEncoder(.25, 0, 0);
            runtime.reset();
            sleep(50);
            DriveWithoutEncoder(.375, 0, !WhiteLineFound(), false);
            runtime.reset();
            sleep(50);
            pressButton("blue", 0, 1, .3, .8);
            sleep(50);
            runtime.reset();
            DriveWithoutEncoder(.375, 0, runtime.seconds() < 1, false);
            runtime.reset();
            DriveWithoutEncoder(.375, 0, !WhiteLineFound(), false);
            pressButton("blue", 0, 1, .3, .8);
        }
    }


    public boolean WhiteLineFound() {

        return ((WhiteLineFinder.blue() > 180 && WhiteLineFinder.red() > 180 && WhiteLineFinder.green() > 180));
    }

    public void TurnWithoutEncoder(double LeftPower, double RightPower, int TargetAngle) {

        SetMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        do {
            if (Math.abs(getIntegratedZValue() - TargetAngle) < 1.25) {
                if (getIntegratedZValue() > TargetAngle) {
                    setPower(LeftPower, RightPower, LeftPower, RightPower);
                } else if (getIntegratedZValue() < TargetAngle) {
                    setPower(LeftPower, RightPower, LeftPower, RightPower);
                } else {
                    setPower(LeftPower, RightPower, LeftPower, RightPower);
                }
            } else {
                setPower(0, 0, 0, 0);
            }

        }
        while (Math.abs(getIntegratedZValue() - TargetAngle) > 1 && opModeIsActive());
        {
            if (Math.abs(getIntegratedZValue() - TargetAngle) < 1.25) {
                if (getIntegratedZValue() > TargetAngle) {
                    setPower(LeftPower, RightPower, LeftPower, RightPower);
                } else if (getIntegratedZValue() < TargetAngle) {
                    setPower(LeftPower, RightPower, LeftPower, RightPower);
                } else {
                    setPower(LeftPower, RightPower, LeftPower, RightPower);
                }
            } else {
                setPower(0, 0, 0, 0);
            }

        }
        setPower(0, 0, 0, 0);
        sleep(50);
    }

    public void DriveWithoutEncoder(double minPower, int AngleToMaintain, boolean StoppingEvent, boolean PIDdesired) {
        double FrontLeftDynamicPower;
        double FrontRightDynamicPower;
        double BackLeftDynamicPower;
        double BackRightDynamicPower;
        SetMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        runtime.reset();
        sleep(100);
        while (StoppingEvent) {
            if (getIntegratedZValue() > AngleToMaintain && PIDdesired) {//VeeringRight

                FrontLeftDynamicPower = Range.clip(minPower - PidPowerAdjustment(AngleToMaintain), 0, 1);
                FrontRightDynamicPower = Range.clip(minPower + PidPowerAdjustment(AngleToMaintain), 0, 1);
                BackLeftDynamicPower = Range.clip(minPower - PidPowerAdjustment(AngleToMaintain), 0, 1);
                BackRightDynamicPower = Range.clip(minPower + PidPowerAdjustment(AngleToMaintain), 0, 1);

            } else if (getIntegratedZValue() < AngleToMaintain && PIDdesired) {//VeeringLeft

                FrontLeftDynamicPower = Range.clip(minPower + PidPowerAdjustment(AngleToMaintain), 0, 1);
                FrontRightDynamicPower = Range.clip(minPower - PidPowerAdjustment(AngleToMaintain), 0, 1);
                BackLeftDynamicPower = Range.clip(minPower + PidPowerAdjustment(AngleToMaintain), 0, 1);
                BackRightDynamicPower = Range.clip(minPower - PidPowerAdjustment(AngleToMaintain), 0, 1);
            } else {

                FrontLeftDynamicPower = Range.clip(minPower, 0, 1);
                FrontRightDynamicPower = Range.clip(minPower, 0, 1);
                BackLeftDynamicPower = Range.clip(minPower, 0, 1);
                BackRightDynamicPower = Range.clip(minPower, 0, 1);
            }
            setPower(FrontLeftDynamicPower, FrontRightDynamicPower, BackLeftDynamicPower, BackRightDynamicPower);


        }
        setPower(0, 0, 0, 0);
        sleep(50);
    }

    public void Drive(double minPower, int Distance, int TargetAngle, double TimeOut, boolean PIDdesired) {
        double FrontLeftDynamicPower;
        double FrontRightDynamicPower;
        double BackLeftDynamicPower;
        double BackRightDynamicPower;
        int AngleToMaintain = getIntegratedZValue();

        sleep(200);
        //SetMode(DcMotor.RunMode.RUN_TO_POSITION);
        double EncoderTicks = Distance * 4 * Math.PI / 1120;
        FrontLeft.setTargetPosition((int) (FrontLeft.getCurrentPosition() + EncoderTicks));
        FrontRight.setTargetPosition((int) (FrontRight.getTargetPosition() + EncoderTicks));
        BackLeft.setTargetPosition((int) (BackLeft.getCurrentPosition() + EncoderTicks));
        BackRight.setTargetPosition((int) (BackRight.getTargetPosition() + EncoderTicks));
        FrontLeft.setPower(minPower);
        FrontRight.setPower(minPower);
        BackLeft.setPower(minPower);
        BackRight.setPower(minPower);
        SetMode(DcMotor.RunMode.RUN_TO_POSITION);
        while ((opModeIsActive() &&
                FrontLeft.isBusy() &&
                FrontRight.isBusy() &&
                BackLeft.isBusy() &&
                BackRight.isBusy())
                || (runtime.seconds() < TimeOut &&
                opModeIsActive())) {


        /*while (((Math.abs(FrontLeft.getCurrentPosition())<EncoderTicks&&*//*
                Math.abs(FrontRight.getCurrentPosition())<EncoderTicks&&
                Math.abs(BackLeft.getCurrentPosition())<EncoderTicks&&
                Math.abs(BackRight.getCurrentPosition())<EncoderTicks)&&*//*
                opModeIsActive()))||(runtime.seconds()<TimeOut&&opModeIsActive())){
*/
            if (getIntegratedZValue() > TargetAngle && PIDdesired) {//VeeringRight

                FrontLeftDynamicPower = Range.clip(minPower - PidPowerAdjustment(AngleToMaintain), 0, 1);
                FrontRightDynamicPower = Range.clip(minPower + PidPowerAdjustment(AngleToMaintain), 0, 1);
                BackLeftDynamicPower = Range.clip(minPower - PidPowerAdjustment(AngleToMaintain), 0, 1);
                BackRightDynamicPower = Range.clip(minPower + PidPowerAdjustment(AngleToMaintain), 0, 1);

            } else if (getIntegratedZValue() < TargetAngle && PIDdesired) {//VeeringLeft

                FrontLeftDynamicPower = Range.clip(minPower + PidPowerAdjustment(AngleToMaintain), 0, 1);
                FrontRightDynamicPower = Range.clip(minPower - PidPowerAdjustment(AngleToMaintain), 0, 1);
                BackLeftDynamicPower = Range.clip(minPower + PidPowerAdjustment(AngleToMaintain), 0, 1);
                BackRightDynamicPower = Range.clip(minPower - PidPowerAdjustment(AngleToMaintain), 0, 1);
            } else {

                FrontLeftDynamicPower = Range.clip(minPower, 0, 1);
                FrontRightDynamicPower = Range.clip(minPower, 0, 1);
                BackLeftDynamicPower = Range.clip(minPower, 0, 1);
                BackRightDynamicPower = Range.clip(minPower, 0, 1);
            }

            setPower(FrontLeftDynamicPower, FrontRightDynamicPower, BackLeftDynamicPower, BackRightDynamicPower);
            idle();
            sleep(50);
        }
        setPower(0, 0, 0, 0);
        sleep(300);
        SetMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    }

    public void Turn(double Power, int TargetAngle) {
        double FrontLeftTurnPower = 0;
        double FrontRightTurnPower = 0;
        double BackLeftTurnPower = 0;
        double BackRightTurnPower = 0;
        SetMode(DcMotor.RunMode.RUN_USING_ENCODER);
        sleep(300);
        if (TargetAngle > getIntegratedZValue()) {
            FrontLeftTurnPower = Power;
            FrontRightTurnPower = -Power;
            BackLeftTurnPower = Power;
            BackRightTurnPower = -Power;

        } else if (TargetAngle < getIntegratedZValue()) {
            FrontLeftTurnPower = -Power;
            FrontRightTurnPower = Power;
            BackLeftTurnPower = -Power;
            BackRightTurnPower = Power;
        } else {
            setPower(0, 0, 0, 0);
        }
        do {
            setPower(FrontLeftTurnPower, FrontRightTurnPower, BackLeftTurnPower, BackRightTurnPower);
            idle();
            telemetry.addData(">", "Turning!");
            sleep(50);

        }
        while (Math.abs(getIntegratedZValue() - TargetAngle) > 1.25);
        {
            setPower(FrontLeftTurnPower, FrontRightTurnPower, BackLeftTurnPower, BackRightTurnPower);
            idle();
            telemetry.addData(">", "Turning!");
            sleep(50);

        }
        setPower(0, 0, 0, 0);
    }

    public void pressButton(String TeamColor, double LeftButtonPusherEngagedPosition, double RightButtonPusherEngagedPosition, double LeftButtonPusherStartPosition, double RightButtonPusherStartPosition) {
        if (BeaconColorSensor.blue() > BeaconColorSensor.red() && BeaconColorSensor.blue() > BeaconColorSensor.red()) {
            switch (TeamColor.toLowerCase()) {
                case "blue": {
                    if (BeaconColorSensor.blue() > BeaconColorSensor.red() && BeaconColorSensor.blue() > BeaconColorSensor.green()) {
                        ButtonPusherRight.setPosition(RightButtonPusherEngagedPosition);
                        sleep(200);
                        ButtonPusherRight.setPosition(RightButtonPusherStartPosition);
                    } else
                        ButtonPusherLeft.setPosition(LeftButtonPusherEngagedPosition);
                    sleep(200);
                    ButtonPusherLeft.setPosition(LeftButtonPusherStartPosition);
                }
                break;
                case "red": {
                    if (BeaconColorSensor.red() > BeaconColorSensor.blue() && BeaconColorSensor.red() > BeaconColorSensor.green()) {
                        ButtonPusherRight.setPosition(RightButtonPusherEngagedPosition);
                        sleep(200);
                        ButtonPusherRight.setPosition(RightButtonPusherStartPosition);
                    } else
                        ButtonPusherLeft.setPosition(LeftButtonPusherEngagedPosition);
                    sleep(200);
                    ButtonPusherLeft.setPosition(LeftButtonPusherStartPosition);

                }
                break;
            }
        }

    }

    public void setPower(double FL, double FR, double BL, double BR) {
        FrontLeft.setPower(FL);
        FrontRight.setPower(FR);
        BackLeft.setPower(BL);
        BackRight.setPower(BR);
    }

    public void SetMode(DcMotor.RunMode mode) {
        FrontLeft.setMode(mode);
        FrontRight.setMode(mode);
        BackLeft.setMode(mode);
        BackRight.setMode(mode);
    }

    public void setMaxSpeed(int TicksPerSecond) {
        FrontLeft.setMaxSpeed(TicksPerSecond);
        FrontRight.setMaxSpeed(TicksPerSecond);
        BackLeft.setMaxSpeed(TicksPerSecond);
        BackRight.setMaxSpeed(TicksPerSecond);
    }

    public void SetPowerwithPIDAdjustment(double minPower, int TargetAngle, boolean PIDdesired) {
        double FrontLeftDynamicPower;
        double FrontRightDynamicPower;
        double BackLeftDynamicPower;
        double BackRightDynamicPower;
        int AngleToMaintain = TargetAngle;
        if (getIntegratedZValue() > TargetAngle && PIDdesired) {//VeeringRight

            FrontLeftDynamicPower = Range.clip(minPower - PidPowerAdjustment(AngleToMaintain), 0, 1);
            FrontRightDynamicPower = Range.clip(minPower + PidPowerAdjustment(AngleToMaintain), 0, 1);
            BackLeftDynamicPower = Range.clip(minPower - PidPowerAdjustment(AngleToMaintain), 0, 1);
            BackRightDynamicPower = Range.clip(minPower + PidPowerAdjustment(AngleToMaintain), 0, 1);

        } else if (getIntegratedZValue() < TargetAngle && PIDdesired) {//VeeringLeft

            FrontLeftDynamicPower = Range.clip(minPower + PidPowerAdjustment(AngleToMaintain), 0, 1);
            FrontRightDynamicPower = Range.clip(minPower - PidPowerAdjustment(AngleToMaintain), 0, 1);
            BackLeftDynamicPower = Range.clip(minPower + PidPowerAdjustment(AngleToMaintain), 0, 1);
            BackRightDynamicPower = Range.clip(minPower - PidPowerAdjustment(AngleToMaintain), 0, 1);
        } else {

            FrontLeftDynamicPower = Range.clip(minPower, 0, 1);
            FrontRightDynamicPower = Range.clip(minPower, 0, 1);
            BackLeftDynamicPower = Range.clip(minPower, 0, 1);
            BackRightDynamicPower = Range.clip(minPower, 0, 1);
        }

        setPower(FrontLeftDynamicPower, FrontRightDynamicPower, BackLeftDynamicPower, BackRightDynamicPower);
    }

    public double PidPowerAdjustment(int TargetAngle) {

        float LinearCumulativeerror = 0;
        float LinearproportionalCorrection;
        float LinearintegralCorrection;
        float LinearSlopeofderivitive;
        float LinearMaxCorrection = 100;
        float LinearMinCorrection = 15;
        float Linearerror = Math.abs(TargetAngle - getIntegratedZValue());
        LinearproportionalCorrection = (LinearproportionalConstant * Linearerror);
        LinearCumulativeerror += Linearerror;
        LinearintegralCorrection = (LinearintegralConstant * LinearCumulativeerror);
        LinearSlopeofderivitive = Linearerror - Linearlasterror;
        float Linearderivitivecorrection = (LinearSlopeofderivitive * LinearderivitiveConstant);


        float LinearCorrection = LinearproportionalCorrection + LinearintegralCorrection + Linearderivitivecorrection;

        if (LinearCorrection > LinearMaxCorrection) {
            LinearCorrection = LinearMaxCorrection;
        } else if (LinearCorrection < LinearMinCorrection) {
            LinearCorrection = LinearMinCorrection;
        } else LinearCorrection = LinearCorrection;
        return LinearCorrection;

    }

    public int getIntegratedZValue() {// Fixes the problematic wrap around from 0 to 359.
        int heading = Gyro.getHeading();
        if (heading > 180) {
            heading -= 360;
        }
        return heading;
    }

}

class MRI_Gyro_Vid4_Turn extends LinearOpMode {   //Linear op mode is being used so the program does not get stuck in loop()
    private ElapsedTime runtime = new ElapsedTime();
    private ElapsedTime timer = new ElapsedTime();

    DcMotor MLeft;  //Left Drive Motor
    DcMotor MRight;  //Right Drive Motor

    int zAccumulated;  //Total rotation left/right
    int target = 0;  //Desired angle to turn to

    GyroSensor sensorGyro;  //General Gyro Sensor allows us to point to the sensor in the configuration file.
    ModernRoboticsI2cGyro mrGyro;  //ModernRoboticsI2cGyro allows us to .getIntegratedZValue()

    @Override
    public void runOpMode() {
        telemetry.addData("Status", "Initialized");

        MLeft = hardwareMap.dcMotor.get("ml");  //Config File
        MRight = hardwareMap.dcMotor.get("mr");
        MRight.setDirection(DcMotor.Direction.REVERSE);  //This robot has two gears between motors and wheels. If your robot does not, you will need to reverse only the opposite motor

        MLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);  //Controls the speed of the motors to be consistent even at different battery levels
        MRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        sensorGyro = hardwareMap.gyroSensor.get("gyro");  //Point to the gyro in the configuration file
        mrGyro = (ModernRoboticsI2cGyro) sensorGyro;      //ModernRoboticsI2cGyro allows us to .getIntegratedZValue()
        mrGyro.calibrate();  //Calibrate the sensor so it knows where 0 is and what still is. DO NOT MOVE SENSOR WHILE BLUE LIGHT IS SOLID

        waitForStart();
        runtime.reset();

        while (mrGyro.isCalibrating()) { //Ensure calibration is complete (usually 2 seconds)
        }

        while(opModeIsActive()){
            telemetry.addData("Status", "Running: " + runtime.toString());

            if (gamepad1.a)
                target = target + 45;
            if (gamepad1.b)
                target = target - 45;

            turnAbsolute(target);
        }
    }

    //This function turns a number of degrees compared to where the robot is. Positive numbers trn left.
    public void turn(int target) throws InterruptedException {
        turnAbsolute(target + mrGyro.getIntegratedZValue());
    }

    //This function turns a number of degrees compared to where the robot was when the program started. Positive numbers trn left.
    public void turnAbsolute(int target) {
        zAccumulated = mrGyro.getIntegratedZValue();  //Set variables to gyro readings
        double turnSpeed = 0.15;

        while (Math.abs(zAccumulated - target) > 3 && opModeIsActive()) {  //Continue while the robot direction is further than three degrees from the target
            if (zAccumulated > target) {  //if gyro is positive, we will turn right
                MLeft.setPower(turnSpeed);
                MRight.setPower(-turnSpeed);
            }

            if (zAccumulated < target) {  //if gyro is positive, we will turn left
                MLeft.setPower(-turnSpeed);
                MRight.setPower(turnSpeed);
            }

            zAccumulated = mrGyro.getIntegratedZValue();  //Set variables to gyro readings
            telemetry.addData("accu", String.format("%03d", zAccumulated));
            telemetry.update();
        }

        MLeft.setPower(0);  //Stop the motors
        MRight.setPower(0);

    }

}

