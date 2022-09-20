package org.frc4607.common.swerve;

public class SwerveDriveModuleTest {
    private static class TestDriver extends SwerveMotorBase {
        public double m_encoderPosition;
        public double m_encoderVelocity;

        public double m_value;
        public double m_feedforward;

        public boolean m_isConnected;

        public TestDriver(SwerveDriverConfig config) {
            super(config);
            //TODO Auto-generated constructor stub
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
    
}
