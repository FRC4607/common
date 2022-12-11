package org.frc4607.common.swerve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.simulation.ADXRS450_GyroSim;
import org.frc4607.common.swerve.SwerveDriverConfig.MotorType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Runs tests on {@link org.frc4607.common.swerve.SwerveDrive}.
 */
public class SwerveDriveTest {
    private static final double DELTA = 1e-2;

    private static class TestDriver extends SwerveMotorBase {
        public double m_encoderPosition;
        public double m_encoderVelocity;

        public double m_value;

        public boolean m_isConnected = true;

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
        c.m_turnPositionCoefficient = 1;
        c.m_wheelOffset = 45;
        c.m_motorType = MotorType.DRIVE;
        c.m_feedforward = new SimpleMotorFeedforward(1, 0.5, 0.05);
        m_driveConfig = c;
    }

    private void setupTurnConfig() {
        SwerveDriverConfig c = new SwerveDriverConfig();
        c.m_pwmChannel = 0;
        c.m_quadChannelA = 1;
        c.m_quadChannelB = 2;
        c.m_turnPositionCoefficient = 1;
        c.m_wheelOffset = 45;
        c.m_motorType = MotorType.TURNING;
        c.m_feedforward = new SimpleMotorFeedforward(1, 0.5, 0.05);
        m_turnConfig = c;
    }

    private void incrementTurnIds() {
        m_turnConfig.m_pwmChannel += 3;
        m_turnConfig.m_quadChannelA += 3;
        m_turnConfig.m_quadChannelB += 3;
    }

    private TestDriver m_driveMotorFrontLeft;
    private TestDriver m_turnMotorFrontLeft;
    private SwerveDriveModule m_moduleFrontLeft;

    private TestDriver m_driveMotorFrontRight;
    private TestDriver m_turnMotorFrontRight;
    private SwerveDriveModule m_moduleFrontRight;

    private TestDriver m_driveMotorBackLeft;
    private TestDriver m_turnMotorBackLeft;
    private SwerveDriveModule m_moduleBackLeft;

    private TestDriver m_driveMotorBackRight;
    private TestDriver m_turnMotorBackRight;
    private SwerveDriveModule m_moduleBackRight;

    private SwerveDrive m_swerveDrive;

    private ADXRS450_Gyro m_gyro;

    /**
     * Runs before each test to initialize the HAL as well as other variables.
     */
    @Before
    public void setup() {
        assertTrue("HAL initialization failed.", HAL.initialize(500, 0));

        m_gyro = new ADXRS450_Gyro();
        ADXRS450_GyroSim sim = new ADXRS450_GyroSim(m_gyro);
        sim.setAngle(0);
        sim.setRate(0);

        setupDriveConfig();
        m_driveMotorFrontLeft = new TestDriver(m_driveConfig);
        setupTurnConfig();
        m_turnMotorFrontLeft = new TestDriver(m_turnConfig);
        m_moduleFrontLeft = new SwerveDriveModule(m_driveMotorFrontLeft, m_turnMotorFrontLeft,
            new Translation2d(0.5, 0.5), 3600);
        m_driveMotorFrontRight = new TestDriver(m_driveConfig);
        incrementTurnIds();
        m_turnMotorFrontRight = new TestDriver(m_turnConfig);
        m_moduleFrontRight = new SwerveDriveModule(m_driveMotorFrontRight, m_turnMotorFrontRight,
            new Translation2d(0.5, -0.5), 3600);
        m_driveMotorBackLeft = new TestDriver(m_driveConfig);
        incrementTurnIds();
        m_turnMotorBackLeft = new TestDriver(m_turnConfig);
        m_moduleBackLeft = new SwerveDriveModule(m_driveMotorBackLeft, m_turnMotorBackLeft,
            new Translation2d(-0.5, 0.5), 3600);
        m_driveMotorBackRight = new TestDriver(m_driveConfig);
        incrementTurnIds();
        m_turnMotorBackRight = new TestDriver(m_turnConfig);
        m_moduleBackRight = new SwerveDriveModule(m_driveMotorBackRight, m_turnMotorBackRight,
            new Translation2d(-0.5, 0.5), 3600);
        
        m_swerveDrive = new SwerveDrive(10, m_gyro, m_moduleFrontLeft, m_moduleFrontRight,
            m_moduleBackLeft, m_moduleBackRight);
    }

    /**
     * Resets used resources after each test.
     */
    @After
    public void teardown() {
        m_turnMotorFrontLeft.m_quadEncoder.close();
        m_turnMotorFrontRight.m_quadEncoder.close();
        m_turnMotorBackLeft.m_quadEncoder.close();
        m_turnMotorBackRight.m_quadEncoder.close();
        m_gyro.close();
        HAL.shutdown();
    }

    @Test
    public void testUpdate() {
        try {
            m_swerveDrive.update(new ChassisSpeeds(5, 5, 2 * Math.PI));
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
    // This test makes a lot more sense with all of the asserts in one place.
    public void testModuleDropping() {
        m_swerveDrive.update(new ChassisSpeeds(5, 5, 2 * Math.PI));
        final double flPreviousVelocity = m_driveMotorFrontLeft.m_value;
        final double flPreviousRotation = m_turnMotorFrontLeft.m_value;
        m_driveMotorFrontLeft.m_isConnected = false;
        m_swerveDrive.update(new ChassisSpeeds(5, -5, -2 * Math.PI));
        assertEquals("Drive motor velocity updated after drive motor disconnect.",
            flPreviousVelocity, m_driveMotorFrontLeft.m_value, DELTA);
        assertEquals("Turn motor position updated after drive motor disconnect.",
            flPreviousRotation, m_turnMotorFrontLeft.m_value, DELTA);

        final double frPreviousVelocity = m_driveMotorFrontRight.m_value;
        final double frPreviousRotation = m_turnMotorFrontRight.m_value;
        m_turnMotorFrontRight.m_isConnected = false;
        m_swerveDrive.update(new ChassisSpeeds(-5, 5, 0));
        assertEquals("FL drive motor velocity updated after FR turn motor disconnect.",
            flPreviousVelocity, m_driveMotorFrontLeft.m_value, DELTA);
        assertEquals("FL turn motor position updated after FR turn motor disconnect.",
            flPreviousRotation, m_turnMotorFrontLeft.m_value, DELTA);
        assertEquals("FR drive motor velocity updated after FR turn motor disconnect.",
            frPreviousVelocity, m_driveMotorFrontRight.m_value, DELTA);
        assertEquals("FR turn motor position updated after FR turn motor disconnect.",
            frPreviousRotation, m_turnMotorFrontRight.m_value, DELTA);

        final double blPreviousVelocity = m_driveMotorBackLeft.m_value;
        final double blPreviousRotation = m_turnMotorBackLeft.m_value;
        m_turnMotorBackLeft.m_isConnected = false;
        m_turnMotorBackLeft.m_isConnected = false;
        m_swerveDrive.update(new ChassisSpeeds(10, -3, -.2));
        assertEquals("FL drive motor velocity updated after BR turn motor disconnect.",
            flPreviousVelocity, m_driveMotorFrontLeft.m_value, DELTA);
        assertEquals("FL turn motor position updated after BR turn motor disconnect.",
            flPreviousRotation, m_turnMotorFrontLeft.m_value, DELTA);
        assertEquals("FR drive motor velocity updated after BR turn motor disconnect.",
            frPreviousVelocity, m_driveMotorFrontRight.m_value, DELTA);
        assertEquals("FR turn motor position updated after BR turn motor disconnect.",
            frPreviousRotation, m_turnMotorFrontRight.m_value, DELTA);
        assertEquals("BL drive motor dropped without causing a kinematics error.",
            blPreviousVelocity, m_driveMotorBackLeft.m_value, DELTA);
        assertEquals("BL turn motor dropped without causing a kinematics error.",
            blPreviousRotation, m_turnMotorBackLeft.m_value, DELTA);
    }

    @Test
    public void testFieldOrientedControl() {
        // Set the gyro to be facing 90 degrees CCW.
        ADXRS450_GyroSim sim = new ADXRS450_GyroSim(m_gyro);
        sim.setAngle(-90);

        // Drive toward +X in the field coordinate system
        m_swerveDrive.updateFieldOriented(new ChassisSpeeds(10, 0, 0));

        // Modules should now face positive X with a -90 degree CCW rotation
        assertEquals("FL motor not aligned in field-oriented test",
            -90, m_turnMotorFrontLeft.m_value, DELTA);
    }
}
