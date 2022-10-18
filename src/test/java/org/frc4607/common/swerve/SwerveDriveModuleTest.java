package org.frc4607.common.swerve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.frc4607.common.swerve.SwerveDriverConfig.MotorType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;

public class SwerveDriveModuleTest {
    private static final double DELTA = 1e-2;

    private static class TestDriver extends SwerveMotorBase {
        public double m_encoderPosition;
        public double m_encoderVelocity;

        public double m_value;
        public double m_feedforward;

        public boolean m_isConnected;

        public TestDriver(SwerveDriverConfig config) {
            super(config);
        }

        @Override
        public double getEncoderPosition() {
            return m_encoderPosition;
        }

        @Override
        public double getEncoderVelocity() {
            return m_encoderVelocity;
        }

        @Override
        public void setEncoder(double value) {
            m_encoderPosition = value;
        }

        @Override
        public void setTarget(double value, double ffVolts) {
            m_value = value;
            m_feedforward = ffVolts;
        }

        @Override
        public boolean isConnected() {
            return m_isConnected;
        }
    }

    private SwerveDriverConfig m_driveConfig;
    private SwerveDriverConfig m_turnConfig;

    private void setupDriveConfig() {
        SwerveDriverConfig c = new SwerveDriverConfig();
        c.m_pwmChannel = 0;
        c.m_quadChannelA = 1;
        c.m_quadChannelB = 2;
        c.m_turnPositionCoefficient = 1;
        c.m_wheelOffset = 45;
        c.m_motorType = MotorType.DRIVE;
        c.m_feedforward = new SimpleMotorFeedforward(1, 0.5, 0.05);
        m_driveConfig = c;
    }

    private void setupTurnConfig() {
        SwerveDriverConfig c = new SwerveDriverConfig();
        c.m_pwmChannel = 3;
        c.m_quadChannelA = 4;
        c.m_quadChannelB = 5;
        c.m_turnPositionCoefficient = 1;
        c.m_wheelOffset = 45;
        c.m_motorType = MotorType.TURNING;
        c.m_feedforward = new SimpleMotorFeedforward(1, 0.5, 0.05);
        m_turnConfig = c;
    }

    private TestDriver m_driveMotor;
    private TestDriver m_turnMotor;
    private SwerveDriveModule m_module;

    private Random m_random = new Random();

    /**
     * Runs before each test to initialize the HAL as well as other variables.
     */
    @Before
    public void setup() {
        assert HAL.initialize(500, 0);
        setupDriveConfig();
        m_driveMotor = new TestDriver(m_driveConfig);
        setupTurnConfig();
        m_turnMotor = new TestDriver(m_turnConfig);
        m_module = new SwerveDriveModule(m_driveMotor, m_turnMotor, new Translation2d(0.5, 0.5),
            3600);
    }

    @After
    public void teardown() {
        m_turnMotor.m_quadEncoder.close();
        HAL.shutdown();
    }
    
    @Test
    public void testProperites() {
        double value = m_random.nextDouble();
        m_turnMotor.m_encoderPosition = value;
        assertEquals(value, m_module.getTurnMotorPosition(), DELTA);
        value = m_random.nextDouble();
        m_driveMotor.m_encoderPosition = value;
        assertEquals(value, m_module.getDriveMotorPosition(), DELTA);
        value = m_random.nextDouble();
        m_turnMotor.m_encoderVelocity = value;
        assertEquals(value, m_module.getTurnMotorVelocity(), DELTA);
        value = m_random.nextDouble();
        m_driveMotor.m_encoderVelocity = value;
        assertEquals(value, m_module.getDriveMotorVelocity(), DELTA);
        m_driveMotor.m_isConnected = false;
        assertFalse(m_module.isDriveMotorConnected());
        m_turnMotor.m_isConnected = false;
        assertFalse(m_module.isTurnMotorConnected());
        m_driveMotor.m_isConnected = true;
        assertTrue(m_module.isDriveMotorConnected());
        m_turnMotor.m_isConnected = true;
        assertTrue(m_module.isTurnMotorConnected());
    }

    @Test
    public void testSetMethod() {
        m_module.set(new SwerveModuleState(1, Rotation2d.fromDegrees(90)));
        assertEquals(1.0, m_driveMotor.m_value, DELTA);
        assertEquals(90, m_turnMotor.m_value, DELTA);
    }

    @Test
    public void testModulePosition() {
        assertEquals(new Translation2d(0.5, 0.5), m_module.getModuleLocation());
    }
}
