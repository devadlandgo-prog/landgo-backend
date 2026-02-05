-- V1__Initial_Schema.sql
-- LandGo Database Schema

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users Table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    phone VARCHAR(20),
    profile_image_url VARCHAR(500),
    auth_provider VARCHAR(20) NOT NULL DEFAULT 'EMAIL',
    provider_id VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    email_verified BOOLEAN DEFAULT FALSE,
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_provider ON users(auth_provider, provider_id);
CREATE INDEX idx_users_role ON users(role);

-- Vendor Profiles Table
CREATE TABLE vendor_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    company_name VARCHAR(100) NOT NULL,
    company_description TEXT,
    company_logo VARCHAR(500),
    business_license VARCHAR(255),
    business_address VARCHAR(255) NOT NULL,
    business_city VARCHAR(100) NOT NULL,
    business_state VARCHAR(100) NOT NULL,
    business_zip_code VARCHAR(20) NOT NULL,
    business_country VARCHAR(100) NOT NULL,
    website VARCHAR(255),
    verified BOOLEAN DEFAULT FALSE,
    rating DECIMAL(3,2),
    total_reviews INTEGER DEFAULT 0,
    total_lands_listed INTEGER DEFAULT 0,
    total_lands_sold INTEGER DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_vendor_user ON vendor_profiles(user_id);
CREATE INDEX idx_vendor_city ON vendor_profiles(business_city);
CREATE INDEX idx_vendor_verified ON vendor_profiles(verified);

-- Lands Table
CREATE TABLE lands (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    vendor_id UUID NOT NULL REFERENCES vendor_profiles(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    land_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_APPROVAL',
    
    -- Location
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    zip_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    
    -- Specifications
    price DECIMAL(15, 2) NOT NULL,
    area_sq_ft DECIMAL(15, 2) NOT NULL,
    frontage DECIMAL(10, 2),
    depth DECIMAL(10, 2),
    
    -- Features
    has_water_access BOOLEAN DEFAULT FALSE,
    has_electricity BOOLEAN DEFAULT FALSE,
    has_road_access BOOLEAN DEFAULT FALSE,
    has_sewage BOOLEAN DEFAULT FALSE,
    zoning_info VARCHAR(255),
    topography VARCHAR(255),
    soil_type VARCHAR(255),
    
    -- Media
    image_urls TEXT[], -- PostgreSQL array
    video_url VARCHAR(500),
    virtual_tour_url VARCHAR(500),
    
    -- Metrics
    view_count INTEGER DEFAULT 0,
    inquiry_count INTEGER DEFAULT 0,
    
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_land_vendor ON lands(vendor_id);
CREATE INDEX idx_land_status ON lands(status);
CREATE INDEX idx_land_city ON lands(city);
CREATE INDEX idx_land_type ON lands(land_type);
CREATE INDEX idx_land_price ON lands(price);
CREATE INDEX idx_land_created ON lands(created_at DESC);

-- Subscriptions Table
CREATE TABLE subscriptions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(50),
    payment_reference VARCHAR(255),
    auto_renew BOOLEAN DEFAULT FALSE,
    
    -- Feature limits
    max_vendor_views_per_month INTEGER,
    max_saved_lands INTEGER,
    can_access_premium_listings BOOLEAN DEFAULT FALSE,
    can_contact_vendor_directly BOOLEAN DEFAULT FALSE,
    
    -- Cancellation
    cancelled_at TIMESTAMP,
    cancellation_reason TEXT,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_subscription_user ON subscriptions(user_id);
CREATE INDEX idx_subscription_status ON subscriptions(status);
CREATE INDEX idx_subscription_end ON subscriptions(end_date);

-- User Saved Lands (Many-to-Many)
CREATE TABLE user_saved_lands (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    land_id UUID NOT NULL REFERENCES lands(id) ON DELETE CASCADE,
    saved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, land_id)
);

-- Function to auto-update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers for updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_vendor_profiles_updated_at BEFORE UPDATE ON vendor_profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_lands_updated_at BEFORE UPDATE ON lands
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_subscriptions_updated_at BEFORE UPDATE ON subscriptions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
