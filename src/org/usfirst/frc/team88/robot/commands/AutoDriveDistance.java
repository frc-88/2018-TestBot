package org.usfirst.frc.team88.robot.commands;

import org.usfirst.frc.team88.robot.Robot;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 *
 */
public class AutoDriveDistance extends Command {

	// states
	private static final int PREP = 0;
	private static final int ACCELERATE = 10;
	private static final int CRUISE = 1;
	private static final int DECELERATE = 5;
	private static final int STOP = 2;
	private static final int END = 3;
	private static final double CRUISING_SPEED = 0.3;
	private static final double ACCELERATION = 0.01;
	private static final double COUNTS_PER_INCH = 805;

	private int state;
	private double speed;
	private double targetDistanceCounts;
	private double targetYaw;
	private double accelerateDistance;
	private double targetDistanceInches;
	private boolean done;


	public AutoDriveDistance(double distance) {
		// Use requires() here to declare subsystem dependencies
		// eg. requires(chassis);
		requires(Robot.drive);
		targetDistanceInches=distance;
	}

	// Called just before this Command runs the first time
	protected void initialize() {
		Robot.drive.resetEncoders();

		state = PREP;
		done = false;
		speed = 0.0;
		targetDistanceCounts = targetDistanceInches * COUNTS_PER_INCH;
		targetYaw = Robot.drive.getYaw();
	}

	// Called repeatedly when this Command is scheduled to run
	protected void execute() {
		double curve = (targetYaw - (Robot.drive.getYaw())) * 0.03;

		switch (state){
		case PREP:
			if(Math.abs(Robot.drive.getAvgPosition())<100){
				state = ACCELERATE;
			}
			break;
		case ACCELERATE:
			speed = speed + ACCELERATION;
			if(Robot.drive.getAvgPosition()> 3*targetDistanceCounts/7){
				state = DECELERATE;	
				accelerateDistance = Robot.drive.getAvgPosition(); 
				SmartDashboard.putNumber("accelerateDistance", accelerateDistance);
			}
			else if (speed > CRUISING_SPEED) {
				state = CRUISE;
				accelerateDistance = Robot.drive.getAvgPosition(); 
				SmartDashboard.putNumber("accelerateDistance", accelerateDistance);
			}
			break;
		case CRUISE:
			if (Robot.drive.getAvgPosition() > (targetDistanceCounts - (accelerateDistance * 3.4))) {
				state = DECELERATE;
			}
			break;
		case DECELERATE:
			speed = speed - ACCELERATION;
			if (speed < 0) {
				speed = 0.0;
				state = STOP;
			}

			if (Robot.drive.getAvgPosition() > targetDistanceCounts) {
				speed = 0;
				state = STOP;
			}

			break;
		case STOP:
			speed = 0.0;

			state = END;
			break;
		case END:
			done = true;
			break;
		}
		SmartDashboard.putNumber("State", state);

		Robot.drive.driveCurve(speed, curve);
		Robot.drive.updateDashboard();
	}

	// Make this return true when this Command no longer needs to run execute()
	protected boolean isFinished() {
		return done;
	}

	// Called once after isFinished returns true
	protected void end() {
		Robot.drive.wheelSpeed(0, 0);
	}

	// Called when another command which requires one or more of the same
	// subsystems is scheduled to run
	protected void interrupted() {
		Robot.drive.wheelSpeed(0, 0);
	}
}
