package org.frc4607.common.swerve;

import com.revrobotics.AlternateEncoderType;
import com.revrobotics.CANEncoder;
import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;

import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.kinematics.SwerveModuleState;

public class SwerveDriveModule {
    private final CANSparkMax mDriveMotor;
    private final CANSparkMax mTurnMotor;

    private final CANEncoder mDriveEncoder;
    private final CANEncoder mTurnEncoder;

    private final CANPIDController mDrivePID;
    private final CANPIDController mTurnPID;
    
    public SwerveDriveModule(CANSparkMax drive, CANSparkMax turn) {
        mDriveMotor = drive;
        mTurnMotor = turn;

        mDriveEncoder = mDriveMotor.getEncoder();
        // https://www.revrobotics.com/rev-11-1271/
        mTurnEncoder = mTurnMotor.getAlternateEncoder(AlternateEncoderType.kQuadrature, 8192);

        mDrivePID = mDriveMotor.getPIDController();
        mDrivePID.setFeedbackDevice(mDriveEncoder);
        mTurnPID = mTurnMotor.getPIDController();
        mTurnPID.setFeedbackDevice(mTurnEncoder);
    }

    public void set(SwerveModuleState state) {
        // Remember, Rotation2d is CCW positive, so make sure to negate angles to and from it.
        SwerveModuleState newState = SwerveModuleState.optimize(state, Rotation2d.fromDegrees(-mTurnEncoder.getPosition()));
        //mDrivePID.setReference(newState.speedMetersPerSecond, ControlType.kVelocity);
        // Approximation, assuming linear motor with full power going 12 m/s.
        // https://www.swervedrivespecialties.com/products/mk3-swerve-module?variant=39420433203313
        mDriveMotor.set(Math.min(1, state.speedMetersPerSecond / 12));
        mTurnPID.setReference(-newState.angle.getDegrees(), ControlType.kPosition);
    }
}