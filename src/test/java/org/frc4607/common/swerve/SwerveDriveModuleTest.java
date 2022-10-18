package org.frc4607.common.swerve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import java.util.Random;
import org.frc4607.common.swerve.SwerveDriverConfig.MotorType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Runs tests on {@link org.frc4607.common.swerve.SwerveDriveModule}.
 */
public class SwerveDriveModuleTest {
    private static final double DELTA = 1e-2;

    private static class TestDriver extends SwerveMotorBase {
        public double m_encoderPosition;
        public double m_encoderVelocity;

        public double m_value;

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
    public void testTurnMotorPosition() {
        double value = m_random.nextDouble();
        m_turnMotor.m_encoderPosition = value;
        assertEquals("Turn motor position value did not match.", value,
            m_module.getTurnMotorPosition(), DELTA);
    }

    @Test
    public void testDriveMotorPosition() {
        double value = m_random.nextDouble();
        m_driveMotor.m_encoderPosition = value;
        assertEquals("Drive motor position value did not match.", value,
            m_module.getDriveMotorPosition(), DELTA);
    }

    @Test
    public void testTurnMotorVelocity() {
        double value = m_random.nextDouble();
        m_turnMotor.m_encoderVelocity = value;
        assertEquals("Turn motor velocity value did not match.", value,
            m_module.getTurnMotorVelocity(), DELTA);
    }

    @Test
    public void testDriveMotorVelocity() {
        double value = m_random.nextDouble();
        m_driveMotor.m_encoderVelocity = value;
        assertEquals("Drive motor velocity value did not match.", value,
            m_module.getDriveMotorVelocity(), DELTA);
    }

    @Test
    public void testDriveMotorNotConnected() {
        m_driveMotor.m_isConnected = false;
        assertFalse("Drive motor connected unexpectedly.", m_module.isDriveMotorConnected());
    }

    @Test
    public void testTurnMotorNotConnected() {
        m_turnMotor.m_isConnected = false;
        assertFalse("Turn motor connected unexpectedly.", m_module.isTurnMotorConnected());
    }

    @Test
    public void testDriveMotorConnected() {
        m_driveMotor.m_isConnected = true;
        assertTrue("Drive motor disconnected unexpectedly.", m_module.isDriveMotorConnected());
    }

    @Test
    public void testTurnMotorConnected() {
        m_turnMotor.m_isConnected = true;
        assertTrue("Turn motor disconnected unexpectedly.", m_module.isTurnMotorConnected());
    }

    @Test
    public void testSetMethodDriveMotor() {
        m_module.set(new SwerveModuleState(1, Rotation2d.fromDegrees(90)));
        assertEquals("Drive motor velocity unexpected.", 1.0, m_driveMotor.m_value, DELTA);
    }

    @Test
    public void testSetMethodTurnMotor() {
        m_module.set(new SwerveModuleState(1, Rotation2d.fromDegrees(90)));
        assertEquals("Turn motor position unexpected.", 90, m_turnMotor.m_value, DELTA);
    }

    @Test
    public void testModulePosition() {
        assertEquals("Module position does not match assigned value.", new Translation2d(0.5, 0.5),
            m_module.getModuleLocation());
    }
}
