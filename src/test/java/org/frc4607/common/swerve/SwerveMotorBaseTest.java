package org.frc4607.common.swerve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.NoSuchElementException;

import org.frc4607.common.swerve.SwerveDriverConfig;
import org.frc4607.common.swerve.SwerveMotorBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DigitalSource;
import edu.wpi.first.wpilibj.DutyCycle;
import edu.wpi.first.wpilibj.simulation.DIOSim;
import edu.wpi.first.wpilibj.simulation.DutyCycleSim;
import edu.wpi.first.wpilibj.simulation.EncoderSim;

public class SwerveMotorBaseTest {
    private static final double DELTA = 1e-2;
    private class SwerveMotorBaseInitable extends SwerveMotorBase {

        public SwerveMotorBaseInitable(SwerveDriverConfig config) {
            super(config);
        }

        @Override
        public double getEncoderPosition() {
            return 0;
        }

        @Override
        public double getEncoderVelocity() {
            return 0;
        }

        @Override
        public void setEncoder(double value) {}

        @Override
        public void setTarget(double value, double ffVolts) {}

        @Override
        public boolean isConnected() {
            return false;
        }

        public double getOffset() {
            return m_offset;
        }
    }

    SwerveDriverConfig m_config;
    boolean m_callbackComplete = false;

    private void setupSDConfig() {
        SwerveDriverConfig c = new SwerveDriverConfig();
        c.m_pwmChannel = 0;
        c.m_quadChannelA = 1;
        c.m_quadChannelB = 2;
        c.m_turnPositionCoefficient = 1;
        c.m_wheelOffset = 45;
        m_config = c;
    }

    @Before
    public void setup() {
        assert HAL.initialize(500, 0);
        setupSDConfig();
    }
    
    @Test
    public void testTurnEncoderInit() throws NoSuchElementException {
        m_config.m_motorType = SwerveDriverConfig.MotorType.TURNING;
        Assert.assertThrows("Encoder should not be created before the object is initialized.",
            NoSuchElementException.class,
            () -> {
                EncoderSim sim = EncoderSim.createForChannel(m_config.m_quadChannelA);
            });
        SwerveMotorBase motor = new SwerveMotorBaseInitable(m_config);
        // This will throw an error if the object has not been created.
        EncoderSim sim = EncoderSim.createForChannel(m_config.m_quadChannelA);
    }

    @Test
    public void testDriveEncoderNonInit() {
        m_config.m_motorType = SwerveDriverConfig.MotorType.DRIVE;
        Assert.assertThrows("Encoder should not be created on drive motors (before init).",
            NoSuchElementException.class, () -> {
                EncoderSim sim = EncoderSim.createForChannel(m_config.m_quadChannelA);
            });
        SwerveMotorBase motor = new SwerveMotorBaseInitable(m_config);
        Assert.assertThrows("Encoder should not be created on drive motors (after init).",
            NoSuchElementException.class, () -> {
                EncoderSim sim = EncoderSim.createForChannel(m_config.m_quadChannelA);
            });
    }
}
