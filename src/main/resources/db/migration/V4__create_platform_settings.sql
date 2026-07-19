CREATE TABLE platform_settings (
    id SERIAL PRIMARY KEY,
    website_title VARCHAR(255) NOT NULL DEFAULT 'DailySpends',
    theme_mode VARCHAR(50) NOT NULL DEFAULT 'dark',
    primary_color VARCHAR(50) NOT NULL DEFAULT '#ef4444',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Insert default settings row
INSERT INTO platform_settings (website_title, theme_mode, primary_color)
VALUES ('DailySpends', 'dark', '#ef4444');
