-- ============================================================================
-- HealthPocket Database Schema
-- PostgreSQL 16+ / Neon Compatible
-- ============================================================================

-- Enable UUID extension for generating unique identifiers
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- USERS TABLE
-- ============================================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    birth_date DATE,
    gender VARCHAR(20), -- 'MALE', 'FEMALE', 'OTHER', 'PREFER_NOT_TO_SAY'
    blood_type VARCHAR(5), -- 'A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'
    allergies TEXT, -- JSON array of allergies
    emergency_contact_name VARCHAR(200),
    emergency_contact_phone VARCHAR(20),
    preferred_language VARCHAR(5) DEFAULT 'fr', -- 'fr', 'en', 'es'
    dark_mode_enabled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index for faster email lookups during authentication
CREATE INDEX idx_users_email ON users(email);

-- ============================================================================
-- MEDICATIONS TABLE
-- ============================================================================
CREATE TABLE medications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    dosage VARCHAR(100) NOT NULL, -- e.g., "500mg", "2 tablets"
    frequency VARCHAR(100) NOT NULL, -- e.g., "3 times per day"
    schedule_times TEXT NOT NULL, -- JSON array of times ["08:00", "14:00", "20:00"]
    start_date DATE NOT NULL,
    end_date DATE, -- NULL if ongoing
    notes TEXT,
    color VARCHAR(7) DEFAULT '#4CAF50', -- Hex color for UI
    reminder_enabled BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    sync_status VARCHAR(20) DEFAULT 'SYNCED', -- 'SYNCED', 'PENDING', 'ERROR'
    local_id VARCHAR(100), -- Local UUID for offline sync
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_medications_user_id ON medications(user_id);
CREATE INDEX idx_medications_is_active ON medications(is_active);
CREATE INDEX idx_medications_sync_status ON medications(sync_status);

-- ============================================================================
-- MEDICATION INTAKES TABLE (History of taken medications)
-- ============================================================================
CREATE TABLE medication_intakes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    medication_id UUID NOT NULL REFERENCES medications(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    scheduled_time TIMESTAMP WITH TIME ZONE NOT NULL,
    taken_time TIMESTAMP WITH TIME ZONE, -- NULL if not taken yet
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'TAKEN', 'SKIPPED', 'MISSED'
    notes TEXT,
    sync_status VARCHAR(20) DEFAULT 'SYNCED',
    local_id VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_medication_intakes_medication_id ON medication_intakes(medication_id);
CREATE INDEX idx_medication_intakes_user_id ON medication_intakes(user_id);
CREATE INDEX idx_medication_intakes_scheduled_time ON medication_intakes(scheduled_time);
CREATE INDEX idx_medication_intakes_status ON medication_intakes(status);

-- ============================================================================
-- APPOINTMENTS TABLE
-- ============================================================================
CREATE TABLE appointments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    doctor_name VARCHAR(200),
    location VARCHAR(300),
    appointment_date TIMESTAMP WITH TIME ZONE NOT NULL,
    duration_minutes INTEGER DEFAULT 30,
    reminder_minutes_before INTEGER DEFAULT 60, -- Reminder 1 hour before
    reminder_enabled BOOLEAN DEFAULT TRUE,
    status VARCHAR(20) DEFAULT 'SCHEDULED', -- 'SCHEDULED', 'COMPLETED', 'CANCELLED'
    notes TEXT,
    sync_status VARCHAR(20) DEFAULT 'SYNCED',
    local_id VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_appointments_user_id ON appointments(user_id);
CREATE INDEX idx_appointments_date ON appointments(appointment_date);
CREATE INDEX idx_appointments_status ON appointments(status);

-- ============================================================================
-- HEALTH LOGS TABLE (Daily journal)
-- ============================================================================
CREATE TABLE health_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    log_date DATE NOT NULL,
    mood INTEGER CHECK (mood >= 1 AND mood <= 5), -- 1=Very Bad, 5=Excellent
    energy_level INTEGER CHECK (energy_level >= 1 AND energy_level <= 5),
    sleep_quality INTEGER CHECK (sleep_quality >= 1 AND sleep_quality <= 5),
    sleep_hours DECIMAL(3,1),
    symptoms TEXT, -- JSON array of symptoms
    notes TEXT,
    sync_status VARCHAR(20) DEFAULT 'SYNCED',
    local_id VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, log_date) -- One log per day per user
);

CREATE INDEX idx_health_logs_user_id ON health_logs(user_id);
CREATE INDEX idx_health_logs_date ON health_logs(log_date);

-- ============================================================================
-- VITAL METRICS TABLE
-- ============================================================================
CREATE TABLE vital_metrics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    metric_type VARCHAR(50) NOT NULL, -- 'WEIGHT', 'BLOOD_PRESSURE', 'BLOOD_GLUCOSE', 'HEART_RATE', 'TEMPERATURE'
    value DECIMAL(10, 2) NOT NULL, -- Primary value
    secondary_value DECIMAL(10, 2), -- For blood pressure (diastolic), etc.
    unit VARCHAR(20) NOT NULL, -- 'kg', 'mmHg', 'mg/dL', 'bpm', '°C'
    measured_at TIMESTAMP WITH TIME ZONE NOT NULL,
    notes TEXT,
    sync_status VARCHAR(20) DEFAULT 'SYNCED',
    local_id VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_vital_metrics_user_id ON vital_metrics(user_id);
CREATE INDEX idx_vital_metrics_type ON vital_metrics(metric_type);
CREATE INDEX idx_vital_metrics_measured_at ON vital_metrics(measured_at);

-- ============================================================================
-- REFRESH TOKENS TABLE (For JWT refresh tokens)
-- ============================================================================
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);

-- ============================================================================
-- SYNC LOG TABLE (Track synchronization history)
-- ============================================================================
CREATE TABLE sync_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    entity_type VARCHAR(50) NOT NULL, -- 'MEDICATION', 'APPOINTMENT', 'HEALTH_LOG', 'VITAL_METRIC'
    entity_id UUID NOT NULL,
    action VARCHAR(20) NOT NULL, -- 'CREATE', 'UPDATE', 'DELETE'
    sync_direction VARCHAR(20) NOT NULL, -- 'UPLOAD', 'DOWNLOAD'
    status VARCHAR(20) NOT NULL, -- 'SUCCESS', 'FAILED', 'CONFLICT'
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sync_logs_user_id ON sync_logs(user_id);
CREATE INDEX idx_sync_logs_created_at ON sync_logs(created_at);

-- ============================================================================
-- FUNCTION: Update updated_at timestamp
-- ============================================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply trigger to all tables with updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_medications_updated_at BEFORE UPDATE ON medications
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_medication_intakes_updated_at BEFORE UPDATE ON medication_intakes
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_appointments_updated_at BEFORE UPDATE ON appointments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_health_logs_updated_at BEFORE UPDATE ON health_logs
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_vital_metrics_updated_at BEFORE UPDATE ON vital_metrics
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- SAMPLE DATA (Optional - for development)
-- ============================================================================
-- Uncomment below to insert sample data for testing

/*
-- Sample user (password: 'Password123!')
INSERT INTO users (id, email, password_hash, first_name, last_name, birth_date, gender, blood_type, allergies, preferred_language)
VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'demo@healthpocket.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mrq4HxZ1klFQWCqXn.kbZQ8bXxBkHKe', -- BCrypt hash of 'Password123!'
    'Jean',
    'Dupont',
    '1990-05-15',
    'MALE',
    'A+',
    '["Pénicilline", "Arachides"]',
    'fr'
);

-- Sample medication
INSERT INTO medications (user_id, name, dosage, frequency, schedule_times, start_date, notes)
VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'Doliprane',
    '1000mg',
    '3 fois par jour',
    '["08:00", "14:00", "20:00"]',
    CURRENT_DATE,
    'Prendre pendant les repas'
);

-- Sample appointment
INSERT INTO appointments (user_id, title, doctor_name, location, appointment_date, description)
VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'Consultation générale',
    'Dr. Martin',
    '15 rue de la Santé, 75014 Paris',
    CURRENT_TIMESTAMP + INTERVAL '7 days',
    'Bilan annuel'
);

-- Sample vital metrics
INSERT INTO vital_metrics (user_id, metric_type, value, unit, measured_at)
VALUES 
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'WEIGHT', 75.5, 'kg', CURRENT_TIMESTAMP),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'BLOOD_PRESSURE', 120, 'mmHg', CURRENT_TIMESTAMP),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'HEART_RATE', 72, 'bpm', CURRENT_TIMESTAMP);
*/

